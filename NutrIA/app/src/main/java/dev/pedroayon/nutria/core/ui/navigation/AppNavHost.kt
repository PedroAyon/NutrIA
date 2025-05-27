package dev.pedroayon.nutria.core.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.pedroayon.nutria.chat.ui.ChatScreen
import dev.pedroayon.nutria.core.ui.components.BottomBar

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenDestination.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenDestination.Chat.route) { ChatScreen() }
            composable(ScreenDestination.Recipes.route) { /* RecipesScreen() */ }
            composable(ScreenDestination.ShoppingList.route) { /* RecipesScreen() */ }
        }
    }


}