package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.DiseaseAlertEntity
import com.example.ui.VetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVetAssistantScreen(
    viewModel: VetViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Symptom Checker, 1: Outbreak Radar
    val diseaseAlerts by viewModel.diseaseAlerts.collectAsState()
    val aiTreatmentResult by viewModel.aiTreatmentResult.collectAsState()
    val isLoadingAiTreatment by viewModel.isLoadingAiTreatment.collectAsState()

    var species by remember { mutableStateOf("Cattle") }
    var breed by remember { mutableStateOf("Holstein Friesian") }
    var age by remember { mutableStateOf("3 years") }
    var symptomsInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Vet Assistant & Disease Radar 🛡️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Symptom Checker", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Symptom") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Disease Radar (${diseaseAlerts.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Coronavirus, contentDescription = "Radar") }
                )
            }

            if (selectedTab == 0) {
                // Symptom Checker & Treatment Suggestion View
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Treatment & Diagnostic Helper",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Describe animal symptoms to receive instant AI treatment suggestions, emergency warning signs, and biosecurity precautions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = species,
                                        onValueChange = { species = it },
                                        label = { Text("Species") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = age,
                                        onValueChange = { age = it },
                                        label = { Text("Age") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = breed,
                                    onValueChange = { breed = it },
                                    label = { Text("Breed / Type") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = symptomsInput,
                                    onValueChange = { symptomsInput = it },
                                    placeholder = { Text("e.g. High fever, nasal discharge, reduced milk yield, coughing...") },
                                    label = { Text("Observed Symptoms & Behavior") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("symptoms_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.requestAiTreatmentSuggestions(species, breed, age, symptomsInput)
                                    },
                                    enabled = symptomsInput.isNotBlank() && !isLoadingAiTreatment,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("check_symptoms_ai_button")
                                ) {
                                    if (isLoadingAiTreatment) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Analyzing with Gemini AI...")
                                    } else {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Analyze")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Get AI Treatment Suggestions ✨")
                                    }
                                }
                            }
                        }
                    }

                    // Display Result
                    if (aiTreatmentResult != null) {
                        item {
                            val res = aiTreatmentResult!!
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "AI Veterinary Assessment Result ✨",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Potential Causes
                                    Text(
                                        text = "Possible Conditions:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    res.potentialCauses.forEach { cause ->
                                        Text(
                                            text = "• $cause",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Recommended Treatment
                                    Text(
                                        text = "Recommended First-Aid & Basic Treatment:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = res.recommendedTreatment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Warning Signs
                                    Text(
                                        text = "⚠️ Emergency Warning Signs (Call Vet immediately if seen):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    res.warningSigns.forEach { sign ->
                                        Text(
                                            text = "🚨 $sign",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC62828)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Biosecurity
                                    Text(
                                        text = "Biosecurity & Herd Protection:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = res.biosecurityTips,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Surface(
                                        color = Color(0xFFFFF8E1),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = res.disclaimer,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFF57F17),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Outbreak Disease Radar List
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Regional Disease Outbreak Threats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    items(diseaseAlerts) { alert ->
                        DiseaseAlertCardItem(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseAlertCardItem(alert: DiseaseAlertEntity) {
    val (riskBg, riskText) = when (alert.riskLevel.uppercase()) {
        "HIGH" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "MEDIUM" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = riskText,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = alert.diseaseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = riskBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${alert.riskLevel} RISK (${alert.distanceKm}km away)",
                        color = riskText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Affected Species: ${alert.affectedSpecies}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Preventive Action:\n${alert.preventiveAction}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
