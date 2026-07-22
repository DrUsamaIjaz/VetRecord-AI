package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entities.AnimalEntity
import com.example.ui.VetViewModel
import com.example.ui.components.NavItem
import com.example.ui.components.VetBottomBar
import com.example.ui.screens.AddAnimalDialog
import com.example.ui.screens.AiVetAssistantScreen
import com.example.ui.screens.AnimalDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.VaccinationScreen
import com.example.ui.screens.VoiceLogScreen
import com.example.ui.theme.VetRecordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetRecordTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: VetViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavItem.HOME) }
    var selectedAnimalForDetail by remember { mutableStateOf<AnimalEntity?>(null) }
    var showAddAnimalDialog by remember { mutableStateOf(false) }

    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedAnimalForDetail == null) {
                VetBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (selectedAnimalForDetail == null) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            if (selectedAnimalForDetail != null) {
                AnimalDetailScreen(
                    animal = selectedAnimalForDetail!!,
                    viewModel = viewModel,
                    onBack = { selectedAnimalForDetail = null },
                    onOpenVoiceAi = {
                        selectedAnimalForDetail = null
                        currentTab = NavItem.VOICE
                    }
                )
            } else {
                Crossfade(
                    targetState = currentTab,
                    label = "TabCrossfade"
                ) { tab ->
                    when (tab) {
                        NavItem.HOME, NavItem.ANIMALS -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onAnimalClick = { selectedAnimalForDetail = it },
                                onOpenVoiceAi = { currentTab = NavItem.VOICE },
                                onOpenAddAnimal = { showAddAnimalDialog = true }
                            )
                        }
                        NavItem.VOICE -> {
                            VoiceLogScreen(viewModel = viewModel)
                        }
                        NavItem.AI_ASSISTANT -> {
                            AiVetAssistantScreen(viewModel = viewModel)
                        }
                        NavItem.VACCINES -> {
                            VaccinationScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    if (showAddAnimalDialog) {
        AddAnimalDialog(
            onDismiss = { showAddAnimalDialog = false },
            onSave = { animal ->
                viewModel.addNewAnimal(animal)
                showAddAnimalDialog = false
            }
        )
    }
}
