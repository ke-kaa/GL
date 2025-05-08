package com.example.greenleaf.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.greenleaf.presentation.viewmodels.ProfileViewModel
import com.example.greenleaf.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val showDeleteDlg by viewModel.showDeleteDlg.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Refresh profile when returning from edit screen
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // If user is gone, go to login
    if (isLoggedOut) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Profile.route)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                    label = { Text("Account") },
                    selected = true,
                    onClick = { /* already here */ }
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.EditProfile.route) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUserProfile() }) {
                            Text("Retry")
                        }
                    }
                }
                user != null -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        // Display name if available, otherwise show empty space
                        if (user?.firstName != null || user?.lastName != null) {
                            Text(
                                "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            Spacer(Modifier.height(24.dp))
                        }
                        
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium)
                        
                        // Display other profile information if available
                        user?.birthdate?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Birth Date: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        user?.gender?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Gender: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        user?.phoneNumber?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Phone: $it", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { viewModel.logout() },
                            Modifier.fillMaxWidth()
                        ) {
                            Text("Log Out")
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.showDeleteDialog(true) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Account")
                        }
                    }
                }
            }
        }

        if (showDeleteDlg) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog(false) },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAccount()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
