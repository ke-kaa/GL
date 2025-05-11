package com.example.greenleaf.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.greenleaf.presentation.ui.admin.AdminDashboardScreen
import com.example.greenleaf.presentation.ui.home.HomeScreen
import com.example.greenleaf.presentation.ui.login.LoginScreen
import com.example.greenleaf.presentation.ui.observation.AddEditObservationScreen
import com.example.greenleaf.presentation.ui.observation.ObservationDetailScreen
import com.example.greenleaf.presentation.ui.plant.AddEditPlantScreen
import com.example.greenleaf.presentation.ui.plant.PlantDetailScreen
import com.example.greenleaf.presentation.ui.profile.EditProfileScreen
import com.example.greenleaf.presentation.ui.profile.ProfileScreen
import com.example.greenleaf.presentation.ui.signup.SignUpScreen
import com.example.greenleaf.presentation.viewmodels.*

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // --- Login & Signup ---
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        // --- Main Tabs ---
        composable(
            route = Screen.Home.route + "?fromDeletion={fromDeletion}&tab={tab}",
            arguments = listOf(
                navArgument("fromDeletion") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = "plants"
                }
            )
        ) { backStackEntry ->
            // Check if user is admin
            val isAdmin = backStackEntry.savedStateHandle.get<Boolean>("isAdmin") ?: false
            if (isAdmin) {
                // Redirect admin users to admin dashboard
                navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            } else {
                HomeScreen(navController = navController)
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // --- Plant Screens ---
        composable(
            route = Screen.AddEditPlant.route,
            arguments = listOf(
                navArgument("plantId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val plantId = backStack.arguments?.getString("plantId")
            AddEditPlantScreen(navController = navController, plantId = plantId)
        }

        // --- Observation Screens ---
        composable(
            route = Screen.AddEditObservation.route,
            arguments = listOf(
                navArgument("observationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val obsId = backStack.arguments?.getString("observationId")
            AddEditObservationScreen(
                navController = navController,
                observationId = obsId
            )
        }

        // --- Admin & Profile Edit ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(
            route = Screen.PlantDetail.route,
            arguments = listOf(navArgument("plantId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val plantId = backStack.arguments!!.getString("plantId")!!
            PlantDetailScreen(navController, plantId)
        }

        // Observation detail – register exactly "observation_detail/{observationId}"
        composable(
            route = Screen.ObservationDetail.route,
            arguments = listOf(navArgument("observationId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val obsId = backStack.arguments!!.getString("observationId")!!
            ObservationDetailScreen(navController, obsId)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
    }
}
