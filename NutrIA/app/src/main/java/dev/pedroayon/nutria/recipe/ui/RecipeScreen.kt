package dev.pedroayon.nutria.recipe.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.meetup.twain.MarkdownText
import dev.pedroayon.nutria.auth.domain.model.Recipe
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.serialization.json.Json

@Composable
fun RecipeScreen(navController: NavController, encodedRecipe: String) {
    val json = Json { ignoreUnknownKeys = true }
    val recipe = remember {
        val decoded = Uri.decode(encodedRecipe)
        json.decodeFromString<Recipe>(decoded)
    }

    val displayText = remember(recipe) { recipe.toString() }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = { Text(recipe.name) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MarkdownText(
                markdown = displayText,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
