package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiVetService
import com.example.ai.ParsedVoiceRecord
import com.example.ai.TreatmentSuggestionResult
import com.example.data.db.VetDatabase
import com.example.data.entities.AnimalEntity
import com.example.data.entities.DiseaseAlertEntity
import com.example.data.entities.HealthLogEntity
import com.example.data.entities.VaccinationEntity
import com.example.data.repository.VetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class VetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VetRepository
    private val geminiService = GeminiVetService()

    // Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All") // All, Livestock, Pet
    val selectedSpeciesFilter = MutableStateFlow("All") // All, Cattle, Goat, Sheep, Poultry, Dog, Cat

    // Speech Recognition state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Swahili") // Swahili, English, Hindi, Spanish, Hausa, French
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Voice Parsing AI state
    private val _isParsingVoice = MutableStateFlow(false)
    val isParsingVoice: StateFlow<Boolean> = _isParsingVoice.asStateFlow()

    private val _lastParsedRecord = MutableStateFlow<ParsedVoiceRecord?>(null)
    val lastParsedRecord: StateFlow<ParsedVoiceRecord?> = _lastParsedRecord.asStateFlow()

    // AI Symptom Checker state
    private val _aiTreatmentResult = MutableStateFlow<TreatmentSuggestionResult?>(null)
    val aiTreatmentResult: StateFlow<TreatmentSuggestionResult?> = _aiTreatmentResult.asStateFlow()

    private val _isLoadingAiTreatment = MutableStateFlow(false)
    val isLoadingAiTreatment: StateFlow<Boolean> = _isLoadingAiTreatment.asStateFlow()

    // Real-Time Regional Disease Alerts state
    private val _isRefreshingAlerts = MutableStateFlow(false)
    val isRefreshingAlerts: StateFlow<Boolean> = _isRefreshingAlerts.asStateFlow()

    // Toast / Message Notice
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        val database = VetDatabase.getDatabase(application)
        repository = VetRepository(database.vetDao())

        // Check if database needs initial seeding
        viewModelScope.launch {
            repository.allAnimals.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultData()
                }
            }
        }

        initSpeechRecognizer(application)
    }

    private fun initSpeechRecognizer(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { _isListening.value = false }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        _toastMessage.value = "Voice recognition error ($error). You can use sample prompt or type transcript."
                    }
                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _spokenText.value = matches[0]
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setSpokenText(text: String) {
        _spokenText.value = text
    }

    fun startVoiceListening(context: Context) {
        if (speechRecognizer == null) {
            initSpeechRecognizer(context)
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocaleForLanguage(_selectedLanguage.value))
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _toastMessage.value = "Speech recognizer not supported on this container. Select a sample prompt below!"
            _isListening.value = false
        }
    }

    fun stopVoiceListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    private fun getLocaleForLanguage(lang: String): String {
        return when (lang) {
            "Swahili" -> "sw-KE"
            "Hindi" -> "hi-IN"
            "Spanish" -> "es-ES"
            "Hausa" -> "ha-NG"
            "French" -> "fr-FR"
            "Vietnamese" -> "vi-VN"
            else -> "en-US"
        }
    }

    // Process spoken text via Gemini AI
    fun processSpokenTextWithAi(transcript: String, availableAnimals: List<AnimalEntity>) {
        if (transcript.isBlank()) return
        viewModelScope.launch {
            _isParsingVoice.value = true
            val animalNames = availableAnimals.joinToString { "${it.tagOrName} (${it.species})" }
            val result = geminiService.parseSpokenVoiceRecord(transcript, _selectedLanguage.value, animalNames)
            _isParsingVoice.value = false
            result.onSuccess { parsed ->
                _lastParsedRecord.value = parsed
            }.onFailure {
                _toastMessage.value = "Failed to parse voice log with AI: ${it.message}"
            }
        }
    }

    fun saveParsedVoiceRecordToDatabase(parsed: ParsedVoiceRecord, allAnimals: List<AnimalEntity>) {
        viewModelScope.launch {
            // Find matching animal or default
            val targetAnimal = allAnimals.firstOrNull {
                it.tagOrName.contains(parsed.animalTagOrName, ignoreCase = true) ||
                        parsed.animalTagOrName.contains(it.tagOrName, ignoreCase = true)
            } ?: allAnimals.firstOrNull()

            val animalId = targetAnimal?.id ?: 1
            val animalTag = targetAnimal?.tagOrName ?: parsed.animalTagOrName

            // 1. Insert Health Log
            val log = HealthLogEntity(
                animalId = animalId,
                animalTagOrName = animalTag,
                symptoms = parsed.symptoms,
                diagnosisOrNote = parsed.diagnosisOrNote,
                treatmentGiven = parsed.treatmentGiven,
                dosage = parsed.dosage,
                source = "Voice Input AI"
            )
            repository.insertHealthLog(log)

            // 2. Schedule Vaccination/Deworming if present
            if (parsed.suggestedVaccine.isNotBlank() && parsed.suggestedVaccine != "None") {
                val daysInMs = if (parsed.vaccineDaysFromNow > 0) parsed.vaccineDaysFromNow * 86400000L else 14 * 86400000L
                val vacc = VaccinationEntity(
                    animalId = animalId,
                    animalTagOrName = animalTag,
                    vaccineName = parsed.suggestedVaccine,
                    targetDisease = "Preventive Health / Booster",
                    scheduledDate = System.currentTimeMillis() + daysInMs,
                    status = "Pending",
                    notes = "Scheduled automatically via Voice AI input"
                )
                repository.insertVaccination(vacc)
            }

            // 3. Update Animal Health status if under treatment
            if (targetAnimal != null && parsed.treatmentGiven.isNotBlank() && parsed.treatmentGiven != "None") {
                repository.updateAnimal(targetAnimal.copy(healthStatus = "Under Treatment"))
            }

            _toastMessage.value = "Health log & reminder saved for $animalTag!"
            _lastParsedRecord.value = null
            _spokenText.value = ""
        }
    }

    // AI Symptom Checker
    fun requestAiTreatmentSuggestions(species: String, breed: String, age: String, symptoms: String) {
        if (symptoms.isBlank()) return
        viewModelScope.launch {
            _isLoadingAiTreatment.value = true
            val result = geminiService.getTreatmentSuggestions(species, breed, age, symptoms)
            _isLoadingAiTreatment.value = false
            result.onSuccess {
                _aiTreatmentResult.value = it
            }.onFailure {
                _toastMessage.value = "AI diagnosis error: ${it.message}"
            }
        }
    }

    fun clearAiTreatmentResult() {
        _aiTreatmentResult.value = null
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // Animal management
    fun addNewAnimal(animal: AnimalEntity) {
        viewModelScope.launch {
            val newId = repository.insertAnimal(animal)
            _toastMessage.value = "${animal.tagOrName} added successfully!"

            // Auto-generate vaccination schedule
            val initialVaccine = VaccinationEntity(
                animalId = newId.toInt(),
                animalTagOrName = animal.tagOrName,
                vaccineName = if (animal.category == "Pet") "Rabies & Combination Shot" else "Foot & Mouth / Deworming",
                targetDisease = if (animal.category == "Pet") "Rabies & Parvovirus" else "FMD & Internal Parasites",
                scheduledDate = System.currentTimeMillis() + (30 * 86400000L),
                status = "Pending",
                notes = "Initial routine vaccination reminder"
            )
            repository.insertVaccination(initialVaccine)
        }
    }

    fun updateAnimalStatus(animal: AnimalEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateAnimal(animal.copy(healthStatus = newStatus))
            _toastMessage.value = "Status updated to $newStatus"
        }
    }

    fun markVaccinationCompleted(vaccinationId: Int) {
        viewModelScope.launch {
            repository.markVaccinationCompleted(vaccinationId)
            _toastMessage.value = "Vaccination marked as Completed!"
        }
    }

    fun addManualHealthLog(log: HealthLogEntity) {
        viewModelScope.launch {
            repository.insertHealthLog(log)
            _toastMessage.value = "Health log added!"
        }
    }

    fun addManualVaccination(vaccination: VaccinationEntity) {
        viewModelScope.launch {
            repository.insertVaccination(vaccination)
            _toastMessage.value = "Vaccination reminder scheduled!"
        }
    }

    // Fetch real-time regional disease alerts based on species in user's records
    fun refreshDiseaseAlertsFromApi() {
        viewModelScope.launch {
            _isRefreshingAlerts.value = true
            val registeredSpecies = allRawAnimals.value
                .map { it.species }
                .distinct()
                .filter { it.isNotBlank() }

            val speciesToQuery = if (registeredSpecies.isEmpty()) listOf("Cattle", "Goat", "Poultry") else registeredSpecies
            val result = geminiService.fetchRealtimeRegionalDiseaseAlerts(speciesToQuery)
            _isRefreshingAlerts.value = false

            result.onSuccess { alerts ->
                if (alerts.isNotEmpty()) {
                    repository.seedDiseaseAlerts(alerts)
                    _toastMessage.value = "Updated real-time alerts for: ${speciesToQuery.joinToString(", ")}"
                }
            }.onFailure {
                _toastMessage.value = "Unable to fetch regional alerts: ${it.message}"
            }
        }
    }

    fun getHealthLogsForAnimal(animalId: Int): Flow<List<HealthLogEntity>> =
        repository.getHealthLogsForAnimal(animalId)

    fun getVaccinationsForAnimal(animalId: Int): Flow<List<VaccinationEntity>> =
        repository.getVaccinationsForAnimal(animalId)

    // Master Flow of Animals
    val animals: StateFlow<List<AnimalEntity>> = combine(
        repository.allAnimals,
        searchQuery,
        selectedCategoryFilter,
        selectedSpeciesFilter
    ) { list, query, category, species ->
        list.filter { animal ->
            val matchesQuery = query.isBlank() ||
                    animal.tagOrName.contains(query, ignoreCase = true) ||
                    animal.breed.contains(query, ignoreCase = true) ||
                    animal.species.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || animal.category == category
            val matchesSpecies = species == "All" || animal.species == species

            matchesQuery && matchesCategory && matchesSpecies
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRawAnimals: StateFlow<List<AnimalEntity>> = repository.allAnimals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val healthLogs: StateFlow<List<HealthLogEntity>> = repository.allHealthLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val vaccinations: StateFlow<List<VaccinationEntity>> = repository.allVaccinations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val diseaseAlerts: StateFlow<List<DiseaseAlertEntity>> = repository.allDiseaseAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private suspend fun seedDefaultData() {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // 1. Animals
        val bella = AnimalEntity(
            tagOrName = "Bella (#C-104)",
            species = "Cattle",
            breed = "Holstein Friesian",
            category = "Livestock",
            gender = "Female",
            ageYears = 3.5,
            location = "Barn A",
            healthStatus = "Healthy",
            notes = "High yield dairy cow. Average 22L/day."
        )
        val thunder = AnimalEntity(
            tagOrName = "Thunder (#G-202)",
            species = "Goat",
            breed = "Boer Goat",
            category = "Livestock",
            gender = "Male",
            ageYears = 1.8,
            location = "Pasture B",
            healthStatus = "Vaccine Due",
            notes = "Breeding buck. Healthy appetite."
        )
        val maxDog = AnimalEntity(
            tagOrName = "Max",
            species = "Dog",
            breed = "German Shepherd",
            category = "Pet",
            gender = "Male",
            ageYears = 2.0,
            location = "Farmhouse / Yard",
            healthStatus = "Healthy",
            notes = "Farm guard dog. Fully house trained."
        )
        val flock1 = AnimalEntity(
            tagOrName = "Flock #1 (#P-50)",
            species = "Poultry",
            breed = "Rhode Island Red",
            category = "Livestock",
            gender = "Female",
            ageYears = 0.8,
            location = "Coop 1",
            healthStatus = "Under Treatment",
            notes = "50 laying hens. Egg production steady."
        )
        val lunaCat = AnimalEntity(
            tagOrName = "Luna",
            species = "Cat",
            breed = "Domestic Short Hair",
            category = "Pet",
            gender = "Female",
            ageYears = 1.2,
            location = "Main Residence",
            healthStatus = "Healthy",
            notes = "Friendly barn cat. Keeps rodents away."
        )

        val id1 = repository.insertAnimal(bella).toInt()
        val id2 = repository.insertAnimal(thunder).toInt()
        val id3 = repository.insertAnimal(maxDog).toInt()
        val id4 = repository.insertAnimal(flock1).toInt()
        val id5 = repository.insertAnimal(lunaCat).toInt()

        // 2. Initial Health Logs
        repository.insertHealthLog(
            HealthLogEntity(
                animalId = id1,
                animalTagOrName = bella.tagOrName,
                logDate = now - (3 * dayMs),
                symptoms = "Mild lethargy after calving",
                diagnosisOrNote = "Post-calving fatigue check. Calcium booster administered.",
                treatmentGiven = "Calcium Gluconate Oral Drench",
                dosage = "500ml",
                administeredBy = "Farmer John",
                source = "Voice Input AI"
            )
        )
        repository.insertHealthLog(
            HealthLogEntity(
                animalId = id4,
                animalTagOrName = flock1.tagOrName,
                logDate = now - (1 * dayMs),
                symptoms = "Slight sneezing in 3 hens",
                diagnosisOrNote = "Mild respiratory distress. Vitamin C & electrolyte added to water.",
                treatmentGiven = "Electrolyte & Antibacterial Water Treatment",
                dosage = "5g / 10L water",
                administeredBy = "Farmer John",
                source = "Manual"
            )
        )

        // 3. Initial Vaccinations
        repository.insertVaccination(
            VaccinationEntity(
                animalId = id1,
                animalTagOrName = bella.tagOrName,
                vaccineName = "Foot & Mouth Disease (FMD) Booster",
                targetDisease = "Foot & Mouth Disease Virus",
                scheduledDate = now + (5 * dayMs),
                status = "Pending",
                notes = "Annual booster protocol"
            )
        )
        repository.insertVaccination(
            VaccinationEntity(
                animalId = id2,
                animalTagOrName = thunder.tagOrName,
                vaccineName = "PPR Goat Plague Vaccine",
                targetDisease = "Peste des Petits Ruminants",
                scheduledDate = now - (2 * dayMs),
                status = "Overdue",
                notes = "Urgent: High risk in rainy season"
            )
        )
        repository.insertVaccination(
            VaccinationEntity(
                animalId = id3,
                animalTagOrName = maxDog.tagOrName,
                vaccineName = "Rabies Annual Shot",
                targetDisease = "Rabies Virus",
                scheduledDate = now + (14 * dayMs),
                status = "Pending",
                notes = "Mandatory pet vaccination"
            )
        )
        repository.insertVaccination(
            VaccinationEntity(
                animalId = id4,
                animalTagOrName = flock1.tagOrName,
                vaccineName = "Newcastle Disease Water Strain",
                targetDisease = "Newcastle Disease",
                scheduledDate = now - (20 * dayMs),
                status = "Completed",
                completedDate = now - (20 * dayMs),
                notes = "Administered via drinking water"
            )
        )

        // 4. Initial Disease Risk Alerts
        val alerts = listOf(
            DiseaseAlertEntity(
                diseaseName = "Lumpy Skin Disease Outbreak",
                riskLevel = "HIGH",
                affectedSpecies = "Cattle & Buffalo",
                distanceKm = 12,
                description = "Lumpy skin virus reported in neighboring sector. Biting flies and mosquitoes are active vectors.",
                preventiveAction = "Apply fly repellent to cattle, spray barns with insecticide, restrict incoming cattle purchases.",
                dateReported = "2 days ago"
            ),
            DiseaseAlertEntity(
                diseaseName = "PPR (Goat Plague) Alert",
                riskLevel = "MEDIUM",
                affectedSpecies = "Goats & Sheep",
                distanceKm = 24,
                description = "Seasonal small ruminant virus spreading along livestock trade routes.",
                preventiveAction = "Vaccinate all goats older than 3 months. Quarantine any new goats for 14 days.",
                dateReported = "4 days ago"
            ),
            DiseaseAlertEntity(
                diseaseName = "Rabies Precautionary Advisory",
                riskLevel = "HIGH",
                affectedSpecies = "Dogs, Cats, Livestock",
                distanceKm = 5,
                description = "Stray canine rabies case confirmed in district 3.",
                preventiveAction = "Ensure guard dogs and house pets have up-to-date rabies shots. Avoid contact with unknown stray dogs.",
                dateReported = "1 day ago"
            )
        )
        repository.seedDiseaseAlerts(alerts)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
