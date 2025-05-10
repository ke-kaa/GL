package com.example.greenleaf.presentation.ui.plant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.greenleaf.presentation.components.MainBottomBar
import com.example.greenleaf.presentation.navigation.Screen
import com.example.greenleaf.presentation.viewmodels.HomeViewModel
import com.example.greenleaf.presentation.viewmodels.PlantDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: String,
    plantViewModel: PlantDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    // Load plant on entry
    LaunchedEffect(plantId) {
        if (plantId.isNotBlank()) {
            plantViewModel.loadPlant(plantId)
        }
    }

    val plant = plantViewModel.plant
    val isLoading = plantViewModel.isLoading
    val error = plantViewModel.error

    Scaffold(
        bottomBar = { MainBottomBar(navController) }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                error != null -> {
                    // unchanged error UI
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(error, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { scope.launch { plantViewModel.loadPlant(plantId) } }) {
                            Text("Retry")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                plant != null -> {
                    // Layered header + card
                    Box(Modifier.fillMaxSize()) {
                        // 1) Header image + back arrow
                        AsyncImage(
                            model = plant.plantImageUrl,
                            contentDescription = plant.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp)
                                .size(36.dp)
                                .background(
                                    color = Color(0x66000000),
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // 2) White card, below image minus 24dp overlap
                        Surface(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 256.dp)  // 280 - 24
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                            ) {
                                // Common name
                                Text(
                                    text = plant.commonName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                // Scientific name
                                Text(
                                    text = plant.scientificName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Gray
                                )

                                Spacer(Modifier.height(16.dp))

                                // Habitat & Origin
                                Row {
                                    Text("Habitat: ", fontWeight = FontWeight.Bold)
                                    Text(plant.habitat)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    Text("Origin: ", fontWeight = FontWeight.Bold)
                                    Text(plant.origin ?: "-")
                                }

                                Spacer(Modifier.height(16.dp))

                                // Description
                                Text("Description", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(plant.description ?: "No description provided.")

                                Spacer(Modifier.height(24.dp))

                                // Edit / Delete buttons
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                Screen.AddEditPlant.createRoute(plant.id)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFA5D6A7),
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit Plant Details")
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                plantViewModel.deletePlant(plant.id)
                                                if (plantViewModel.error == null) {
                                                    homeViewModel.refreshPlants()
                                                    delay(500)
                                                    navController.navigate(
                                                        Screen.Home.route +
                                                                "?fromDeletion=true&tab=plants"
                                                    ) {
                                                        popUpTo(Screen.Home.route) { inclusive = false }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delete Plant Details")
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
                else -> {
                    // unchanged “no data” UI
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No plant data available", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

