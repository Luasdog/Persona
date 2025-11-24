package com.example.persona.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.persona.ui.persona.PersonaCreationScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Chat : Screen("chat", "消息", Icons.Default.Email)
    object Social : Screen("social", "广场", Icons.Default.Home)
    object Contacts : Screen("contacts", "联系人", Icons.Default.Face)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Chat,
        Screen.Social,
        Screen.Contacts,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            val isMainTab = items.any { it.route == currentDestination?.route }
            
            if (isMainTab) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute == Screen.Contacts.route) {
                FloatingActionButton(onClick = { navController.navigate("create_persona") }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Persona")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                ChatListScreen(
                    onChatClick = { chatId ->
                        navController.navigate("chat_detail/$chatId")
                    }
                )
            }
            composable(Screen.Social.route) {
                SocialScreen()
            }
            composable(Screen.Contacts.route) {
                ContactScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
            
            composable("chat_detail/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                ChatDetailScreen(
                    chatId = chatId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("create_persona") {
                PersonaCreationScreen(
                    onBackClick = { navController.popBackStack() },
                    onPersonaCreated = { navController.popBackStack() }
                )
            }
        }
    }
}