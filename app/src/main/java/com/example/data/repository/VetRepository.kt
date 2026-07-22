package com.example.data.repository

import com.example.data.dao.VetDao
import com.example.data.entities.AnimalEntity
import com.example.data.entities.DiseaseAlertEntity
import com.example.data.entities.HealthLogEntity
import com.example.data.entities.VaccinationEntity
import kotlinx.coroutines.flow.Flow

class VetRepository(private val vetDao: VetDao) {

    val allAnimals: Flow<List<AnimalEntity>> = vetDao.getAllAnimals()
    val allHealthLogs: Flow<List<HealthLogEntity>> = vetDao.getAllHealthLogs()
    val allVaccinations: Flow<List<VaccinationEntity>> = vetDao.getAllVaccinations()
    val allDiseaseAlerts: Flow<List<DiseaseAlertEntity>> = vetDao.getAllDiseaseAlerts()

    suspend fun getAnimalById(id: Int): AnimalEntity? = vetDao.getAnimalById(id)

    suspend fun insertAnimal(animal: AnimalEntity): Long = vetDao.insertAnimal(animal)

    suspend fun updateAnimal(animal: AnimalEntity) = vetDao.updateAnimal(animal)

    suspend fun deleteAnimal(id: Int) = vetDao.deleteAnimal(id)

    fun getHealthLogsForAnimal(animalId: Int): Flow<List<HealthLogEntity>> =
        vetDao.getHealthLogsForAnimal(animalId)

    suspend fun insertHealthLog(log: HealthLogEntity): Long = vetDao.insertHealthLog(log)

    fun getVaccinationsForAnimal(animalId: Int): Flow<List<VaccinationEntity>> =
        vetDao.getVaccinationsForAnimal(animalId)

    suspend fun insertVaccination(vaccination: VaccinationEntity): Long =
        vetDao.insertVaccination(vaccination)

    suspend fun markVaccinationCompleted(id: Int, completedDate: Long = System.currentTimeMillis()) {
        vetDao.updateVaccinationStatus(id, "Completed", completedDate)
    }

    suspend fun seedInitialDataIfEmpty() {
        // We will seed mock data if animals list is empty in ViewModel
    }

    suspend fun seedDiseaseAlerts(alerts: List<DiseaseAlertEntity>) {
        vetDao.clearDiseaseAlerts()
        vetDao.insertDiseaseAlerts(alerts)
    }
}
