package dev.pedroayon.nutria.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.auth.domain.model.MessageType
import dev.pedroayon.nutria.chat.domain.model.ChatMessage
import dev.pedroayon.nutria.chat.ui.components.ChatBottomBar
import dev.pedroayon.nutria.chat.ui.components.MessageBubble
import dev.pedroayon.nutria.common.ui.components.CommonTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen() {
    val hintText = stringResource(id = R.string.chat_hint)
    val coroutineScope = rememberCoroutineScope()
    var isBotTyping by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf("") }
    val messages = remember {
        // mockup data
        val recipeJsonString = """
        {
            "calories": 450,
            "description": "Un plato nutritivo y fácil de preparar, ideal para una comida rápida y saludable.",
            "ingredients": [
                {
                    "name": "Espinacas",
                    "quantity": "2",
                    "unit": "tazas"
                },
                {
                    "name": "Tomates",
                    "quantity": "1",
                    "unit": "mediano"
                },
                {
                    "name": "Huevos",
                    "quantity": "2",
                    "unit": "grandes"
                },
                {
                    "name": "Hummus",
                    "quantity": "2",
                    "unit": "cucharadas"
                },
                {
                    "name": "Pan de linaza germinado",
                    "quantity": "2",
                    "unit": "rebanadas"
                },
                {
                    "name": "Queso Mozzarella",
                    "quantity": "30",
                    "unit": "gramos"
                }
            ],
            "instructions": [
                {
                    "description": "Prepara los ingredientes",
                    "instructions": "Lava y corta los tomates en rodajas. Lava las espinacas. Ralla el queso mozzarella.",
                    "step": 1,
                    "duration": "5 minutos"
                },
                {
                    "description": "Cocina los huevos",
                    "instructions": "Bate los huevos y cocínalos en una sartén antiadherente hasta que estén revueltos o en forma de tortilla, según tu preferencia.",
                    "step": 2,
                    "duration": "5 minutos"
                },
                {
                    "description": "Tuesta el pan",
                    "instructions": "Mientras se cocinan los huevos, tuesta las rebanadas de pan de linaza germinado.",
                    "step": 3,
                    "duration": "3 minutos"
                },
                {
                    "description": "Arma el sándwich",
                    "instructions": "Unta hummus en ambas rebanadas de pan tostado. Coloca las espinacas, las rodajas de tomate y los huevos cocidos sobre una de las rebanadas. Espolvorea el queso mozzarella rallado por encima. Cubre con la otra rebanada de pan.",
                    "step": 4,
                    "duration": "2 minutos"
                },
                {
                    "description": "Sirve",
                    "instructions": "Corta el sándwich por la mitad si lo deseas y sírvelo inmediatamente.",
                    "step": 5,
                    "duration": "1 minuto"
                }
            ],
            "name": "Sándwich Saludable de Huevo, Espinacas y Hummus",
            "prepTime": "15 minutos"
        }
    """
        mutableStateListOf(
            ChatMessage(
                text = "¿Quién eres? ¿Qué puedes hacer?".trim(),
                messageType = MessageType.USER
            ),
            ChatMessage(
                text = "Soy NutrIA, tu asistente de nutrición y cocina. Puedo responder preguntas sobre nutrición, sugerir recetas saludables incluso con los ingredientes que tengas, o extraer los ingredientes de una foto, como el contenido de tu nevera o despensa. ¡Estoy aquí para ayudarte a comer mejor y de forma más fácil!\n".trim(),
                messageType = MessageType.BOT
            ),
            ChatMessage(
                text = "¿Puedo usar espinaca en lugar de lechuga en una ensalada?".trim(),
                messageType = MessageType.USER
            ),
            ChatMessage(
                text = "¡Por supuesto! La espinaca es una excelente alternativa a la lechuga en una ensalada. De hecho, es más nutritiva que muchos tipos de lechuga, ya que es rica en vitaminas y minerales. ¡Anímate a probarla!\n".trim(),
                messageType = MessageType.BOT
            ),
            ChatMessage(
                text = "No se que preparar hoy, recomiéndame algo, te mando foto de mi refri y mi alacena".trim(),
                messageType = MessageType.USER
            ),
            ChatMessage(
                text = recipeJsonString.trim(),
                messageType = MessageType.RECIPE
            ),
        )
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
                        val newUserMessage = ChatMessage(currentInput, messageType = MessageType.USER)
                        messages.add(newUserMessage)
                        val userInput = currentInput
                        currentInput = ""
                        isBotTyping = true

                        coroutineScope.launch {
                            delay(5000)
                            val botReply = ChatMessage("bot response", messageType = MessageType.BOT)
                            messages.add(botReply)
                            isBotTyping = false
                        }
                    }
                }


,
                onCameraClick = {},
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
                items(messages) { msg ->
                    MessageBubble(message = msg)
                }
                if (isBotTyping) {
                    item {
                        WritingAnimation()
                    }
                }}

            // Desplaza automáticamente al final cuando se agregan mensajes
            LaunchedEffect(messages.size) {
                listState.scrollToItem(index = messages.size - 1, scrollOffset = Int.MAX_VALUE)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.size - 1)
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
        modifier = modifier.size(72.dp)
    )
}

@Preview
@Composable
fun AnimationTestScreen() {
    WritingAnimation()
}