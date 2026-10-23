package com.pascm.inspseg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pascm.inspseg.domain.model.AnswerResult

/** Respuesta capturada para un punto de control dentro de un recorrido. */
@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChecklistItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["inspectionId", "itemId"], unique = true), Index("itemId")]
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectionId: Long,
    val itemId: Long,
    val result: AnswerResult = AnswerResult.SIN_EVALUAR,
    val note: String = "",
    val photoUri: String? = null,
    val answeredAt: Long? = null
)
