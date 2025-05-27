package dev.pedroayon.nutria.recipe.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import dev.pedroayon.nutria.auth.domain.model.Ingredient
import dev.pedroayon.nutria.auth.domain.model.Instruction
import dev.pedroayon.nutria.auth.domain.model.Recipe
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dev.pedroayon.nutria.R
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeBookScreen(navController: NavController) {
    var recipes by remember {
        mutableStateOf(
            listOf(
                Recipe(
                    name = "Espagueti a la Boloñesa",
                    description = "Un clásico platillo italiano de pasta.",
                    calories = 550,
                    prepTime = "30 min",
                    ingredients = listOf(
                        Ingredient("Espagueti", "200", "g"),
                        Ingredient("Carne molida", "150", "g"),
                        Ingredient("Salsa de tomate", "1", "taza")
                    ),
                    instructions = listOf(
                        Instruction("Hervir la pasta", "Cocinar hasta que esté al dente", 1, "10 min"),
                        Instruction("Cocinar la carne", "Dorar en sartén", 2, "10 min"),
                        Instruction("Mezclar la salsa", "Combinar todo", 3, "10 min")
                    )
                ),
                Recipe(
                    name = "Tacos",
                    description = "Tacos mexicanos con carne y vegetales.",
                    calories = 450,
                    prepTime = "20 min",
                    ingredients = listOf(
                        Ingredient("Tortillas", "3", "unidades"),
                        Ingredient("Carne", "100", "g"),
                        Ingredient("Lechuga", "50", "g")
                    ),
                    instructions = listOf(
                        Instruction("Preparar la carne", "Cocinar con especias", 1, "10 min"),
                        Instruction("Ensamblar los tacos", "Agregar todos los ingredientes a las tortillas", 2, "5 min")
                    )
                )
            )
        )
    }

    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showPopup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CommonTopBar(title = { Text("Libro de Recetas") })
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .combinedClickable(
                            onClick = {
                                val json = Json.encodeToString(recipe).encodeURL()
                                navController.navigate("app/recipe/$json")
                            },
                            onLongClick = {
                                selectedRecipe = recipe
                                showPopup = true
                            }
                        ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 12.dp, top = 12.dp, end = 32.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${recipe.calories} kcal • ${recipe.prepTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.chef_hat_24px),
                            contentDescription = "Chef",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (showPopup && selectedRecipe != null) {
            AlertDialog(
                onDismissRequest = { showPopup = false },
                confirmButton = {
                    TextButton(onClick = {
                        recipes = recipes.filterNot { it == selectedRecipe }
                        selectedRecipe = null
                        showPopup = false
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedRecipe = null
                        showPopup = false
                    }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("¿Eliminar receta?") },
                text = { Text("¿Estás seguro de que deseas eliminar esta receta?") }
            )
        }
    }
}

fun String.encodeURL(): String = java.net.URLEncoder.encode(this, "UTF-8")
