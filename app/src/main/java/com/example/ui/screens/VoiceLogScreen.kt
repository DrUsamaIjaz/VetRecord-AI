package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.VetViewModel
import com.example.ui.components.AnimatedVoiceWaveform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLogScreen(
    viewModel: VetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isParsingVoice by viewModel.isParsingVoice.collectAsState()
    val parsedRecord by viewModel.lastParsedRecord.collectAsState()
    val allAnimals by viewModel.allRawAnimals.collectAsState()

    var showLanguageDropdown by remember { mutableStateOf(false) }

    val languages = listOf("Urdu", "Punjabi", "Swahili", "English", "Hindi", "Spanish", "Hausa", "French", "Vietnamese")

    val samplePrompts = listOf(
        "Urdu" to "گائے بیلا کو ہلکا بخار ہے اور آج 10ml پینسلین دی گئی ہے، 14 دن بعد ڈیوورمر کا ٹیکہ لگانا ہے۔",
        "Punjabi" to "ਗਾਂ ਬੇਲਾ ਨੂੰ ਹਲਕਾ ਬੁਖਾਰ ਹੈ, ਅੱਜ 10ml ਪੈਨਸਿਲਿਨ ਦਿੱਤੀ ਹੈ, 14 ਦਿਨਾਂ ਬਾਅਦ ਕੀੜਿਆਂ ਦੀ ਦਵਾਈ ਦੇਣੀ ਹੈ।",
        "Swahili" to "Ng'ombe Bella ana homa na alipewa Penicillin 10ml leo, na deworming baada ya siku 14.",
        "English" to "Dairy cow Bella has a slight fever and was given 10ml Penicillin today, schedule dewormer in 14 days.",
        "Hindi" to "गाय बेला को आज 10ml पेिसिलिन दिया और 14 दिन में टीका लगाना है।",
        "Spanish" to "La vaca Bella tiene fiebre y se le dio 10ml de Penicilina hoy, programar desparasitación en 14 días.",
        "Hausa" to "Saniya Bella tana da zazzabi kuma an ba ta 10ml Penicillin a yau, dewormer a kwanaki 14.",
        "French" to "La vache Bella a de la fièvre et a reçu 10ml de Pénicilline aujourd'hui, vermifuge dans 14 jours."
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voice Record AI 🎙️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        Button(
                            onClick = { showLanguageDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedLanguage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLanguageDropdown = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Intro Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Speak Natural Local Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Simply speak what you observed or administered to your animals. Gemini AI will automatically convert spoken words into digital health logs and vaccination reminders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Big Microphone Button & Visualizer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Color(0xFFD32F2F)
                                else MaterialTheme.colorScheme.primary
                            )
                            .clickable {
                                if (isListening) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceListening(context)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("mic_record_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVoiceWaveform(isListening = isListening)

                    Text(
                        text = if (isListening) "Listening... Speak now ($selectedLanguage)"
                        else "Tap mic to speak or select a sample prompt below",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Sample Spoken Prompts Chips for Quick Testing
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Quick Sample Voice Inputs ($selectedLanguage)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(samplePrompts) { (lang, promptText) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.setLanguage(lang)
                                    viewModel.setSpokenText(promptText)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🗣️ $lang: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = promptText.take(35) + "...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Editable Spoken Transcript Text Field
            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Spoken Transcript",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = spokenText,
                        onValueChange = { viewModel.setSpokenText(it) },
                        placeholder = { Text("Your spoken health notes will appear here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("spoken_transcript_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.processSpokenTextWithAi(spokenText, allAnimals) },
                        enabled = spokenText.isNotBlank() && !isParsingVoice,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("process_ai_button")
                    ) {
                        if (isParsingVoice) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI Parsing Transcript...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parse Transcript with Gemini AI ✨")
                        }
                    }
                }
            }

            // Parsed AI Result Preview
            if (parsedRecord != null) {
                item {
                    val record = parsedRecord!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Extracted Health Record ✨",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Extracted",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ParsedRowItem("Animal", record.animalTagOrName)
                            ParsedRowItem("Symptoms Observed", record.symptoms)
                            ParsedRowItem("Treatment Administered", "${record.treatmentGiven} (${record.dosage})")
                            ParsedRowItem("Assessment / Notes", record.diagnosisOrNote)

                            if (record.suggestedVaccine.isNotBlank() && record.suggestedVaccine != "None") {
                                ParsedRowItem(
                                    "Follow-up / Vaccine",
                                    "${record.suggestedVaccine} (Scheduled in ${if (record.vaccineDaysFromNow > 0) record.vaccineDaysFromNow else 14} days)"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.saveParsedVoiceRecordToDatabase(record, allAnimals)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_parsed_record_button")
                            ) {
                                Text("Confirm & Save to Health Records")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParsedRowItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
