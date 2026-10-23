package com.pascm.inspseg.data.repository

import androidx.room.withTransaction
import com.pascm.inspseg.data.local.InspSegDatabase
import com.pascm.inspseg.data.local.SeedData
import com.pascm.inspseg.data.local.entity.AnswerEntity
import com.pascm.inspseg.data.local.entity.IncidenceEntity
import com.pascm.inspseg.data.local.entity.InspectionEntity
import com.pascm.inspseg.data.mapper.toDocument
import com.pascm.inspseg.data.mapper.toDomain
import com.pascm.inspseg.data.remote.FirestoreDataSource
import com.pascm.inspseg.data.remote.StorageDataSource
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.Normative
import com.pascm.inspseg.domain.model.SyncReport
import com.pascm.inspseg.domain.model.SyncSummary
import com.pascm.inspseg.domain.model.WorkZone
import com.pascm.inspseg.domain.model.ZoneRequirement
import com.pascm.inspseg.domain.repository.AuthRepository
import com.pascm.inspseg.domain.repository.InspectionRepository
import com.pascm.inspseg.domain.rules.CloudSecurityRules
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import com.pascm.inspseg.util.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

/**
 * Implementacion offline-first del repositorio de inspecciones.
 *
 *  1. Todo se guarda primero en Room: el inspector puede trabajar sin señal.
 *  2. Lo cerrado se sube a Firebase: Firestore para los datos y Cloud Storage
 *     para las fotografias, bajo la carpeta del inspector con sesion.
 *  3. Hacia la UI solo salen modelos de dominio; entidades de Room y documentos
 *     de Firestore se convierten con los mappers de data/mapper.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstInspectionRepository(
    private val db: InspSegDatabase,
    private val auth: AuthRepository,
    private val firestore: FirestoreDataSource?,
    private val storage: StorageDataSource?
) : InspectionRepository {

    private val catalogDao = db.catalogDao()
    private val inspectionDao = db.inspectionDao()
    private val incidenceDao = db.incidenceDao()
    private val syncMutex = Mutex()
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun requestSync() {
        syncScope.launch { runCatching { syncPending() } }
    }

    override suspend fun ensureSeeded() {
        if (catalogDao.countNormatives() > 0) return
        db.withTransaction {
            catalogDao.insertNormatives(SeedData.normatives)
            catalogDao.insertZones(SeedData.zones)
            catalogDao.insertChecklistItems(SeedData.checklist)
        }
    }

    override fun observeZones(): Flow<List<WorkZone>> =
        catalogDao.observeZones().map { list -> list.map { it.toDomain() } }

    override fun observeNormatives(): Flow<List<Normative>> =
        catalogDao.observeNormatives().map { list -> list.map { it.toDomain() } }

    override fun observeRecentInspections(): Flow<List<Inspection>> =
        inspectionDao.observeRecent(limit = 20).map { it.toDomain() }

    override fun observePendingReinspections(): Flow<List<Inspection>> =
        inspectionDao.observePendingReinspections().map { it.toDomain() }

    override fun observeIncidences(): Flow<List<Incidence>> =
        incidenceDao.observeAll().map { it.toDomain() }

    override fun observeOpenIncidenceCount(): Flow<Int> = incidenceDao.observeOpenCount()

    override fun observeOpenCriticalCount(): Flow<Int> = incidenceDao.observeOpenCriticalCount()

    override suspend fun getZone(zoneId: Long): WorkZone? = catalogDao.getZone(zoneId)?.toDomain()

    override suspend fun buildChecklist(zone: WorkZone): List<ChecklistPoint> =
        catalogDao.getChecklist().map { row ->
            val item = row.item.toDomain()
            val applicable = when (item.zoneRequirement) {
                ZoneRequirement.SIEMPRE -> true
                ZoneRequirement.OCULAR -> zone.requiresEyeProtection
                ZoneRequirement.AUDITIVA -> zone.requiresHearingProtection
                ZoneRequirement.ALTURA -> zone.involvesHeightWork
            }
            ChecklistPoint(
                item = item,
                normative = row.normative.toDomain(),
                applicable = applicable,
                result = if (applicable) AnswerResult.SIN_EVALUAR else AnswerResult.NO_APLICA
            )
        }

    override suspend fun saveInspection(
        zone: WorkZone,
        inspector: String,
        points: List<ChecklistPoint>,
        evaluation: SafetyRuleEngine.Evaluation,
        location: GeoPoint?,
        startedAt: Long
    ): Long = db.withTransaction {
        val now = System.currentTimeMillis()
        val ownerUid = auth.currentInspector()?.uid

        val inspectionId = inspectionDao.insert(
            InspectionEntity(
                zoneId = zone.id,
                zoneName = zone.name,
                inspector = inspector,
                startedAt = startedAt,
                finishedAt = now,
                scorePercent = evaluation.scorePercent,
                status = evaluation.status,
                criticalStop = evaluation.criticalStop,
                reinspectionDueAt = evaluation.reinspectionDueAt,
                latitude = location?.latitude,
                longitude = location?.longitude,
                ownerUid = ownerUid
            )
        )

        inspectionDao.upsertAnswers(
            points.map { point ->
                AnswerEntity(
                    inspectionId = inspectionId,
                    itemId = point.id,
                    result = point.result,
                    note = point.note,
                    photoUri = point.photoUri,
                    answeredAt = if (point.result == AnswerResult.SIN_EVALUAR) null else now
                )
            }
        )

        incidenceDao.insertAll(
            points.filter { it.applicable && it.result == AnswerResult.NO_CONFORME }
                .map { point ->
                    IncidenceEntity(
                        inspectionId = inspectionId,
                        itemId = point.id,
                        itemTitle = point.item.title,
                        zoneName = zone.name,
                        normativeCode = point.item.normativeCode,
                        criticality = point.item.criticality,
                        stopsWork = point.item.stopsWork,
                        note = point.note,
                        photoUri = point.photoUri,
                        status = IncidenceStatus.ABIERTO,
                        createdAt = now,
                        updatedAt = now,
                        latitude = location?.latitude,
                        longitude = location?.longitude,
                        ownerUid = ownerUid
                    )
                }
        )

        inspectionId
    }

    override suspend fun updateIncidenceStatus(id: Long, status: IncidenceStatus) =
        incidenceDao.updateStatus(id, status, System.currentTimeMillis())

    override fun observeSyncSummary(): Flow<SyncSummary> =
        auth.observeInspector().flatMapLatest { inspector ->
            if (inspector == null) {
                flowOf(SyncSummary())
            } else {
                val cloud: Flow<Int?> = firestore?.observeInspectionCount(inspector.uid)
                    ?.map<Int, Int?> { it }?.onStart { emit(null) }
                    ?: flowOf(null)
                combine(
                    inspectionDao.observePendingSyncCount(inspector.uid),
                    incidenceDao.observePendingSyncCount(inspector.uid),
                    cloud
                ) { pendingInspections, pendingIncidences, cloudCount ->
                    SyncSummary(pendingInspections, pendingIncidences, cloudCount)
                }
            }
        }

    /**
     * Sube lo pendiente del inspector con sesion. Primero las fotos (Storage), luego
     * el recorrido con sus respuestas y por ultimo los hallazgos (Firestore). Cada
     * escritura tiene un tiempo limite para no colgar la app sin señal; lo que no
     * alcanza a subir queda pendiente para el siguiente intento.
     */
    override suspend fun syncPending(): SyncReport = syncMutex.withLock {
        val inspector = auth.currentInspector()
            ?: return SyncReport(0, 0, 0, "Inicia sesion para respaldar en la nube.")
        val remote = firestore
            ?: return SyncReport(0, 0, 0, "Firebase no esta configurado en este proyecto.")
        val uid = inspector.uid

        // Lo capturado sin sesion en este telefono pasa a ser del inspector actual.
        inspectionDao.claimOrphans(uid)
        incidenceDao.claimOrphans(uid)

        var inspections = 0
        var incidences = 0
        var photos = 0
        val photoUrls = mutableMapOf<String, String>() // uri local -> url de Storage
        try {
            for (entity in inspectionDao.pendingSync(uid)) {
                val inspection = entity.toDomain()
                if (!CloudSecurityRules.canUpload(inspection, uid)) continue
                val answers = inspectionDao.getAnswers(inspection.id).map { it.toDomain() }
                val urlsByItem = mutableMapOf<Long, String>()
                for (answer in answers) {
                    val local = answer.photoUri ?: continue
                    val url = photoUrls[local] ?: withTimeout(UPLOAD_TIMEOUT_MS) {
                        storage?.uploadEvidence(uid, inspection.id, answer.itemId, local)
                    }?.also { photoUrls[local] = it; photos++ }
                    if (url != null) urlsByItem[answer.itemId] = url
                }
                val now = System.currentTimeMillis()
                withTimeout(WRITE_TIMEOUT_MS) {
                    remote.saveInspection(uid, inspection.toDocument(uid, answers, urlsByItem, now))
                }
                inspectionDao.markSynced(inspection.id, now)
                inspections++
            }
            for (entity in incidenceDao.pendingSync(uid)) {
                val incidence = entity.toDomain()
                if (!CloudSecurityRules.canUpload(incidence, uid)) continue
                val photoUrl = incidence.photoUri?.let { local ->
                    photoUrls[local] ?: withTimeout(UPLOAD_TIMEOUT_MS) {
                        storage?.uploadEvidence(uid, incidence.inspectionId, incidence.itemId, local)
                    }?.also { photoUrls[local] = it; photos++ }
                }
                withTimeout(WRITE_TIMEOUT_MS) {
                    remote.saveIncidence(uid, incidence.toDocument(uid, photoUrl))
                }
                incidenceDao.markSynced(incidence.id, incidence.updatedAt, System.currentTimeMillis())
                incidences++
            }
            SyncReport(inspections, incidences, photos)
        } catch (e: Exception) {
            SyncReport(inspections, incidences, photos, "Sin conexion con Firebase; se reintentara.")
        }
    }

    private companion object {
        const val UPLOAD_TIMEOUT_MS = 30_000L
        const val WRITE_TIMEOUT_MS = 15_000L
    }
}
