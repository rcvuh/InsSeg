package com.pascm.inspseg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pascm.inspseg.data.local.entity.AnswerEntity
import com.pascm.inspseg.data.local.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

/** Cabeceras de recorrido y respuestas capturadas. */
@Dao
interface InspectionDao {

    @Insert
    suspend fun insert(inspection: InspectionEntity): Long

    @Update
    suspend fun update(inspection: InspectionEntity)

    @Query("SELECT * FROM inspections WHERE id = :id")
    suspend fun getById(id: Long): InspectionEntity?

    @Query("SELECT * FROM inspections ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE reinspectionDueAt IS NOT NULL AND status <> 'EN_CAPTURA' ORDER BY reinspectionDueAt")
    fun observePendingReinspections(): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnswers(answers: List<AnswerEntity>)

    @Query("SELECT * FROM answers WHERE inspectionId = :inspectionId")
    suspend fun getAnswers(inspectionId: Long): List<AnswerEntity>

    @Query("DELETE FROM answers WHERE inspectionId = :inspectionId")
    suspend fun clearAnswers(inspectionId: Long)

    // ---- Sincronizacion con Firebase ----

    @Query("SELECT * FROM inspections WHERE ownerUid = :uid AND syncedAt IS NULL AND status <> 'EN_CAPTURA'")
    suspend fun pendingSync(uid: String): List<InspectionEntity>

    @Query("SELECT COUNT(*) FROM inspections WHERE ownerUid = :uid AND syncedAt IS NULL")
    fun observePendingSyncCount(uid: String): Flow<Int>

    @Query("UPDATE inspections SET syncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Long, syncedAt: Long)

    /** Recorridos capturados sin sesion: se asignan al inspector cuando inicia sesion. */
    @Query("UPDATE inspections SET ownerUid = :uid WHERE ownerUid IS NULL")
    suspend fun claimOrphans(uid: String)
}
