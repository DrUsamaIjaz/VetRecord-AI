package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entities.AnimalEntity
import com.example.ui.components.AnimalForm

@Composable
fun AddAnimalDialog(
    onDismiss: () -> Unit,
    onSave: (AnimalEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimalForm(
            onSave = { animal ->
                onSave(animal)
            },
            onCancel = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}
