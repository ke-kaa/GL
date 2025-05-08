package com.example.greenleaf.presentation.ui.plant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.greenleaf.presentation.navigation.Screen
import com.example.greenleaf.presentation.viewmodels.AddEditPlantViewModel
import com.example.greenleaf.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    navController: NavController,
    viewModel: AddEditPlantViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    plantId: String? = null
) {
    // If editing, load existing plant into form
    LaunchedEffect(plantId) {
        plantId?.let { viewModel.loadPlant(it) }
    }

    // Handle save success
    LaunchedEffect(viewModel.isSaved.value) {
        if (viewModel.isSaved.value) {
            // Refresh home screen data
            homeViewModel.loadData()
            // Navigate to plant detail screen
            navController.navigate(Screen.PlantDetail.createRoute(viewModel.plantId.value ?: "")) {
                // Clear the back stack up to home screen
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add/Edit Plant") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Add Image")
                    Text("Add/Change Image")
                }

                Spacer(Modifier.height(16.dp))

                // Form fields
                OutlinedTextField(
                    value = viewModel.commonName.value,
                    onValueChange = { viewModel.commonName.value = it },
                    label = { Text("Common Name") },
                    placeholder = { Text("e.g. Red Maple") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.error.value != null && viewModel.commonName.value.isBlank()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.scientificName.value,
                    onValueChange = { viewModel.scientificName.value = it },
                    label = { Text("Scientific Name") },
                    placeholder = { Text("e.g. Acer rubrum") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.error.value != null && viewModel.scientificName.value.isBlank()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.habitat.value,
                    onValueChange = { viewModel.habitat.value = it },
                    label = { Text("Habitat") },
                    placeholder = { Text("e.g. Deciduous forest") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.error.value != null && viewModel.habitat.value.isBlank()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.origin.value,
                    onValueChange = { viewModel.origin.value = it },
                    label = { Text("Origin") },
                    placeholder = { Text("e.g. Ethiopia") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.description.value,
                    onValueChange = { viewModel.description.value = it },
                    label = { Text("Description") },
                    placeholder = { Text("Provide detailed description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                viewModel.error.value?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Cancel / Save buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { 
                            viewModel.savePlant { id ->
                                // ID is already set in the plantId state
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
