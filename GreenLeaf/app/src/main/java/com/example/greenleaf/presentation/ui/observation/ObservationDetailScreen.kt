package com.example.greenleaf.presentation.ui.observation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.greenleaf.presentation.viewmodels.PlantDetailViewModel
import com.example.greenleaf.presentation.viewmodels.ObservationDetailViewModel
import com.example.greenleaf.fakedata.Observation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.greenleaf.presentation.components.MainBottomBar
import kotlinx.coroutines.launch
import com.example.greenleaf.presentation.navigation.Screen
import com.example.greenleaf.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationDetailScreen(
    navController: NavController,
    observationId: String,
    observationViewModel: ObservationDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(observationId) {
        if (observationId.isNotBlank()) {
            observationViewModel.loadObservation(observationId)
        }
    }
//   dameabera11@gmail.com  password  2222-22-22
    val observation = observationViewModel.observation
    val isLoading = observationViewModel.isLoading
    val error = observationViewModel.error

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Observation Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(navController)
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
                                    observationViewModel.loadObservation(observationId)
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
                observation != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = observation.observationImageUrl,
                            contentDescription = observation.relatedPlantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = observation.relatedPlantName,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(Modifier.height(16.dp))

                        Text("Location", fontWeight = FontWeight.Bold)
                        Text(observation.location)

                        Spacer(Modifier.height(8.dp))

                        Text("Notes", fontWeight = FontWeight.Bold)
                        Text(observation.note ?: "No notes provided.")

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate(Screen.AddEditObservation.createRoute(observation.id))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit Observation")
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        observationViewModel.deleteObservation(observation.id)
                                        if (observationViewModel.error == null) {
                                            homeViewModel.refreshObservations()
                                            kotlinx.coroutines.delay(500)
                                            navController.navigate(Screen.Home.route + "?fromDeletion=true&tab=observations") {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete Observation")
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
                            text = "No observation data available",
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

