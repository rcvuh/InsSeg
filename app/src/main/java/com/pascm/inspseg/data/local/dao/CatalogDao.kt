package com.pascm.inspseg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity
import com.pascm.inspseg.data.local.entity.WorkZoneEntity
import com.pascm.inspseg.data.local.relation.ChecklistItemWithNormative
import kotlinx.coroutines.flow.Flow

/** Acceso al catalogo normativo: normas, zonas y puntos de control. */
@Dao
interface CatalogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNormatives(items: List<NormativeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(items: List<WorkZoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Query("SELECT COUNT(*) FROM normatives")
    suspend fun countNormatives(): Int

    @Query("SELECT * FROM work_zones ORDER BY name")
    fun observeZones(): Flow<List<WorkZoneEntity>>

    @Query("SELECT * FROM work_zones WHERE id = :zoneId")
    suspend fun getZone(zoneId: Long): WorkZoneEntity?

    @Transaction
    @Query("SELECT * FROM checklist_items ORDER BY orderIndex")
    suspend fun getChecklist(): List<ChecklistItemWithNormative>

    @Query("SELECT * FROM normatives ORDER BY code")
    fun observeNormatives(): Flow<List<NormativeEntity>>
}
