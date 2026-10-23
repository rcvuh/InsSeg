package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.IncidenceStatus

/**
 * Tabla de Hallazgos. Persiste localmente cada desviacion detectada con su estado
 * de mitigacion, estampa de tiempo y coordenadas GPS del punto de inspeccion.
 */
@Entity(
    tableName = "incidences",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inspectionId"), Index("status"), Index("syncedAt")]
)
data class IncidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectionId: Long,
    val itemId: Long,
    val itemTitle: String,
    val zoneName: String,
    val normativeCode: String,
    val criticality: CriticalityLevel,
    val stopsWork: Boolean,
    val note: String,
    val photoUri: String?,
    val status: IncidenceStatus = IncidenceStatus.ABIERTO,
    val createdAt: Long,
    val updatedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val ownerUid: String? = null,
    val syncedAt: Long? = null
)
