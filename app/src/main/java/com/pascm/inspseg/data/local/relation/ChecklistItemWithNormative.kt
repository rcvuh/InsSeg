package com.pascm.inspseg.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity

/** Punto de control junto con la norma que lo fundamenta. */
data class ChecklistItemWithNormative(
    @Embedded val item: ChecklistItemEntity,
    @Relation(parentColumn = "normativeCode", entityColumn = "code")
    val normative: NormativeEntity
)
