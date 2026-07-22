package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.VetDao
import com.example.data.entities.AnimalEntity
import com.example.data.entities.DiseaseAlertEntity
import com.example.data.entities.HealthLogEntity
import com.example.data.entities.VaccinationEntity

@Database(
    entities = [AnimalEntity::class, HealthLogEntity::class, VaccinationEntity::class, DiseaseAlertEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VetDatabase : RoomDatabase() {
    abstract fun vetDao(): VetDao

    companion object {
        @Volatile
        private var INSTANCE: VetDatabase? = null

        fun getDatabase(context: Context): VetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VetDatabase::class.java,
                    "vet_record_ai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
