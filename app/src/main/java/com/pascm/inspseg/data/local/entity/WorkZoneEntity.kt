package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Zona de trabajo del recorrido. Sus banderas determinan que puntos de control
 * son aplicables (proteccion ocular, auditiva y trabajos en altura).
 */
@Entity(tableName = "work_zones")
data class WorkZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val requiresEyeProtection: Boolean,
    val requiresHearingProtection: Boolean,
    val involvesHeightWork: Boolean
)
