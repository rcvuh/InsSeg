package com.pascm.inspseg.domain.rules

import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.InspectionStatus
import java.util.concurrent.TimeUnit

/**
 * Traduccion de las normas a reglas de negocio ejecutables.
 *
 * Aqui viven las dos reglas que exige el planteamiento:
 *
 *  1. Regla de paro critico: si un punto marcado como `stopsWork` resulta no
 *     conforme (andamio sin tarjeta verde, arnes con costuras dañadas, LOTO
 *     ausente...), el recorrido pasa a DETENCION_LABORES, se inhabilita marcarlo
 *     como conforme y se exige fotografia y nota antes de poder guardar.
 *
 *  2. Regla de ponderacion de riesgo: el cumplimiento se pondera por criticidad
 *     (ALTO 5, MEDIO 3, BAJO 1). Por debajo de [MIN_COMPLIANCE_PERCENT] la norma
 *     obliga a reinspeccion dentro de [REINSPECTION_WINDOW_HOURS] horas.
 *
 * Es logica pura y sin dependencias de Android: el ViewModel la invoca en cada
 * cambio de respuesta y es directamente testeable con JUnit.
 */
object SafetyRuleEngine {

    const val MIN_COMPLIANCE_PERCENT = 85.0
    const val REINSPECTION_WINDOW_HOURS = 24L

    fun evaluate(points: List<ChecklistPoint>, now: Long = System.currentTimeMillis()): Evaluation {
        val applicable = points.filter { it.applicable }
        val evaluated = applicable.filter {
            it.result == AnswerResult.CONFORME || it.result == AnswerResult.NO_CONFORME
        }

        val possibleWeight = evaluated.sumOf { it.item.criticality.weight }
        val obtainedWeight = evaluated
            .filter { it.result == AnswerResult.CONFORME }
            .sumOf { it.item.criticality.weight }

        val score = if (possibleWeight == 0) 100.0 else obtainedWeight * 100.0 / possibleWeight

        val stopPoints = applicable.filter { it.triggersStop }
        val criticalStop = stopPoints.isNotEmpty()

        val pendingEvidence = applicable.filter {
            it.result == AnswerResult.NO_CONFORME && !it.evidenceComplete
        }

        val pendingAnswers = applicable.count { it.result == AnswerResult.SIN_EVALUAR }
        // Un formulario vacio no esta "completo": evita habilitar el guardado antes de cargar.
        val complete = applicable.isNotEmpty() && pendingAnswers == 0

        val status = when {
            criticalStop -> InspectionStatus.DETENCION_LABORES
            !complete -> InspectionStatus.EN_CAPTURA
            score < MIN_COMPLIANCE_PERCENT -> InspectionStatus.REINSPECCION_24H
            else -> InspectionStatus.CONFORME
        }

        val requiresReinspection = criticalStop || (complete && score < MIN_COMPLIANCE_PERCENT)

        return Evaluation(
            applicableCount = applicable.size,
            answeredCount = applicable.size - pendingAnswers,
            evaluatedCount = evaluated.size,
            nonConformCount = evaluated.count { it.result == AnswerResult.NO_CONFORME },
            scorePercent = score,
            criticalStop = criticalStop,
            stopPointTitles = stopPoints.map { it.item.title },
            pendingEvidenceIds = pendingEvidence.map { it.id },
            pendingAnswers = pendingAnswers,
            status = status,
            // El paro critico bloquea el cierre "Conforme" aunque el puntaje sea alto.
            canMarkAsConform = !criticalStop && complete && score >= MIN_COMPLIANCE_PERCENT,
            canSave = complete && pendingEvidence.isEmpty(),
            reinspectionDueAt = if (requiresReinspection) {
                now + TimeUnit.HOURS.toMillis(REINSPECTION_WINDOW_HOURS)
            } else {
                null
            }
        )
    }

    /** Resultado de aplicar las reglas normativas al estado actual de captura. */
    data class Evaluation(
        val applicableCount: Int,
        val answeredCount: Int,
        val evaluatedCount: Int,
        val nonConformCount: Int,
        val scorePercent: Double,
        val criticalStop: Boolean,
        val stopPointTitles: List<String>,
        val pendingEvidenceIds: List<Long>,
        val pendingAnswers: Int,
        val status: InspectionStatus,
        val canMarkAsConform: Boolean,
        val canSave: Boolean,
        val reinspectionDueAt: Long?
    ) {
        val progress: Float
            get() = if (applicableCount == 0) 0f else answeredCount.toFloat() / applicableCount
    }
}
