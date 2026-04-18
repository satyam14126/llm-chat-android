package com.llmchat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.llmchat.app.ui.chat.ChatScreen
import com.llmchat.app.ui.sessions.SessionsScreen
import com.llmchat.app.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Sessions : Screen("sessions")
    object Chat : Screen("chat/{sessionId}") {
        fun createRoute(sessionId: Long) = "chat/$sessionId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Sessions.route) {
        composable(Screen.Sessions.route) {
            SessionsScreen(
                onSessionSelected = { sessionId ->
                    navController.navigate(Screen.Chat.createRoute(sessionId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
            ChatScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
