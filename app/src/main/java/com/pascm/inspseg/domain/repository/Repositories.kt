package com.pascm.inspseg.domain.repository

import com.pascm.inspseg.domain.model.AuthResult
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.Inspector
import com.pascm.inspseg.domain.model.Normative
import com.pascm.inspseg.domain.model.SyncReport
import com.pascm.inspseg.domain.model.SyncSummary
import com.pascm.inspseg.domain.model.WorkZone
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import com.pascm.inspseg.util.GeoPoint
import kotlinx.coroutines.flow.Flow

/*
 * Contratos de la capa de dominio (Clean Architecture). Los ViewModels dependen
 * de estas interfaces; las implementaciones viven en data/repository y son las
 * unicas que conocen Room y Firebase.
 */

interface InspectionRepository {
    suspend fun ensureSeeded()
    fun observeZones(): Flow<List<WorkZone>>
    fun observeNormatives(): Flow<List<Normative>>
    fun observeRecentInspections(): Flow<List<Inspection>>
    fun observePendingReinspections(): Flow<List<Inspection>>
    fun observeIncidences(): Flow<List<Incidence>>
    fun observeOpenIncidenceCount(): Flow<Int>
    fun observeOpenCriticalCount(): Flow<Int>
    suspend fun getZone(zoneId: Long): WorkZone?
    suspend fun buildChecklist(zone: WorkZone): List<ChecklistPoint>
    suspend fun saveInspection(
        zone: WorkZone,
        inspector: String,
        points: List<ChecklistPoint>,
        evaluation: SafetyRuleEngine.Evaluation,
        location: GeoPoint?,
        startedAt: Long
    ): Long
    suspend fun updateIncidenceStatus(id: Long, status: IncidenceStatus)

    /** Pendientes locales y conteo en Firestore (en tiempo real) del inspector activo. */
    fun observeSyncSummary(): Flow<SyncSummary>

    /** Sube a Firestore y Storage todo lo pendiente del inspector con sesion. */
    suspend fun syncPending(): SyncReport

    /** Lanza la sincronizacion en segundo plano, sin depender de la pantalla que la pide. */
    fun requestSync()
}

interface AuthRepository {
    val isAvailable: Boolean
    fun observeInspector(): Flow<Inspector?>
    fun currentInspector(): Inspector?
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun sendPasswordReset(email: String): AuthResult
    fun signOut()
}
