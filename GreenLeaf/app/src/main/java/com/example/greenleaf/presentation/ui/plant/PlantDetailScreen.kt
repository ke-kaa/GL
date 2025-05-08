package com.example.greenleaf.presentation.ui.plant
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.greenleaf.presentation.viewmodels.PlantDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.greenleaf.presentation.viewmodels.HomeViewModel
import com.example.greenleaf.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: String,
    plantViewModel: PlantDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(plantId) {
        if (plantId.isNotBlank()) {
            plantViewModel.loadPlant(plantId)
        }
    }

    val plant = plantViewModel.plant
    val isLoading = plantViewModel.isLoading
    val error = plantViewModel.error

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Plant Details") },
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                scope.launch {
                                    plantViewModel.loadPlant(plantId)
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                plant != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = plant.plantImageUrl,
                            contentDescription = plant.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = plant.commonName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = plant.scientificName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(16.dp))

                        Text("Habitat", fontWeight = FontWeight.Bold)
                        Text(plant.habitat)

                        Spacer(Modifier.height(8.dp))

                        Text("Origin", fontWeight = FontWeight.Bold)
                        Text(plant.origin ?: "-")

                        Spacer(Modifier.height(8.dp))

                        Text("Description", fontWeight = FontWeight.Bold)
                        Text(plant.description ?: "No description provided.")

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { 
                                    navController.navigate(Screen.AddEditPlant.createRoute(plant.id))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit Plant")
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        plantViewModel.deletePlant(plant.id)
                                        if (plantViewModel.error == null) {
                                            homeViewModel.refreshPlants()
                                            kotlinx.coroutines.delay(500)
                                            navController.navigate(Screen.Home.route + "?fromDeletion=true&tab=plants") {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete Plant")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No plant data available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}
