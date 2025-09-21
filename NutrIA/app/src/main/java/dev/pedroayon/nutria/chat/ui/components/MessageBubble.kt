package dev.pedroayon.nutria.chat.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row // Added for HeartSwitch alignment
import androidx.compose.foundation.layout.Spacer // Added for HeartSwitch alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // Added for HeartSwitch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meetup.twain.MarkdownText // Ensure this dependency is present
import com.popovanton0.heartswitch.HeartSwitch // Ensure this dependency is present
import dev.pedroayon.nutria.chat.domain.model.MessageType
import dev.pedroayon.nutria.common.model.Recipe // Use the common model Recipe
import dev.pedroayon.nutria.chat.domain.model.ChatMessage

@Composable
fun MessageBubble(
    message: ChatMessage,
    onRecipeSaveToggle: ((recipe: Recipe, shouldBeSaved: Boolean) -> Unit)? = null // Pass Recipe, not ChatMessage
) {
    val alignment = if (message.messageType == MessageType.USER) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleColor = if (message.messageType == MessageType.USER)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant // Slightly different color for bot/recipe for contrast

    val textColor = if (message.messageType == MessageType.USER) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val recipeForBubble: Recipe? = if (message.messageType == MessageType.RECIPE) message.recipe else null

    // The text to display. If it's a recipe and the recipe object exists, use its toString().
    // Otherwise, use the message's plain text.
    val displayText = if (message.messageType == MessageType.RECIPE && recipeForBubble != null) {
        recipeForBubble.toString() // This uses the custom Markdown toString()
    } else {
        message.text
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(
                start = if (message.messageType == MessageType.USER) 64.dp else 8.dp, // Indent user messages more
                end = if (message.messageType == MessageType.USER) 8.dp else 64.dp    // Indent bot messages more
            ),
        contentAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp), // Slightly more rounded
            color = bubbleColor,
            tonalElevation = 1.dp, // Softer elevation
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(if (recipeForBubble != null) 0.dp else 12.dp)) { // No padding for Column if recipe, recipe content has it
                if (recipeForBubble != null) {
                    // Recipe specific layout with HeartSwitch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp, start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Optional: Recipe Name as a separate Text element if not prominent in toString()
                        // Text(text = recipeForBubble.name, style = MaterialTheme.typography.titleMedium, color = textColor, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f)) // Pushes HeartSwitch to the end

                        // Use message.isRecipeSavedInMemory for the initial state
                        var isHeartChecked by remember(message.id, message.isRecipeSavedInMemory) {
                            mutableStateOf(message.isRecipeSavedInMemory)
                        }

                        HeartSwitch(
                            checked = isHeartChecked,
                            onCheckedChange = { newState ->
                                isHeartChecked = newState
                                onRecipeSaveToggle?.invoke(recipeForBubble, newState)
                                Log.d("MessageBubble", "HeartSwitch toggled for ${recipeForBubble.name} to $newState")
                            },
                            modifier = Modifier.size(36.dp) // Adjust size as needed
                        )
                    }
                }

                // MarkdownText for all message types (USER, BOT, RECIPE content)
                MarkdownText(
                    markdown = displayText,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp,
                        top = if (recipeForBubble != null) 4.dp else 0.dp // Less top padding if heart switch is there
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    // Consider adding onLinkClicked for handling links in Markdown if any
                )
            }
        }
    }
}