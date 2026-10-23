package com.pascm.inspseg.domain.model

/** Recorrido cerrado por un inspector. */
data class Inspection(
    val id: Long,
    val zoneId: Long,
    val zoneName: String,
    val inspector: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val scorePercent: Double,
    val status: InspectionStatus,
    val criticalStop: Boolean,
    val reinspectionDueAt: Long?,
    val latitude: Double?,
    val longitude: Double?,
    /** UID de Firebase del inspector que cerro el recorrido (null si se capturo sin sesion). */
    val ownerUid: String?,
    /** Momento en que Firestore confirmo la copia en la nube; null = pendiente. */
    val syncedAt: Long?
) {
    val isSynced: Boolean get() = syncedAt != null
}

/** Respuesta a un punto de control dentro de un recorrido. */
data class Answer(
    val inspectionId: Long,
    val itemId: Long,
    val result: AnswerResult,
    val note: String,
    val photoUri: String?,
    val answeredAt: Long?
)

/** Hallazgo (no conformidad) con su seguimiento hasta el cierre. */
data class Incidence(
    val id: Long,
    val inspectionId: Long,
    val itemId: Long,
    val itemTitle: String,
    val zoneName: String,
    val normativeCode: String,
    val criticality: CriticalityLevel,
    val stopsWork: Boolean,
    val note: String,
    val photoUri: String?,
    val status: IncidenceStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val ownerUid: String?,
    val syncedAt: Long?
) {
    val isSynced: Boolean get() = syncedAt != null
}
