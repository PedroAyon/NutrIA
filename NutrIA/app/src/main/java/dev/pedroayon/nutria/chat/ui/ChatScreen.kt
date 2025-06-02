package dev.pedroayon.nutria.chat.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.auth.domain.model.MessageType
import dev.pedroayon.nutria.chat.domain.model.ChatMessage
import dev.pedroayon.nutria.chat.domain.model.Message
import dev.pedroayon.nutria.chat.domain.model.MessageRole
import dev.pedroayon.nutria.chat.ui.components.ChatBottomBar
import dev.pedroayon.nutria.chat.ui.components.MessageBubble
import dev.pedroayon.nutria.common.data.ApiService
import dev.pedroayon.nutria.common.data.ShoppingListManager
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

// --- API Service Setup (Simple) ---
object ApiClient {
    private const val BASE_URL = "https://direct-kodiak-grateful.ngrok-free.app"

    val gson: Gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Use BODY for dev, NONE for production
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
// --- End API Service Setup ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val hintText = stringResource(id = R.string.chat_hint)
    val coroutineScope = rememberCoroutineScope()
    var isBotTyping by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val shoppingListManager = remember { ShoppingListManager(context) }

    val messages = remember { mutableStateListOf<ChatMessage>() }
    val apiChatHistory = remember { mutableStateListOf<Message>() }
    val apiMessageIdCounter = remember { AtomicInteger(0) }

