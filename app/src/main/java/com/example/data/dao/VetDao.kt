package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.AnimalEntity
import com.example.data.entities.DiseaseAlertEntity
import com.example.data.entities.HealthLogEntity
import com.example.data.entities.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VetDao {
    // Animal queries
    @Query("SELECT * FROM animals ORDER BY id DESC")
    fun getAllAnimals(): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: Int): AnimalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity): Long

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Query("DELETE FROM animals WHERE id = :id")
    suspend fun deleteAnimal(id: Int)

    // Health logs queries
    @Query("SELECT * FROM health_logs ORDER BY logDate DESC")
    fun getAllHealthLogs(): Flow<List<HealthLogEntity>>

    @Query("SELECT * FROM health_logs WHERE animalId = :animalId ORDER BY logDate DESC")
    fun getHealthLogsForAnimal(animalId: Int): Flow<List<HealthLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthLog(log: HealthLogEntity): Long

    // Vaccination queries
    @Query("SELECT * FROM vaccinations ORDER BY scheduledDate ASC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId ORDER BY scheduledDate ASC")
    fun getVaccinationsForAnimal(animalId: Int): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity): Long

    @Query("UPDATE vaccinations SET status = :status, completedDate = :completedDate WHERE id = :id")
    suspend fun updateVaccinationStatus(id: Int, status: String, completedDate: Long?)

    // Disease alert queries
    @Query("SELECT * FROM disease_alerts ORDER BY id DESC")
    fun getAllDiseaseAlerts(): Flow<List<DiseaseAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseaseAlerts(alerts: List<DiseaseAlertEntity>)

    @Query("DELETE FROM disease_alerts")
    suspend fun clearDiseaseAlerts()
}
