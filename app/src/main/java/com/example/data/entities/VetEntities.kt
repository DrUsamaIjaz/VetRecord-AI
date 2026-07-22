package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tagOrName: String,
    val species: String, // Cattle, Goat, Sheep, Poultry, Dog, Cat, Pig, Horse
    val breed: String,
    val category: String, // Livestock or Pet
    val gender: String, // Male or Female
    val ageYears: Double,
    val location: String,
    val birthDate: String = "",
    val healthStatus: String = "Healthy", // Healthy, Under Treatment, Vaccine Due, Sick
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_logs")
data class HealthLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val animalTagOrName: String,
    val logDate: Long = System.currentTimeMillis(),
    val symptoms: String,
    val diagnosisOrNote: String,
    val treatmentGiven: String = "",
    val dosage: String = "",
    val administeredBy: String = "Farmer",
    val source: String = "Voice Input" // Voice Input, Manual, AI Assessment
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val animalTagOrName: String,
    val vaccineName: String,
    val targetDisease: String,
    val scheduledDate: Long,
    val status: String = "Pending", // Pending, Completed, Overdue
    val completedDate: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "disease_alerts")
data class DiseaseAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val diseaseName: String,
    val riskLevel: String, // HIGH, MEDIUM, LOW
    val affectedSpecies: String,
    val distanceKm: Int,
    val description: String,
    val preventiveAction: String,
    val dateReported: String
)
