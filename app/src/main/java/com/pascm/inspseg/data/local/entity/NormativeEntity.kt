package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla de Normas. Guarda el codigo oficial y el requisito exigible, de modo que
 * cada punto de control pueda citar su fundamento normativo.
 */
@Entity(tableName = "normatives")
data class NormativeEntity(
    @PrimaryKey val code: String,
    val title: String,
    val requirement: String,
    val internationalEquivalent: String
)
