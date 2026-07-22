package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.entities.AnimalEntity
import com.example.data.entities.VaccinationEntity
import com.example.ui.VetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationScreen(
    viewModel: VetViewModel,
    modifier: Modifier = Modifier
) {
    val vaccinations by viewModel.vaccinations.collectAsState()
    val allAnimals by viewModel.allRawAnimals.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending/Overdue, 1: Completed
    var showAddDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val pendingList = vaccinations.filter { it.status == "Pending" || it.status == "Overdue" }
    val completedList = vaccinations.filter { it.status == "Completed" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vaccination & Deworming Schedule 💉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("add_vaccine_fab")
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Schedule Vaccine",
                    modifier = Modifier.size(28.dp)
                )
            }
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
                    text = { Text("Pending / Overdue (${pendingList.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed (${completedList.size})", fontWeight = FontWeight.Bold) }
                )
            }

            val currentDisplayList = if (selectedTab == 0) pendingList else completedList

            if (currentDisplayList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "No pending vaccinations!" else "No completed records yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap '+' to schedule a new immunization or deworming reminder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentDisplayList) { vacc ->
                        VaccinationCardItem(
                            vaccination = vacc,
                            dateFormat = dateFormat,
                            onMarkDone = { viewModel.markVaccinationCompleted(vacc.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ScheduleVaccinationDialog(
            allAnimals = allAnimals,
            onDismiss = { showAddDialog = false },
            onSave = { animal, vaccineName, disease, daysFromNow, notes ->
                val daysMs = daysFromNow * 86400000L
                viewModel.addManualVaccination(
                    VaccinationEntity(
                        animalId = animal.id,
                        animalTagOrName = animal.tagOrName,
                        vaccineName = vaccineName,
                        targetDisease = disease,
                        scheduledDate = System.currentTimeMillis() + daysMs,
                        status = "Pending",
                        notes = notes
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleVaccinationDialog(
    allAnimals: List<AnimalEntity>,
    onDismiss: () -> Unit,
    onSave: (animal: AnimalEntity, vaccineName: String, disease: String, daysFromNow: Int, notes: String) -> Unit
) {
    var selectedAnimal by remember { mutableStateOf(allAnimals.firstOrNull()) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var vaccineName by remember { mutableStateOf("") }
    var targetDisease by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("14") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Schedule Vaccination / Deworming",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Animal Selector
                if (allAnimals.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedAnimal?.tagOrName ?: "Select Animal",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Animal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            allAnimals.forEach { animal ->
                                DropdownMenuItem(
                                    text = { Text("${animal.tagOrName} (${animal.species})") },
                                    onClick = {
                                        selectedAnimal = animal
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine / Treatment Name") },
                    placeholder = { Text("e.g. Rabies Shot, Foot & Mouth, Dewormer") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetDisease,
                    onValueChange = { targetDisease = it },
                    label = { Text("Target Disease / Preventive") },
                    placeholder = { Text("e.g. Rabies Virus, Internal Worms") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Days from today for Reminder") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedAnimal != null && vaccineName.isNotBlank()) {
                                val days = daysText.toIntOrNull() ?: 14
                                onSave(selectedAnimal!!, vaccineName, targetDisease, days, notes)
                            }
                        },
                        enabled = selectedAnimal != null && vaccineName.isNotBlank()
                    ) {
                        Text("Save Schedule")
                    }
                }
            }
        }
    }
}