    var userIdAsBearerToken by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) } // For camera capture
    var showPermissionRationale by remember { mutableStateOf(false) } // State for showing rationale dialog

    var showImageMessageDialog by remember { mutableStateOf(false) }
    var imageMessageInput by remember { mutableStateOf("") }
    val selectedImageUris = remember { mutableStateListOf<Uri>() }


    // Launcher for taking photos
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    selectedImageUris.clear()
                    selectedImageUris.add(uri)
                    showImageMessageDialog = true
                }
            } else {
                Log.d("ChatScreen", "Picture capture cancelled or failed.")
            }
            tempImageUri = null // Clear temp URI after use
        }
    )

    // --- Permission Launcher ---
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, proceed to launch camera
                launchCamera(context, takePictureLauncher) { uri -> tempImageUri = uri }
            } else {
                // Permission denied
                Log.e("ChatScreen", "CAMERA permission denied.")
                showPermissionRationale = true // Show rationale if denied
            }
        }
    )

    // Launcher for selecting images from gallery (now allows multiple)
    val pickMultipleImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(), // Use GetMultipleContents
        onResult = { uris: List<Uri>? ->
            uris?.let {
                if (it.isNotEmpty()) {
                    selectedImageUris.clear()
                    selectedImageUris.addAll(it)
                    showImageMessageDialog = true
                }
            } ?: Log.d("ChatScreen", "Image selection cancelled.")
        }
    )

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            userIdAsBearerToken = "Bearer $uid"
            Log.d("ChatScreen", "Firebase User ID (UID) formatted as Bearer token: $userIdAsBearerToken")
        } else {
            Log.e("ChatScreen", "Firebase User is null. User might not be logged in.")
        }

        if (messages.isEmpty()) {
            val initialBotMessage = ChatMessage(
                text = "Soy NutrIA, tu asistente de nutrición y cocina. ¿Cómo puedo ayudarte hoy?",
                messageType = MessageType.BOT
            )
            messages.add(initialBotMessage)
            apiChatHistory.add(
                Message(
                    id = apiMessageIdCounter.getAndIncrement(),
                    role = MessageRole.ASSISTANT,
                    text = initialBotMessage.text
                )
            )
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CommonTopBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.chat_topbar_title))
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomBar(
                currentInput = currentInput,
                onInputChange = { currentInput = it },
                onSendClick = {
                    if (currentInput.isNotBlank()) {
                        val userInputText = currentInput
                        val userChatMessage = ChatMessage(text = userInputText, messageType = MessageType.USER)
                        messages.add(userChatMessage)

                        apiChatHistory.add(
                            Message(
                                id = apiMessageIdCounter.getAndIncrement(),
                                role = MessageRole.USER,
                                text = userInputText
                            )
                        )

                        currentInput = ""
                        isBotTyping = true // Set typing indicator immediately

                        coroutineScope.launch {
                            // When sending a text-only message, photoParts will be empty
                            // The message is already added to apiChatHistory
                            sendMessageToBot(
                                userIdAsBearerToken,
                                messages,
                                apiChatHistory,
                                apiMessageIdCounter,
                                shoppingListManager,
                                emptyList() // No photos for text message
                            ) { value -> isBotTyping = value }
                        }
                    }
                },
                onCameraClick = {
                    showImageSourceDialog = true
                },
                hintText = hintText,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onRecipeSaveToggle = { recipeToToggle, shouldSave ->
                            coroutineScope.launch {
                                // Ensure userIdAsBearerToken is available before making the API call
                                if (userIdAsBearerToken == null) {
                                    Log.e("ChatScreenRecipe", "Cannot save/unsave recipe: User ID as Bearer token is null.")
                                    val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                    if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = !shouldSave)
                                    return@launch
                                }

                                if (shouldSave) {
                                    try {
                                        val response = ApiClient.instance.createRecipe(userIdAsBearerToken!!, recipeToToggle)
                                        if (response.isSuccessful) {
                                            val newRecipeId = response.body()?.recipeId
                                            Log.i("ChatScreenRecipe", "Recipe ${recipeToToggle.name} saved successfully with ID: $newRecipeId using UID as bearer token.")
                                            val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                            if (msgIndex != -1) {
                                                val updatedRecipe = recipeToToggle.copy(id = newRecipeId)
                                                messages[msgIndex] = msg.copy(recipe = updatedRecipe, isRecipeSavedInMemory = true)
                                            }
                                        } else {
                                            Log.e("ChatScreenRecipe", "Error saving recipe: ${response.code()} - ${response.errorBody()?.string()}")
                                            val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                            if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = false)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ChatScreenRecipe", "Exception saving recipe: ${e.message}", e)
                                        val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                        if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = false)
                                    }
                                } else {
                                    recipeToToggle.id?.let { recipeId ->
                                        try {
                                            val response = ApiClient.instance.deleteRecipe(userIdAsBearerToken!!, recipeId.toString())
                                            if (response.isSuccessful) {
                                                Log.i("ChatScreenRecipe", "Recipe ID $recipeId unsaved successfully using UID as bearer token.")
                                                val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                                if (msgIndex != -1) {
                                                    messages[msgIndex] = msg.copy(isRecipeSavedInMemory = false)
                                                }
                                            } else {
                                                Log.e("ChatScreenRecipe", "Error unsaving recipe: ${response.code()} - ${response.errorBody()?.string()}")
                                                val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                                if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = true)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ChatScreenRecipe", "Exception unsaving recipe: ${e.message}", e)
                                            val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                            if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = true)
                                        }
                                    } ?: run {
                                        Log.w("ChatScreenRecipe", "Cannot unsave recipe, ID is null.")
                                        val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                        if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = true)
                                    }
                                }
                            }
                        }
                    )
                }
                if (isBotTyping) {
                    item {
                        WritingAnimation()
                    }
                }
            }

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(index = messages.size - 1)
                }
            }
        }

        // Image source selection dialog
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Seleccionar imagen") },
                text = { Text("¿Deseas tomar una foto o seleccionarla de la galería?") },
                confirmButton = {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        // Check for CAMERA permission before launching camera
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                launchCamera(context, takePictureLauncher) { uri -> tempImageUri = uri }
                            }
                            // You can add a `shouldShowRequestPermissionRationale` check here for a custom dialog
                            else -> {
                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }) {
                        Text("Tomar foto")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pickMultipleImagesLauncher.launch("image/*") // Launch for multiple
                        showImageSourceDialog = false
                    }) {
                        Text("Galería")
                    }
                }
            )
        }

        // Permission Rationale Dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                title = { Text("Permiso de Cámara Requerido") },
                text = { Text("Para tomar fotos y subirlas a NutrIA, necesitamos acceso a tu cámara. Por favor, concede el permiso en la configuración de la aplicación.") },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionRationale = false
                        // Optionally, guide the user to app settings
                        // val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        // val uri = Uri.fromParts("package", context.packageName, null)
                        // intent.data = uri
                        // context.startActivity(intent)
                    }) {
                        Text("Entendido")
                    }
                }
            )
        }

        // Dialog for message with image(s)
        if (showImageMessageDialog) {
            AlertDialog(
                onDismissRequest = {
                    showImageMessageDialog = false
                    selectedImageUris.clear()
                    imageMessageInput = ""
                },
                title = { Text("Añadir mensaje a la imagen") },
                text = {
                    Column {
                        Text("Puedes añadir un mensaje a tus imágenes (opcional):")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = imageMessageInput,
                            onValueChange = { imageMessageInput = it },
                            label = { Text("Tu mensaje") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val messageForApi = imageMessageInput.ifBlank { "Imágenes enviadas." } // Use this for API history
                        val userTextForUI = imageMessageInput.ifBlank { "Imágenes enviadas." } // Use this for local UI chat bubble
                        val photosToSend = selectedImageUris.toList()

                        // Add user message to chat UI immediately
                        messages.add(ChatMessage(text = userTextForUI, messageType = MessageType.USER))

                        // Add user message to API chat history BEFORE sending
                        apiChatHistory.add(
                            Message(
                                id = apiMessageIdCounter.getAndIncrement(),
                                role = MessageRole.USER,
                                text = messageForApi
                            )
                        )

                        coroutineScope.launch {
                            handleImageUpload(
                                photosToSend, // Pass list of Uris
                                context,
                                messages,
                                apiChatHistory,
                                apiMessageIdCounter,
                                userIdAsBearerToken,
                                shoppingListManager
                            ) { value -> isBotTyping = value }
                        }
                        showImageMessageDialog = false
                        selectedImageUris.clear()
                        imageMessageInput = "" // Clear input after sending
                    }) {
                        Text("Enviar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showImageMessageDialog = false
                        selectedImageUris.clear()
                        imageMessageInput = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Helper function to create URI and launch camera
private fun launchCamera(
    context: Context,
    takePictureLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    onUriCreated: (Uri) -> Unit
) {
    val tmpFile = File.createTempFile("IMG_", ".jpg", context.cacheDir).apply {
        createNewFile()
    }
    val uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        tmpFile
    )
    onUriCreated(uri)
    takePictureLauncher.launch(uri)
}


// --- Helper function for image processing and upload ---
private suspend fun handleImageUpload(
    uris: List<Uri>, // Now accepts a list of Uris
    context: Context,
    messages: MutableList<ChatMessage>,
    apiChatHistory: MutableList<Message>,
    apiMessageIdCounter: AtomicInteger,
    userIdAsBearerToken: String?,
    shoppingListManager: ShoppingListManager, // Pass the manager instance
    onSetBotTyping: (Boolean) -> Unit // Renamed and changed type to lambda
) {
    onSetBotTyping(true) // Call the lambda

    withContext(Dispatchers.IO) {
        val photoParts = mutableListOf<MultipartBody.Part>()
        try {
            uris.forEachIndexed { index, uri ->
                val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$index.jpg")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photos", tempFile.name, requestFile)
                photoParts.add(photoPart)
            }


            // Now send the message with the photo(s)
            // The text message for the API is already added to apiChatHistory before calling handleImageUpload
            sendMessageToBot(
                userIdAsBearerToken,
                messages,
                apiChatHistory,
                apiMessageIdCounter,
                shoppingListManager, // Pass the manager instance
                photoParts // Pass the actual photo parts
            ) { value -> onSetBotTyping(value) } // Pass lambda to update isBotTyping

            // Clean up temporary files
            photoParts.forEach { part ->
                // The filename is in the part.headers()["Content-Disposition"]
                val contentDisposition = part.headers?.get("Content-Disposition")
                val filename = contentDisposition?.substringAfter("filename=\"")?.substringBefore("\"")
                filename?.let {
                    File(context.cacheDir, it).delete()
                }
            }


        } catch (e: Exception) {
            Log.e("ChatScreenAPI", "Error processing image(s) for upload: ${e.message}", e)
            messages.add(ChatMessage(text = "Lo siento, no pude procesar la(s) imagen(es).", messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
        } finally {
            onSetBotTyping(false) // Call the lambda
        }
    }
}

// --- Helper function for sending message to bot ---
private suspend fun sendMessageToBot(
    userIdAsBearerToken: String?,
    messages: MutableList<ChatMessage>,
    apiChatHistory: MutableList<Message>,
    apiMessageIdCounter: AtomicInteger,
    shoppingListManager: ShoppingListManager, // Accept the manager instance
    photoParts: List<MultipartBody.Part>, // photoParts can be empty for text-only messages
    onSetBotTyping: (Boolean) -> Unit // Renamed and changed type to lambda
) {
    if (userIdAsBearerToken == null) {
        Log.e("ChatScreenAPI", "Cannot send message: User ID as Bearer token is null.")
        messages.add(ChatMessage(text = "Lo siento, no pude autenticarte. Por favor, reinicia la app.", messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
        onSetBotTyping(false) // Call the lambda
        return
    }

    try {
        val chatHistoryJson = ApiClient.gson.toJson(apiChatHistory.toList())
        val chatHistoryRequestBody = chatHistoryJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val shoppingListJson = ApiClient.gson.toJson(shoppingListManager.shoppingListFlow.value) // Access value from the passed manager
        val shoppingListRequestBody = shoppingListJson.toRequestBody("application/json; charset=utf-u8".toMediaTypeOrNull())


        val response = ApiClient.instance.sendMessage(
            token = userIdAsBearerToken,
            photos = photoParts, // This will be empty for text-only, or contain photos for image messages
            chatHistory = chatHistoryRequestBody,
            shoppingList = shoppingListRequestBody
        )

        if (response.isSuccessful) {
            response.body()?.let { botResponse ->
                var botMessageTextForApi: String? = null
                var botRecipeForApi: dev.pedroayon.nutria.common.model.Recipe? = null

                if (botResponse.recipe != null) {
                    val recipeMessage = ChatMessage(
                        text = botResponse.recipe.toString(),
                        messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.RECIPE,
                        recipe = botResponse.recipe
                    )
                    messages.add(recipeMessage)
                    botRecipeForApi = botResponse.recipe
                    botMessageTextForApi = "Aquí tienes una receta: ${botResponse.recipe.name}"
                }
                if (botResponse.shoppingList != null) {
                    shoppingListManager.saveShoppingList(botResponse.shoppingList) // Use the passed manager
                    val shoppingListText = "Tu lista de compras actualizada:\n" +
                            botResponse.shoppingList.joinToString("\n") { "- $it" }
                    messages.add(ChatMessage(text = shoppingListText, messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
                    if (botMessageTextForApi == null) botMessageTextForApi = shoppingListText else botMessageTextForApi += "\n" + shoppingListText
                }
                if (botResponse.message != null) {
                    messages.add(ChatMessage(text = botResponse.message, messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
                    if (botMessageTextForApi == null) botMessageTextForApi = botResponse.message else botMessageTextForApi += "\n" + botResponse.message
                }

                if (botMessageTextForApi != null || botRecipeForApi != null) {
                    apiChatHistory.add(
                        Message(
                            id = apiMessageIdCounter.getAndIncrement(),
                            role = MessageRole.ASSISTANT,
                            text = botMessageTextForApi,
                            recipe = botRecipeForApi
                        )
                    )
                }
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e("ChatScreenAPI", "Error sending message: ${response.code()} - $errorBody")
            messages.add(ChatMessage(text = "Lo siento, ocurrió un error al procesar tu solicitud. Código: ${response.code()}", messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
            apiChatHistory.add(Message(id = apiMessageIdCounter.getAndIncrement(), role = MessageRole.ASSISTANT, text = "Error response from server: $errorBody"))
        }
    } catch (e: Exception) {
        Log.e("ChatScreenAPI", "Exception sending message: ${e.message}", e)
        messages.add(ChatMessage(text = "Lo siento, no pude conectarme. Verifica tu conexión.", messageType = dev.pedroayon.nutria.auth.domain.model.MessageType.BOT))
    } finally {
        onSetBotTyping(false) // Call the lambda
    }
}


@Composable
fun WritingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("bot_typing.json")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
            .size(72.dp)
            .padding(start = 8.dp)
    )
}

@Preview
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen()
    }
}