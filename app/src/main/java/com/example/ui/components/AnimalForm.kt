package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.AnimalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnimalForm(
    initialAnimal: AnimalEntity? = null,
    onSave: (AnimalEntity) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var tagOrName by remember { mutableStateOf(initialAnimal?.tagOrName ?: "") }
    var species by remember { mutableStateOf(initialAnimal?.species ?: "Cattle") }
    var breed by remember { mutableStateOf(initialAnimal?.breed ?: "") }
    var category by remember { mutableStateOf(initialAnimal?.category ?: "Livestock") }
    var gender by remember { mutableStateOf(initialAnimal?.gender ?: "Female") }
    var birthDate by remember {
        mutableStateOf(
            initialAnimal?.birthDate?.ifBlank { dateFormatter.format(Date()) }
                ?: dateFormatter.format(Date())
        )
    }
    var ageText by remember {
        mutableStateOf(
            initialAnimal?.ageYears?.toString() ?: "1.0"
        )
    }
    var location by remember { mutableStateOf(initialAnimal?.location ?: "Barn A") }
    var healthStatus by remember { mutableStateOf(initialAnimal?.healthStatus ?: "Healthy") }
    var notes by remember { mutableStateOf(initialAnimal?.notes ?: "") }

    var showErrorTag by remember { mutableStateOf(false) }
    var expandedSpeciesDropdown by remember { mutableStateOf(false) }

    val speciesOptions = listOf("Cattle", "Goat", "Sheep", "Poultry", "Dog", "Cat", "Pig", "Horse", "Other")
    val healthStatusOptions = listOf("Healthy", "Under Treatment", "Vaccine Due", "Sick")

    // DatePicker Dialog helper
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                birthDate = dateFormatter.format(selectedCal.time)

                // Auto calculate age in years
                val now = Calendar.getInstance()
                val diffInMillis = now.timeInMillis - selectedCal.timeInMillis
                if (diffInMillis > 0) {
                    val ageInYears = diffInMillis.toDouble() / (1000L * 60 * 60 * 24 * 365)
                    ageText = String.format(Locale.US, "%.1f", ageInYears)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Animal Form",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = if (initialAnimal == null) "Add New Animal Record" else "Edit Animal Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter basic info & birth date to keep digital health records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Tag ID or Name
            OutlinedTextField(
                value = tagOrName,
                onValueChange = {
                    tagOrName = it
                    if (it.isNotBlank()) showErrorTag = false
                },
                label = { Text("Name or Tag ID *") },
                placeholder = { Text("e.g. Bella, Tag #C-102") },
                isError = showErrorTag,
                supportingText = {
                    if (showErrorTag) {
                        Text("Name or Tag ID is required", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Unique identifier for this animal")
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("animal_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Category & Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "Livestock",
                            onClick = { category = "Livestock" },
                            modifier = Modifier.testTag("category_livestock_radio")
                        )
                        Text("Livestock", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        RadioButton(
                            selected = category == "Pet",
                            onClick = { category = "Pet" },
                            modifier = Modifier.testTag("category_pet_radio")
                        )
                        Text("Pet", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Gender
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "Female",
                            onClick = { gender = "Female" },
                            modifier = Modifier.testTag("gender_female_radio")
                        )
                        Text("Female", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        RadioButton(
                            selected = gender == "Male",
                            onClick = { gender = "Male" },
                            modifier = Modifier.testTag("gender_male_radio")
                        )
                        Text("Male", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Species Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedSpeciesDropdown,
                onExpandedChange = { expandedSpeciesDropdown = !expandedSpeciesDropdown }
            ) {
                OutlinedTextField(
                    value = species,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Species *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpeciesDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("animal_species_input")
                )
                ExposedDropdownMenu(
                    expanded = expandedSpeciesDropdown,
                    onDismissRequest = { expandedSpeciesDropdown = false }
                ) {
                    speciesOptions.forEach { sp ->
                        DropdownMenuItem(
                            text = { Text(sp) },
                            onClick = {
                                species = sp
                                expandedSpeciesDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Breed / Variety
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed / Variety") },
                placeholder = { Text("e.g. Holstein Friesian, Boer, German Shepherd") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("animal_breed_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Birth Date Field with Picker & Presets
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Birth Date (YYYY-MM-DD)") },
                placeholder = { Text("2023-04-12") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick Birth Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                supportingText = {
                    Text("Tap calendar icon to select birth date")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("animal_birth_date_input")
            )

            // Quick Birth Date Presets
            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf(
                    "Today" to 0,
                    "6 Mos Ago" to 6,
                    "1 Yr Ago" to 12,
                    "2 Yrs Ago" to 24,
                    "3 Yrs Ago" to 36
                )
                presets.forEach { (label, months) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MONTH, -months)
                            birthDate = dateFormatter.format(cal.time)
                            ageText = String.format(Locale.US, "%.1f", months / 12.0)
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Age & Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Age (Years)") },
                    placeholder = { Text("1.5") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("animal_age_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Pen") },
                    placeholder = { Text("Barn A, Pasture 2") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("animal_location_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Initial Health Status Selection
            Text(
                text = "Initial Health Status",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                healthStatusOptions.forEach { status ->
                    val isSelected = healthStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { healthStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 8. Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes (Optional)") },
                placeholder = { Text("Vaccination history, markings, dietary needs...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("animal_notes_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("cancel_animal_form_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (tagOrName.isBlank()) {
                            showErrorTag = true
                        } else {
                            val parsedAge = ageText.toDoubleOrNull() ?: 1.0
                            val newAnimal = AnimalEntity(
                                id = initialAnimal?.id ?: 0,
                                tagOrName = tagOrName.trim(),
                                species = species,
                                breed = breed.ifBlank { "Standard" }.trim(),
                                category = category,
                                gender = gender,
                                birthDate = birthDate.trim(),
                                ageYears = parsedAge,
                                location = location.ifBlank { "Main Section" }.trim(),
                                healthStatus = healthStatus,
                                notes = notes.trim()
                            )
                            onSave(newAnimal)
                        }
                    },
                    modifier = Modifier.testTag("save_animal_form_button")
                ) {
                    Text(if (initialAnimal == null) "Save Animal" else "Update Animal")
                }
            }
        }
    }
}
