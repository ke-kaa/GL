package com.example.greenleaf.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.greenleaf.presentation.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainBottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.startsWith(Screen.Home.route) == true,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            modifier = Modifier.padding(bottom = 4.dp),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00C853),
                selectedTextColor = Color(0xFF00C853),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                if (currentRoute != Screen.Profile.route) {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Account") },
            label = { Text("Account") },
            modifier = Modifier.padding(bottom = 4.dp),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00C853),
                selectedTextColor = Color(0xFF00C853),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
