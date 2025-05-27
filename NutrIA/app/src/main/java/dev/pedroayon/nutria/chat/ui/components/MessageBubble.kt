package dev.pedroayon.nutria.chat.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.meetup.twain.MarkdownText
import com.popovanton0.heartswitch.HeartSwitch
import dev.pedroayon.nutria.auth.domain.model.MessageType
import dev.pedroayon.nutria.auth.domain.model.Recipe
import dev.pedroayon.nutria.chat.domain.model.ChatMessage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


@Composable
fun MessageBubble(
    message: ChatMessage,
    onRecipeSaveToggle: ((Recipe, Boolean) -> Unit)? = null
) {
    val alignment = if (message.messageType == MessageType.USER) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleColor = if (message.messageType == MessageType.USER)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surface

    val textColor = if (message.messageType == MessageType.USER)
        if (isSystemInDarkTheme()) Color.Black else Color.White
    else
        MaterialTheme.colorScheme.onSurface

    // Handle parsing only if RECIPE
    var parsedRecipe: Recipe? = null
    val displayText = if (message.messageType == MessageType.RECIPE) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val recipe = json.decodeFromString<Recipe>(message.text)
            parsedRecipe = recipe
            recipe.toString()
        } catch (e: SerializationException) {
            "Error displaying recipe: Invalid recipe format."
        } catch (e: Exception) {
            "An unexpected error occurred while displaying the recipe."
        }
    } else {
        message.text
    }

    Box(
        modifier = if (message.messageType == MessageType.USER) {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(start = 24.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(end = 24.dp)
        },
        contentAlignment = alignment
    ) {
        if (message.messageType != MessageType.RECIPE) {
            // 🟢 OLD LAYOUT for normal messages
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bubbleColor,
                tonalElevation = 2.dp
            ) {
                MarkdownText(
                    markdown = displayText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        } else {
            // 🟡 RECIPE LAYOUT with HeartSwitch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bubbleColor,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (parsedRecipe != null) {
                        var isRecipeSaved by remember { mutableStateOf(false) }

                        HeartSwitch(
                            checked = isRecipeSaved,
                            onCheckedChange = { newState ->
                                isRecipeSaved = newState
                                onRecipeSaveToggle?.invoke(parsedRecipe!!, newState)
                            },
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        MarkdownText(
                            markdown = displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
