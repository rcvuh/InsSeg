package com.pascm.inspseg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pascm.inspseg.data.local.entity.IncidenceEntity
import com.pascm.inspseg.domain.model.IncidenceStatus
import kotlinx.coroutines.flow.Flow

/** Hallazgos detectados en campo y su seguimiento hasta el cierre. */
@Dao
interface IncidenceDao {

    @Insert
    suspend fun insertAll(incidences: List<IncidenceEntity>)

    @Query("SELECT * FROM incidences ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<IncidenceEntity>>

    @Query("SELECT * FROM incidences WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: IncidenceStatus): Flow<List<IncidenceEntity>>

    @Query("SELECT COUNT(*) FROM incidences WHERE status <> 'CERRADO'")
    fun observeOpenCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM incidences WHERE status <> 'CERRADO' AND stopsWork = 1")
    fun observeOpenCriticalCount(): Flow<Int>

    /** Cambiar el estado obliga a volver a subir el hallazgo (syncedAt = NULL). */
    @Query("UPDATE incidences SET status = :status, updatedAt = :updatedAt, syncedAt = NULL WHERE id = :id")
    suspend fun updateStatus(id: Long, status: IncidenceStatus, updatedAt: Long)

    @Query("DELETE FROM incidences WHERE inspectionId = :inspectionId")
    suspend fun deleteByInspection(inspectionId: Long)

    // ---- Sincronizacion con Firebase ----

    @Query("SELECT * FROM incidences WHERE ownerUid = :uid AND syncedAt IS NULL")
    suspend fun pendingSync(uid: String): List<IncidenceEntity>

    @Query("SELECT COUNT(*) FROM incidences WHERE ownerUid = :uid AND syncedAt IS NULL")
    fun observePendingSyncCount(uid: String): Flow<Int>

    @Query("UPDATE incidences SET syncedAt = :syncedAt WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: Long, updatedAt: Long, syncedAt: Long)

    @Query("UPDATE incidences SET ownerUid = :uid WHERE ownerUid IS NULL")
    suspend fun claimOrphans(uid: String)
}
