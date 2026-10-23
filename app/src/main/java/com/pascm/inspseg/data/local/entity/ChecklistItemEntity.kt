package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.ZoneRequirement

/**
 * Tabla de Puntos de Control. Relaciona cada item del formulario con su norma,
 * su nivel de criticidad y si exige evidencia fotografica obligatoria.
 *
 * [stopsWork] marca los items que, al resultar no conformes, disparan la regla de
 * paro critico (por ejemplo: andamio sin tarjeta verde, arnes con costuras dañadas).
 */
@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = NormativeEntity::class,
            parentColumns = ["code"],
            childColumns = ["normativeCode"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("normativeCode"), Index("module")]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val module: InspectionModule,
    val title: String,
    val criterion: String,
    val normativeCode: String,
    val criticality: CriticalityLevel,
    val requiresPhoto: Boolean,
    val stopsWork: Boolean,
    val zoneRequirement: ZoneRequirement = ZoneRequirement.SIEMPRE,
    val orderIndex: Int
)
