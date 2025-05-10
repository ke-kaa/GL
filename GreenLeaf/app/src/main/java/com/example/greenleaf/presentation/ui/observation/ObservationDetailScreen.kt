package com.example.greenleaf.presentation.ui.observation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
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
import com.example.greenleaf.presentation.viewmodels.ObservationDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationDetailScreen(
    navController: NavController,
    observationId: String,
    observationViewModel: ObservationDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    // Load when the screen appears
    LaunchedEffect(observationId) {
        if (observationId.isNotBlank()) {
            observationViewModel.loadObservation(observationId)
        }
    }

    val obs = observationViewModel.observation
    val isLoading = observationViewModel.isLoading
    val error = observationViewModel.error

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
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(error, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch { observationViewModel.loadObservation(observationId) }
                        }) { Text("Retry") }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) { Text("Go Back") }
                    }
                }
                obs != null -> {
                    // Layered header + card
                    Box(Modifier.fillMaxSize()) {
                        // 1) Header image + back button
                        AsyncImage(
                            model = obs.observationImageUrl,
                            contentDescription = obs.relatedPlantName,
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
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // 2) White card starts below the image minus 24dp overlap
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
                                // Plant name
                                Text(
                                    text = obs.relatedPlantName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                // Scientific name (if mapped in your model)


                                Spacer(Modifier.height(16.dp))

                                // Date/time + location
                                val raw = "${obs.date} ${obs.time}"
                                val formatted = runCatching {
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        .parse(raw)
                                        ?.let { d ->
                                            SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault())
                                                .format(d)
                                        }
                                }.getOrNull() ?: raw

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = "Location",
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(formatted, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    obs.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 32.dp)
                                )

                                Spacer(Modifier.height(16.dp))

                                // Notes
                                Text("Notes", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(obs.note ?: "No notes provided.")

                                Spacer(Modifier.height(24.dp))

                                // Buttons
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                Screen.AddEditObservation.createRoute(obs.id)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFA5D6A7),
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit Observation")
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                observationViewModel.deleteObservation(obs.id)
                                                if (observationViewModel.error == null) {
                                                    homeViewModel.refreshObservations()
                                                    delay(500)
                                                    navController.navigate(
                                                        Screen.Home.route + "?fromDeletion=true&tab=observations"
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
                                        Text("Delete Observation")
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No observation data available",
                            style = MaterialTheme.typography.bodyLarge
                        )
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
