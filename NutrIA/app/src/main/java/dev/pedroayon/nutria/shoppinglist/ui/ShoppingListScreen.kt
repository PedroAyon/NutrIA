package dev.pedroayon.nutria.shoppinglist.ui

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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    val items = remember {
        mutableStateListOf(
            ShoppingItem(text = "Leche"),
            ShoppingItem(text = "Huevos"),
            ShoppingItem(text = "Pan"),
            ShoppingItem(text = "Queso"),
            ShoppingItem(text = "Frutas"),
            ShoppingItem(text = "Verduras"),
            ShoppingItem(text = "Pasta"),
            ShoppingItem(text = "Carne")
        )
    }
    val coroutineScope = rememberCoroutineScope()
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
                var checked by remember { mutableStateOf(false) }
                var visible by remember { mutableStateOf(true) }

                if (visible) {
                    var strikethrough by remember { mutableStateOf(false) }

                    val alphaAnim by animateFloatAsState(
                        targetValue = if (strikethrough) 0f else 1f,
                        animationSpec = tween(durationMillis = 500)
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
                                onCheckedChange = {
                                    checked = true
                                    coroutineScope.launch {
                                        strikethrough = true
                                        delay(500)
                                        visible = false
                                        delay(500)
                                        items.remove(item)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.text,
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(alphaAnim)
                                    .clickable {
                                        currentText = item.text
                                        editingItemId = item.id
                                        showDialog = true
                                    },
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    textDecoration = if (strikethrough) TextDecoration.LineThrough else TextDecoration.None,
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
                                items.add(ShoppingItem(text = currentText))
                            }
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
