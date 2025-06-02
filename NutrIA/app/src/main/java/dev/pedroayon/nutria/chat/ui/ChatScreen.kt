package dev.pedroayon.nutria.chat.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    // Observe the shopping list from the manager
    val currentShoppingListForApi by shoppingListManager.shoppingListFlow.collectAsState()

    val messages = remember { mutableStateListOf<ChatMessage>() }
    val apiChatHistory = remember { mutableStateListOf<Message>() }
    val apiMessageIdCounter = remember { AtomicInteger(0) }

    // State to hold the Firebase User ID, formatted as "Bearer <UID>"
    var userIdAsBearerToken by remember { mutableStateOf<String?>(null) }

    // --- CRITICAL CHANGE: Fetch UID and format it as Bearer token ---
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            userIdAsBearerToken = "Bearer $uid" // FORCING UID AS BEARER TOKEN
            Log.d("ChatScreen", "Firebase User ID (UID) formatted as Bearer token: $userIdAsBearerToken")
        } else {
            Log.e("ChatScreen", "Firebase User is null. User might not be logged in.")
            // Handle case where user is not logged in (e.g., navigate to login)
        }

        // Initial message from bot if list is empty
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
    // --- End CRITICAL CHANGE ---

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
                        isBotTyping = true

                        coroutineScope.launch {
                            // Ensure userIdAsBearerToken is available before making the API call
                            if (userIdAsBearerToken == null) {
                                Log.e("ChatScreenAPI", "Cannot send message: User ID as Bearer token is null.")
                                messages.add(ChatMessage(text = "Lo siento, no pude autenticarte. Por favor, reinicia la app.", messageType = MessageType.BOT))
                                isBotTyping = false
                                return@launch
                            }

                            try {
                                val chatHistoryJson = ApiClient.gson.toJson(apiChatHistory.toList())
                                val chatHistoryRequestBody = chatHistoryJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                                val shoppingListJson = ApiClient.gson.toJson(currentShoppingListForApi)
                                val shoppingListRequestBody = shoppingListJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                                val photoParts: List<MultipartBody.Part> = emptyList()

                                // --- CRITICAL CHANGE: Pass userIdAsBearerToken here ---
                                val response = ApiClient.instance.sendMessage(
                                    token = userIdAsBearerToken!!, // Pass the UID formatted as Bearer token
                                    photos = photoParts,
                                    chatHistory = chatHistoryRequestBody,
                                    shoppingList = shoppingListRequestBody
                                )
                                // --- End CRITICAL CHANGE ---

                                if (response.isSuccessful) {
                                    response.body()?.let { botResponse ->
                                        var botMessageTextForApi: String? = null
                                        var botRecipeForApi: dev.pedroayon.nutria.common.model.Recipe? = null

                                        if (botResponse.recipe != null) {
                                            val recipeMessage = ChatMessage(
                                                text = botResponse.recipe.toString(),
                                                messageType = MessageType.RECIPE,
                                                recipe = botResponse.recipe
                                            )
                                            messages.add(recipeMessage)
                                            botRecipeForApi = botResponse.recipe
                                            botMessageTextForApi = "Aquí tienes una receta: ${botResponse.recipe.name}"
                                        }
                                        if (botResponse.shoppingList != null) {
                                            shoppingListManager.saveShoppingList(botResponse.shoppingList)
                                            val shoppingListText = "Tu lista de compras actualizada:\n" +
                                                    botResponse.shoppingList.joinToString("\n") { "- $it" }
                                            messages.add(ChatMessage(text = shoppingListText, messageType = MessageType.BOT))
                                            if (botMessageTextForApi == null) botMessageTextForApi = shoppingListText else botMessageTextForApi += "\n" + shoppingListText
                                        }
                                        if (botResponse.message != null) {
                                            messages.add(ChatMessage(text = botResponse.message, messageType = MessageType.BOT))
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
                                    messages.add(ChatMessage(text = "Lo siento, ocurrió un error al procesar tu solicitud. Código: ${response.code()}", messageType = MessageType.BOT))
                                    apiChatHistory.add(Message(id = apiMessageIdCounter.getAndIncrement(), role = MessageRole.ASSISTANT, text = "Error response from server: $errorBody"))
                                }
                            } catch (e: Exception) {
                                Log.e("ChatScreenAPI", "Exception sending message: ${e.message}", e)
                                messages.add(ChatMessage(text = "Lo siento, no pude conectarme. Verifica tu conexión.", messageType = MessageType.BOT))
                            } finally {
                                isBotTyping = false
                            }
                        }
                    }
                },
                onCameraClick = {
                    Log.d("ChatScreen", "Camera icon clicked - implement image handling.")
                    coroutineScope.launch {
                        messages.add(ChatMessage(text = "La función de cámara aún no está implementada.", messageType = MessageType.BOT))
                    }
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
                                    // Revert UI state if action can't proceed
                                    val msgIndex = messages.indexOfFirst { it.id == msg.id }
                                    if (msgIndex != -1) messages[msgIndex] = msg.copy(isRecipeSavedInMemory = !shouldSave)
                                    return@launch
                                }

                                if (shouldSave) {
                                    try {
                                        // --- CRITICAL CHANGE: Pass userIdAsBearerToken here ---
                                        val response = ApiClient.instance.createRecipe(userIdAsBearerToken!!, recipeToToggle)
                                        // --- End CRITICAL CHANGE ---
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
                                            // --- CRITICAL CHANGE: Pass userIdAsBearerToken here ---
                                            val response = ApiClient.instance.deleteRecipe(userIdAsBearerToken!!, recipeId.toString())
                                            // --- End CRITICAL CHANGE ---
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