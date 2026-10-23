package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pascm.inspseg.domain.model.InspectionStatus

/**
 * Cabecera del recorrido: quien, donde, cuando y con que resultado se cerro.
 * Entidad de Room: solo la capa data/local la usa; hacia arriba viaja como
 * [com.pascm.inspseg.domain.model.Inspection] gracias a los mappers.
 */
@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zoneId: Long,
    val zoneName: String,
    val inspector: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val scorePercent: Double = 0.0,
    val status: InspectionStatus = InspectionStatus.EN_CAPTURA,
    val criticalStop: Boolean = false,
    val reinspectionDueAt: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** UID de Firebase del inspector dueño del registro (v2). */
    val ownerUid: String? = null,
    /** Confirmacion de Firestore; null = pendiente de subir (v2). */
    val syncedAt: Long? = null
)
