package com.example.greenleaf.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.greenleaf.presentation.components.MainBottomBar
import com.example.greenleaf.presentation.navigation.Screen
import com.example.greenleaf.presentation.viewmodels.ProfileViewModel
import com.example.greenleaf.presentation.components.AdminBottomBar

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
    val isAdmin by viewModel.isAdmin.collectAsState()

    // initial load
    LaunchedEffect(Unit) { viewModel.loadUserProfile() }

    // navigate away if logged out
    if (isLoggedOut) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                title = {
                    Text(
                        "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = { 
            if (isAdmin) {
                AdminBottomBar(navController)
            } else {
                MainBottomBar(navController)
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                user != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(24.dp))

                        // Avatar
                        val img = user!!.profileImage
                        if (!img.isNullOrBlank()) {
                            AsyncImage(
                                model = img,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Placeholder",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // First / Last name row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("First Name", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    user!!.firstName.orEmpty(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Last Name", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    user!!.lastName.orEmpty(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

// Birth Date & Gender side by side
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Birth Date
                            Column(Modifier.weight(1f)) {
                                Text("Birth Date", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    user!!.birthdate.orEmpty(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            }
                            // Gender
                            Column(Modifier.weight(1f)) {
                                Text("Gender", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    user!!.gender.orEmpty(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Email
                        Column(Modifier.fillMaxWidth()) {
                            Text("Email", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                user!!.email,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Mobile
                        Column(Modifier.fillMaxWidth()) {
                            Text("Mobile", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                user!!.phoneNumber.orEmpty(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        }

                        Spacer(Modifier.height(32.dp))

                        // Log Out
                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Delete Account
                        TextButton(
                            onClick = { viewModel.showDeleteDialog(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Delete Account",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(24.dp))
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
}


