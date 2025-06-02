package dev.pedroayon.nutria.recipe.ui

import android.net.Uri
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dev.pedroayon.nutria.common.model.Recipe
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.chat.ui.ApiClient // Ensure ApiClient is correctly imported
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeBookScreen(navController: NavController) {
    val recipes = remember { mutableStateListOf<Recipe>() }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showPopup by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    // State to hold the Firebase User ID, formatted as "Bearer <UID>"
    var userIdAsBearerToken by remember { mutableStateOf<String?>(null) }

    // Fetch Firebase User ID and format it as "Bearer <UID>"
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            userIdAsBearerToken = "Bearer $uid" // FORCING UID AS BEARER TOKEN
            Log.d("RecipeBookScreen", "Firebase User ID (UID) formatted as Bearer token: $userIdAsBearerToken")
        } else {
            Log.e("RecipeBookScreen", "Firebase User is null. Cannot get UID.")
            // Handle case where user is not logged in (e.g., navigate to login)
        }
    }

    // Function to fetch recipes from the API using the userIdAsBearerToken
    val fetchRecipes: () -> Unit = {
        if (userIdAsBearerToken != null) {
            coroutineScope.launch {
                try {
                    // Pass userIdAsBearerToken directly to the API
                    val response = ApiClient.instance.getRecipes(userIdAsBearerToken!!)
                    if (response.isSuccessful) {
                        response.body()?.recipes?.let { fetchedRecipes ->
                            recipes.clear()
                            recipes.addAll(fetchedRecipes)
                            Log.d("RecipeBookScreen", "Recipes fetched: ${fetchedRecipes.size}")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("RecipeBookScreen", "Error fetching recipes: ${response.code()} - $errorBody")
                        // Optionally, show a SnackBar or Toast to the user
                    }
                } catch (e: Exception) {
                    Log.e("RecipeBookScreen", "Exception fetching recipes: ${e.message}", e)
                    // Optionally, show a SnackBar or Toast to the user
                }
            }
        } else {
            Log.d("RecipeBookScreen", "Firebase User ID as Bearer token not available, cannot fetch recipes yet.")
        }
    }

    // Trigger recipe fetch when userIdAsBearerToken becomes available
    LaunchedEffect(userIdAsBearerToken) {
        fetchRecipes()
    }

    Scaffold(
        topBar = {
            CommonTopBar(title = { Text("Libro de Recetas") })
        }
    ) { innerPadding ->
        if (recipes.isEmpty() && userIdAsBearerToken != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty() && userIdAsBearerToken == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando recetas...")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.id ?: UUID.randomUUID().hashCode() }) { recipe ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .combinedClickable(
                                onClick = {
                                    val jsonRecipe = gson.toJson(recipe)
                                    val encodedJson = Uri.encode(jsonRecipe)
                                    navController.navigate("app/recipe/$encodedJson")
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
                                    .padding(start = 12.dp, top = 12.dp, end = 48.dp, bottom = 12.dp),
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
        }

        if (showPopup && selectedRecipe != null) {
            AlertDialog(
                onDismissRequest = {
                    selectedRecipe = null
                    showPopup = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedRecipe?.id?.let { recipeId ->
                            if (userIdAsBearerToken != null) {
                                coroutineScope.launch {
                                    try {
                                        // Pass userIdAsBearerToken directly to the API for deletion
                                        val response = ApiClient.instance.deleteRecipe(userIdAsBearerToken!!, recipeId.toString())
                                        if (response.isSuccessful) {
                                            Log.d("RecipeBookScreen", "Recipe ID $recipeId deleted successfully using UID as bearer token.")
                                            recipes.remove(selectedRecipe)
                                            selectedRecipe = null
                                            showPopup = false
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                            Log.e("RecipeBookScreen", "Error deleting recipe: ${response.code()} - $errorBody")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RecipeBookScreen", "Exception deleting recipe: ${e.message}", e)
                                    }
                                }
                            } else {
                                Log.e("RecipeBookScreen", "Firebase User ID as Bearer token not available, cannot delete recipe.")
                            }
                        }
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
                text = { Text("¿Estás seguro de que deseas eliminar la receta '${selectedRecipe?.name}'?") }
            )
        }
    }
}