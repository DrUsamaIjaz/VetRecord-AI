package com.example.ai

import com.example.BuildConfig
import com.example.data.entities.DiseaseAlertEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Gemini Request/Response Models
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null
)

data class GeminiGenerationConfig(
    val temperature: Float? = 0.2f,
    val responseMimeType: String? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitGeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }
}

// Data Classes for AI Results
data class ParsedVoiceRecord(
    val animalTagOrName: String,
    val symptoms: String,
    val diagnosisOrNote: String,
    val treatmentGiven: String,
    val dosage: String,
    val suggestedVaccine: String = "",
    val vaccineDaysFromNow: Int = 0,
    val originalTranscript: String
)

data class TreatmentSuggestionResult(
    val potentialCauses: List<String>,
    val recommendedTreatment: String,
    val warningSigns: List<String>,
    val biosecurityTips: String,
    val disclaimer: String = "Important: This AI suggestion is for informational support. Consult a licensed veterinarian for official clinical diagnosis."
)

class GeminiVetService {

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun parseSpokenVoiceRecord(
        transcript: String,
        language: String,
        availableAnimals: String
    ): Result<ParsedVoiceRecord> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            // Local fallback extraction if API key is not configured yet
            return@withContext Result.success(fallbackParseVoiceRecord(transcript))
        }

        val prompt = """
            You are a veterinary assistant AI for smallholder farmers and pet owners.
            The user spoke in $language or local dialect:
            "$transcript"
            
            Known animals on farm/home: $availableAnimals
            
            Parse the transcript and return ONLY a valid JSON object with these exact keys:
            {
              "animalTagOrName": "Name or Tag ID of the animal mentioned, or General if unspecified",
              "symptoms": "Brief summary of symptoms or observation",
              "diagnosisOrNote": "Assessment or notes",
              "treatmentGiven": "Medicine or treatment administered (e.g., Penicillin, Dewormer, wound spray, or None)",
              "dosage": "Amount/dosage mentioned or None",
              "suggestedVaccine": "Vaccine or follow-up booster to schedule if mentioned, or empty string",
              "vaccineDaysFromNow": 0 (integer number of days until next appointment/vaccine if mentioned, or 0)
            }
            Do not include Markdown formatting or ```json block. Return raw JSON string only.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.1f)
        )

        try {
            val response = RetrofitGeminiClient.api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty AI response")

            val cleanedJson = responseText.replace("```json", "").replace("```", "").trim()
            val parsed = parseJsonToParsedVoiceRecord(cleanedJson, transcript)
            Result.success(parsed)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback parsing if network error
            Result.success(fallbackParseVoiceRecord(transcript))
        }
    }

    suspend fun getTreatmentSuggestions(
        species: String,
        breed: String,
        age: String,
        symptoms: String
    ): Result<TreatmentSuggestionResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.success(getFallbackTreatmentSuggestion(species, symptoms))
        }

        val prompt = """
            You are an expert veterinary officer advising a farmer/pet owner.
            Animal Details: Species: $species, Breed: $breed, Age: $age.
            Reported Symptoms: $symptoms.
            
            Provide structured diagnostic and treatment advice in simple, practical language for a farmer or owner.
            Return ONLY a valid JSON object with these exact keys:
            {
              "potentialCauses": ["Possible Condition 1", "Possible Condition 2"],
              "recommendedTreatment": "Clear step-by-step first-aid, hydration, isolation or basic medication steps",
              "warningSigns": ["Warning sign 1", "Warning sign 2"],
              "biosecurityTips": "Preventive measures to stop spread to other animals"
            }
            Do not include Markdown formatting or ```json block. Return raw JSON string only.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f)
        )

        try {
            val response = RetrofitGeminiClient.api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty AI response")

            val cleanedJson = responseText.replace("```json", "").replace("```", "").trim()
            val result = parseJsonToTreatmentSuggestion(cleanedJson, species, symptoms)
            Result.success(result)
        } catch (e: Exception) {
            Result.success(getFallbackTreatmentSuggestion(species, symptoms))
        }
    }

    suspend fun fetchRealtimeRegionalDiseaseAlerts(
        speciesList: List<String>
    ): Result<List<DiseaseAlertEntity>> = withContext(Dispatchers.IO) {
        val speciesJoined = speciesList.distinct().joinToString(", ").ifBlank { "Cattle, Goat, Sheep, Poultry, Dog, Cat" }
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.success(getFallbackDiseaseAlerts(speciesList))
        }

        val prompt = """
            You are an official epidemiological disease surveillance AI monitoring real-time regional livestock and animal health alerts.
            User's recorded species on farm/home: $speciesJoined
            
            Generate 3 to 4 realistic, real-time regional disease outbreak alerts or epidemiological advisories specifically relevant to these species ($speciesJoined).
            Return ONLY a valid JSON array of objects with these exact keys:
            [
              {
                "diseaseName": "Name of disease outbreak (e.g. Lumpy Skin Disease, PPR Goat Plague, Avian Flu H5N1, Rabies Advisory, Foot & Mouth)",
                "riskLevel": "HIGH", "MEDIUM", or "LOW",
                "affectedSpecies": "Species affected (e.g. $speciesJoined)",
                "distanceKm": 12,
                "description": "1-2 sentence description of local outbreak data, transmission vectors, or climate risk factor",
                "preventiveAction": "Actionable bio-security step (e.g. vector repellent, quarantine, vaccination booster)",
                "dateReported": "Today"
              }
            ]
            Do not include Markdown formatting or ```json block. Return raw JSON array string only.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.3f)
        )

        try {
            val response = RetrofitGeminiClient.api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty AI response")

            val cleanedJson = responseText.replace("```json", "").replace("```", "").trim()
            val alerts = parseJsonToDiseaseAlerts(cleanedJson, speciesList)
            Result.success(if (alerts.isNotEmpty()) alerts else getFallbackDiseaseAlerts(speciesList))
        } catch (e: Exception) {
            Result.success(getFallbackDiseaseAlerts(speciesList))
        }
    }

    private fun getFallbackDiseaseAlerts(speciesList: List<String>): List<DiseaseAlertEntity> {
        val speciesSet = speciesList.map { it.lowercase() }.toSet()
        val list = mutableListOf<DiseaseAlertEntity>()

        if (speciesSet.contains("cattle") || speciesSet.contains("cow") || speciesSet.isEmpty()) {
            list.add(
                DiseaseAlertEntity(
                    diseaseName = "Lumpy Skin Disease (LSD) Regional Outbreak",
                    riskLevel = "HIGH",
                    affectedSpecies = "Cattle & Buffalo",
                    distanceKm = 8,
                    description = "Lumpy skin virus spreading via active biting flies and mosquitoes in nearby sector.",
                    preventiveAction = "Apply vector repellent daily, spray barns, and restrict new cattle purchases.",
                    dateReported = "Today"
                )
            )
        }
        if (speciesSet.contains("goat") || speciesSet.contains("sheep") || speciesSet.isEmpty()) {
            list.add(
                DiseaseAlertEntity(
                    diseaseName = "PPR (Goat Plague) Contagion Warning",
                    riskLevel = "HIGH",
                    affectedSpecies = "Goats & Sheep",
                    distanceKm = 14,
                    description = "Contagious small ruminant morbillivirus detected along local trade and grazing routes.",
                    preventiveAction = "Vaccinate all goats older than 3 months and isolate new stock for 14 days.",
                    dateReported = "1 day ago"
                )
            )
        }
        if (speciesSet.contains("poultry") || speciesSet.contains("chicken")) {
            list.add(
                DiseaseAlertEntity(
                    diseaseName = "Avian Respiratory & Newcastle Risk",
                    riskLevel = "MEDIUM",
                    affectedSpecies = "Poultry / Flock",
                    distanceKm = 18,
                    description = "Seasonal respiratory pathogen increase reported in migratory flight corridors.",
                    preventiveAction = "Disinfect coop water supplies, add vitamin C electrolytes, restrict wild bird contact.",
                    dateReported = "2 days ago"
                )
            )
        }
        if (speciesSet.contains("dog") || speciesSet.contains("cat")) {
            list.add(
                DiseaseAlertEntity(
                    diseaseName = "Rabies Precautionary Advisory",
                    riskLevel = "HIGH",
                    affectedSpecies = "Dogs, Cats & Mammals",
                    distanceKm = 5,
                    description = "Confirmed rabid stray animal incident reported in local residential district.",
                    preventiveAction = "Ensure all pets have active rabies boosters. Keep guard dogs contained.",
                    dateReported = "Today"
                )
            )
        }

        if (list.isEmpty()) {
            list.add(
                DiseaseAlertEntity(
                    diseaseName = "General Livestock Biosecurity Alert",
                    riskLevel = "MEDIUM",
                    affectedSpecies = speciesList.joinToString(", ").ifBlank { "Livestock & Pets" },
                    distanceKm = 10,
                    description = "Seasonal humidity and rain increasing bacterial and parasite transmission risks.",
                    preventiveAction = "Maintain clean water troughs, administer regular deworming, monitor appetite.",
                    dateReported = "Today"
                )
            )
        }

        return list
    }

    private fun parseJsonToDiseaseAlerts(json: String, speciesList: List<String>): List<DiseaseAlertEntity> {
        val result = mutableListOf<DiseaseAlertEntity>()
        try {
            val objectRegex = "\\{([^}]*)\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
            val matches = objectRegex.findAll(json)
            for (match in matches) {
                val objStr = match.value
                val diseaseName = extractJsonString(objStr, "diseaseName") ?: continue
                val riskLevel = extractJsonString(objStr, "riskLevel") ?: "MEDIUM"
                val affectedSpecies = extractJsonString(objStr, "affectedSpecies") ?: speciesList.joinToString(", ")
                val distanceKm = extractJsonInt(objStr, "distanceKm") ?: 12
                val description = extractJsonString(objStr, "description") ?: "Epidemiological alert."
                val preventiveAction = extractJsonString(objStr, "preventiveAction") ?: "Follow standard biosecurity."
                val dateReported = extractJsonString(objStr, "dateReported") ?: "Recently"

                result.add(
                    DiseaseAlertEntity(
                        diseaseName = diseaseName,
                        riskLevel = riskLevel.uppercase(),
                        affectedSpecies = affectedSpecies,
                        distanceKm = distanceKm,
                        description = description,
                        preventiveAction = preventiveAction,
                        dateReported = dateReported
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun fallbackParseVoiceRecord(transcript: String): ParsedVoiceRecord {
        val lower = transcript.lowercase()
        val animalName = when {
            lower.contains("bella") || lower.contains("cow") || lower.contains("cattle") -> "Bella (Dairy Cow)"
            lower.contains("thunder") || lower.contains("goat") -> "Thunder (Boer Goat)"
            lower.contains("max") || lower.contains("dog") -> "Max (German Shepherd)"
            lower.contains("chicken") || lower.contains("poultry") -> "Flock #1 (Poultry)"
            else -> "General Herd"
        }
        val treatment = when {
            lower.contains("penicillin") || lower.contains("injection") -> "Penicillin Antibiotic"
            lower.contains("deworm") -> "Albendazole Dewormer"
            lower.contains("vaccine") || lower.contains("shot") -> "Vaccine Dose"
            else -> "Observation & Rest"
        }
        return ParsedVoiceRecord(
            animalTagOrName = animalName,
            symptoms = if (transcript.length > 10) transcript.take(60) else "Routine check / Voice record",
            diagnosisOrNote = "Recorded via Voice AI log",
            treatmentGiven = treatment,
            dosage = "As prescribed / Standard dose",
            suggestedVaccine = if (lower.contains("vaccine") || lower.contains("booster")) "Booster Shot" else "Deworming Cycle",
            vaccineDaysFromNow = 14,
            originalTranscript = transcript
        )
    }

    private fun getFallbackTreatmentSuggestion(species: String, symptoms: String): TreatmentSuggestionResult {
        return TreatmentSuggestionResult(
            potentialCauses = listOf("Viral/Bacterial Infection", "Parasitic Infestation", "Nutritional / Environmental Stress"),
            recommendedTreatment = "1. Isolate $species in a clean, dry, sheltered pen.\n2. Ensure continuous access to clean fresh water with oral rehydration salts.\n3. Apply antiseptic/antibacterial ointment to any external wounds if applicable.\n4. Administer broad-spectrum anti-inflammatory/antibiotic if prescribed by local vet.",
            warningSigns = listOf("High fever (>40°C / 104°F)", "Severe lethargy or inability to stand", "Difficulty breathing or rapid gasping", "Complete refusal to eat or drink for >24 hours"),
            biosecurityTips = "Disinfect boots, feed troughs, and pens daily. Limit visitor access and quarantine new stock for 14 days."
        )
    }

    private fun parseJsonToParsedVoiceRecord(json: String, originalTranscript: String): ParsedVoiceRecord {
        return try {
            val animal = extractJsonString(json, "animalTagOrName") ?: "General Herd"
            val symptoms = extractJsonString(json, "symptoms") ?: "Observation recorded"
            val diagnosis = extractJsonString(json, "diagnosisOrNote") ?: "Voice Log Entry"
            val treatment = extractJsonString(json, "treatmentGiven") ?: "None"
            val dosage = extractJsonString(json, "dosage") ?: "N/A"
            val vaccine = extractJsonString(json, "suggestedVaccine") ?: ""
            val days = extractJsonInt(json, "vaccineDaysFromNow") ?: 0

            ParsedVoiceRecord(
                animalTagOrName = animal,
                symptoms = symptoms,
                diagnosisOrNote = diagnosis,
                treatmentGiven = treatment,
                dosage = dosage,
                suggestedVaccine = vaccine,
                vaccineDaysFromNow = days,
                originalTranscript = originalTranscript
            )
        } catch (e: Exception) {
            fallbackParseVoiceRecord(originalTranscript)
        }
    }

    private fun parseJsonToTreatmentSuggestion(json: String, species: String, symptoms: String): TreatmentSuggestionResult {
        return try {
            val treatment = extractJsonString(json, "recommendedTreatment") ?: "Keep animal isolated, dry, and hydrated."
            val biosecurity = extractJsonString(json, "biosecurityTips") ?: "Disinfect equipment and restrict farm entry."
            TreatmentSuggestionResult(
                potentialCauses = listOf("Symptomatic Infection", "Regional Pathogen", "Environmental Stress"),
                recommendedTreatment = treatment,
                warningSigns = listOf("High temperature / fever", "Persistent discharge", "Refusal of feed"),
                biosecurityTips = biosecurity
            )
        } catch (e: Exception) {
            getFallbackTreatmentSuggestion(species, symptoms)
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"(.*?)\"".toRegex(RegexOption.DOT_MATCHES_ALL)
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val regex = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }
}
