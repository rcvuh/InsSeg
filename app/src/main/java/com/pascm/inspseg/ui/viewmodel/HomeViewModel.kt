package com.pascm.inspseg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.Inspector
import com.pascm.inspseg.domain.model.Normative
import com.pascm.inspseg.domain.model.SyncSummary
import com.pascm.inspseg.domain.model.WorkZone
import com.pascm.inspseg.domain.repository.AuthRepository
import com.pascm.inspseg.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val zones: List<WorkZone> = emptyList(),
    val recentInspections: List<Inspection> = emptyList(),
    val pendingReinspections: List<Inspection> = emptyList(),
    val openIncidences: Int = 0,
    val openCriticalIncidences: Int = 0
)

/** Estado de la tarjeta "Respaldo en la nube" del tablero. */
data class CloudUiState(
    val inspector: Inspector? = null,
    val summary: SyncSummary = SyncSummary(),
    val syncing: Boolean = false,
    val lastMessage: String? = null
)

class HomeViewModel(
    private val repository: InspectionRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _inspector = MutableStateFlow(auth.currentInspector()?.displayName.orEmpty())
    val inspector: StateFlow<String> = _inspector.asStateFlow()

    val normatives: StateFlow<List<Normative>> = repository.observeNormatives()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<HomeUiState> = combine(
        repository.observeZones(),
        repository.observeRecentInspections(),
        repository.observePendingReinspections(),
        repository.observeOpenIncidenceCount(),
        repository.observeOpenCriticalCount()
    ) { zones, recent, pending, open, critical ->
        HomeUiState(
            zones = zones,
            recentInspections = recent,
            pendingReinspections = pending.filter { it.reinspectionDueAt != null },
            openIncidences = open,
            openCriticalIncidences = critical
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val syncing = MutableStateFlow(false)
    private val lastMessage = MutableStateFlow<String?>(null)

    val cloud: StateFlow<CloudUiState> = combine(
        auth.observeInspector(),
        repository.observeSyncSummary(),
        syncing,
        lastMessage
    ) { inspector, summary, busy, message ->
        CloudUiState(inspector, summary, busy, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CloudUiState())

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    fun setInspector(name: String) {
        _inspector.value = name
    }

    fun syncNow() {
        if (syncing.value) return
        syncing.value = true
        viewModelScope.launch {
            val report = repository.syncPending()
            lastMessage.value = report.error ?: buildString {
                append("Subidos: ${report.uploadedInspections} recorrido(s), ")
                append("${report.uploadedIncidences} hallazgo(s), ${report.uploadedPhotos} foto(s).")
            }
            syncing.update { false }
        }
    }

    fun signOut() = auth.signOut()
}

data class IncidencesUiState(
    val incidences: List<Incidence> = emptyList(),
    val filter: IncidenceStatus? = null
)

class IncidenceViewModel(private val repository: InspectionRepository) : ViewModel() {

    private val filter = MutableStateFlow<IncidenceStatus?>(null)

    val state: StateFlow<IncidencesUiState> = combine(
        repository.observeIncidences(),
        filter
    ) { incidences, active ->
        IncidencesUiState(
            incidences = incidences.filter { active == null || it.status == active },
            filter = active
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IncidencesUiState())

    fun setFilter(status: IncidenceStatus?) {
        filter.value = status
    }

    /** Actualiza Room y de inmediato intenta reflejar el cambio en Firestore. */
    fun advance(id: Long, status: IncidenceStatus) {
        viewModelScope.launch {
            repository.updateIncidenceStatus(id, status)
            repository.requestSync()
        }
    }
}
