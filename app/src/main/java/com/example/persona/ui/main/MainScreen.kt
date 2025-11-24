package com.example.persona.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Chat : Screen("chat", "消息", Icons.Default.Email)
    object Contacts : Screen("contacts", "联系人", Icons.Default.Face)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    
    // State to handle nested navigation (like Chat Detail)
    // If we are in a detailed view, we might want to hide the bottom bar, 
    // but for simplicity in this structure, we'll keep it or just overlay full screen.
    // The standard Compose Navigation approach is used here.
    
    val items = listOf(
        Screen.Chat,
        Screen.Contacts,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            // Hide bottom bar on detail screens if needed. 
            // For now, let's show it only on main tabs.
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Simple check: if route is one of the main tabs
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
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
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
            composable(Screen.Contacts.route) {
                ContactScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
            
            // Detail Screen
            composable("chat_detail/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                ChatDetailScreen(
                    chatId = chatId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
