package dev.pedroayon.nutria.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import dev.pedroayon.nutria.R

sealed class ScreenDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit
) {
    data object Chat : ScreenDestination(
        AppDestinations.CHAT,
        R.string.chat,
        { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) }
    )

    data object WeeklyPlan : ScreenDestination(
        AppDestinations.WEEKLY,
        R.string.weekly_plan,
        { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
    )

    data object Recipes : ScreenDestination(
        AppDestinations.RECIPES,
        R.string.recipes,
        { Icon(Icons.Default.Book, contentDescription = null) }
    )

    data object Account : ScreenDestination(
        AppDestinations.ACCOUNT,
        R.string.account_settings,
        { Icon(Icons.Default.Person, contentDescription = null) }
    )
}