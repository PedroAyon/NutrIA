package dev.pedroayon.nutria.common.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pedroayon.nutria.chat.ui.ChatScreen
import dev.pedroayon.nutria.common.ui.components.BottomBar
import dev.pedroayon.nutria.recipe.ui.RecipeBookScreen
import dev.pedroayon.nutria.recipe.ui.RecipeScreen
import dev.pedroayon.nutria.shoppinglist.ui.ShoppingListScreen

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
            composable(ScreenDestination.RecipeBook.route) { RecipeBookScreen(navController) }
            composable(
                route = "app/recipe/{recipeJson}",
                arguments = listOf(navArgument("recipeJson") { defaultValue = "" })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("recipeJson") ?: ""
                val recipeJson = java.net.URLDecoder.decode(encoded, "UTF-8")
                RecipeScreen(navController, recipeJson)
            }

            composable(ScreenDestination.ShoppingList.route) { ShoppingListScreen() }
        }
    }


}