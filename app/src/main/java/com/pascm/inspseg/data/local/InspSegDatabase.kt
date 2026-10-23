package com.pascm.inspseg.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pascm.inspseg.data.local.dao.CatalogDao
import com.pascm.inspseg.data.local.dao.IncidenceDao
import com.pascm.inspseg.data.local.dao.InspectionDao
import com.pascm.inspseg.data.local.entity.AnswerEntity
import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.IncidenceEntity
import com.pascm.inspseg.data.local.entity.InspectionEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity
import com.pascm.inspseg.data.local.entity.WorkZoneEntity

@Database(
    entities = [
        NormativeEntity::class,
        WorkZoneEntity::class,
        ChecklistItemEntity::class,
        InspectionEntity::class,
        AnswerEntity::class,
        IncidenceEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class InspSegDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun incidenceDao(): IncidenceDao

    companion object {
        @Volatile
        private var instance: InspSegDatabase? = null

        /**
         * v1 -> v2: columnas para la nube. ownerUid liga cada registro con su
         * inspector de Firebase y syncedAt marca lo que ya confirmo Firestore.
         * Los recorridos capturados antes de Firebase se conservan.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE inspections ADD COLUMN ownerUid TEXT")
                db.execSQL("ALTER TABLE inspections ADD COLUMN syncedAt INTEGER")
                db.execSQL("ALTER TABLE incidences ADD COLUMN ownerUid TEXT")
                db.execSQL("ALTER TABLE incidences ADD COLUMN syncedAt INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_incidences_syncedAt ON incidences (syncedAt)")
            }
        }

        fun get(context: Context): InspSegDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                InspSegDatabase::class.java,
                "inspseg.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
