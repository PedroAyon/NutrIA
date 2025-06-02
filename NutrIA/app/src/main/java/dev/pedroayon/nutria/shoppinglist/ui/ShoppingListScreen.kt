package dev.pedroayon.nutria.shoppinglist.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import dev.pedroayon.nutria.common.data.ShoppingListManager
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isChecked: Boolean = false
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    val context = LocalContext.current
    val shoppingListManager = remember { ShoppingListManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Observe the shopping list from the manager (the source of truth)
    val persistedShoppingListStrings by shoppingListManager.shoppingListFlow.collectAsState()

    // This is the local mutable list that drives the UI (LazyColumn)
    val items = remember { mutableStateListOf<ShoppingItem>() }

    // This LaunchedEffect synchronizes the local 'items' list with the persisted list
    // It runs whenever persistedShoppingListStrings changes (i.e., when data is loaded or saved elsewhere)
    LaunchedEffect(persistedShoppingListStrings) {
        val currentItemsText = items.map { it.text }
        // Only update if the content actually differs to avoid unnecessary recompositions
        if (currentItemsText != persistedShoppingListStrings) {
            items.clear()
            persistedShoppingListStrings.forEach { text ->
                // When loading from a simple string list, assume unchecked state (as your current ShoppingListManager only saves strings)
                items.add(ShoppingItem(text = text, isChecked = false))
            }
            Log.d("ShoppingListScreen", "Local list updated from manager: $persistedShoppingListStrings")
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var currentText by remember { mutableStateOf("") }
    var editingItemId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = {
                    Text("Lista de Compras")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    currentText = ""
                    editingItemId = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(
                items = items,
                key = { it.id }
            ) { item ->
                var checked by remember { mutableStateOf(item.isChecked) }
                var visible by remember { mutableStateOf(true) }

                if (visible) {
                    val strikethroughAnim by animateFloatAsState(
                        targetValue = if (checked) 1f else 0f,
                        animationSpec = tween(durationMillis = 300)
                    )

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    checked = isChecked
                                    // Update the item in the local mutableStateListOf
                                    val index = items.indexOfFirst { it.id == item.id }
                                    if (index != -1) {
                                        items[index] = items[index].copy(isChecked = isChecked)
                                    }

                                    if (isChecked) {
                                        coroutineScope.launch {
                                            delay(500) // Animation delay
                                            visible = false // Start fade out
                                            delay(500) // Wait for fade out to complete

                                            // CRUCIAL: Get the list AFTER removal for saving
                                            val listAfterRemoval = items.filter { it.id != item.id }.map { it.text }
                                            shoppingListManager.saveShoppingList(listAfterRemoval)
                                            Log.d("ShoppingListScreen", "Item removed and list saved: $listAfterRemoval")

                                            // Now remove from the UI list if it hasn't already been updated by the LaunchedEffect
                                            // This prevents a flickering if the LaunchedEffect hasn't triggered yet.
                                            // The LaunchedEffect observing 'persistedShoppingListStrings' will eventually
                                            // reconcile 'items' with the truly saved list.
                                            items.remove(item)
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.text,
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(1f - strikethroughAnim)
                                    .clickable {
                                        currentText = item.text
                                        editingItemId = item.id
                                        showDialog = true
                                    },
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                currentText = ""
                editingItemId = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentText.isNotBlank()) {
                            if (editingItemId != null) {
                                val index = items.indexOfFirst { it.id == editingItemId }
                                if (index >= 0) {
                                    items[index] = items[index].copy(text = currentText)
                                }
                            } else {
                                items.add(ShoppingItem(text = currentText, isChecked = false))
                            }
                            // Save to preferences immediately after adding/editing
                            shoppingListManager.saveShoppingList(items.map { it.text })
                            Log.d("ShoppingListScreen", "Item added/edited and list saved: ${items.map { it.text }}")
                        }
                        showDialog = false
                        currentText = ""
                        editingItemId = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    currentText = ""
                    editingItemId = null
                }) {
                    Text("Cancelar")
                }
            },
            title = {
                Text(if (editingItemId != null) "Editar producto" else "Agregar producto")
            },
            text = {
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    placeholder = { Text("Nombre del producto") }
                )
            }
        )
    }
}