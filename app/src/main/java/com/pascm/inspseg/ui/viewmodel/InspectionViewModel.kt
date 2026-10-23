package com.pascm.inspseg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.WorkZone
import com.pascm.inspseg.domain.repository.InspectionRepository
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import com.pascm.inspseg.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de captura. La evaluacion normativa viaja dentro del
 * estado: la UI no calcula reglas, solo las refleja.
 */
data class InspectionUiState(
    val loading: Boolean = true,
    val zone: WorkZone? = null,
    val inspector: String = "",
    val points: List<ChecklistPoint> = emptyList(),
    val evaluation: SafetyRuleEngine.Evaluation = SafetyRuleEngine.evaluate(emptyList()),
    val startedAt: Long = System.currentTimeMillis(),
    val location: GeoPoint? = null,
    val saving: Boolean = false,
    val savedInspectionId: Long? = null,
    val message: String? = null
) {
    fun pointsOf(module: InspectionModule): List<ChecklistPoint> =
        points.filter { it.item.module == module }

    fun moduleProgress(module: InspectionModule): Pair<Int, Int> {
        val applicable = pointsOf(module).filter { it.applicable }
        return applicable.count { it.result != AnswerResult.SIN_EVALUAR } to applicable.size
    }

    /** Puntos que bloquean el guardado por evidencia incompleta. */
    val blockingPoints: List<ChecklistPoint>
        get() = points.filter { it.id in evaluation.pendingEvidenceIds }
}

class InspectionViewModel(private val repository: InspectionRepository) : ViewModel() {

    private val _state = MutableStateFlow(InspectionUiState())
    val state: StateFlow<InspectionUiState> = _state.asStateFlow()

    fun load(zoneId: Long, inspector: String) {
        viewModelScope.launch {
            val zone = repository.getZone(zoneId)
            if (zone == null) {
                _state.update { it.copy(loading = false, message = "La zona ya no existe.") }
                return@launch
            }
            val points = repository.buildChecklist(zone)
            _state.update {
                it.copy(
                    loading = false,
                    zone = zone,
                    inspector = inspector,
                    points = points,
                    evaluation = SafetyRuleEngine.evaluate(points),
                    startedAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun setLocation(point: GeoPoint?) = _state.update { it.copy(location = point) }

    fun setResult(itemId: Long, result: AnswerResult) = mutate(itemId) { point ->
        // Al volver a conforme se descarta la evidencia: ya no corresponde a un hallazgo.
        if (result == AnswerResult.CONFORME) {
            point.copy(result = result, note = "", photoUri = null)
        } else {
            point.copy(result = result)
        }
    }

    fun setNote(itemId: Long, note: String) = mutate(itemId) { it.copy(note = note) }

    fun setPhoto(itemId: Long, uri: String?) = mutate(itemId) { it.copy(photoUri = uri) }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    /**
     * Guarda el recorrido. La regla de paro critico y la de evidencia obligatoria
     * se verifican tambien aqui, no solo en la UI: el estado es la unica autoridad.
     */
    fun save() {
        val current = _state.value
        val zone = current.zone ?: return
        if (!current.evaluation.canSave || current.saving) {
            _state.update { it.copy(message = "Faltan respuestas o evidencia obligatoria.") }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val id = repository.saveInspection(
                zone = zone,
                inspector = current.inspector,
                points = current.points,
                evaluation = current.evaluation,
                location = current.location,
                startedAt = current.startedAt
            )
            _state.update { it.copy(saving = false, savedInspectionId = id) }
            // Respaldo en Firebase sin bloquear al inspector: si no hay señal queda pendiente.
            repository.requestSync()
        }
    }

    private fun mutate(itemId: Long, transform: (ChecklistPoint) -> ChecklistPoint) {
        _state.update { current ->
            val points = current.points.map { point ->
                if (point.id == itemId && point.applicable) transform(point) else point
            }
            // Cada captura vuelve a pasar por el motor de reglas: puntaje, paro
            // critico y evidencia pendiente se recalculan en tiempo real.
            current.copy(points = points, evaluation = SafetyRuleEngine.evaluate(points))
        }
    }
}
