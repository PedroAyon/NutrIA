package dev.pedroayon.nutria.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.pedroayon.nutria.R

sealed class ScreenDestination(
    val route: String,
    val labelRes: Int,
    val icon: Int
) {
    data object Chat : ScreenDestination(
        AppDestinations.CHAT,
        R.string.chat,
        R.drawable.chat_24px
    )

    data object Recipes : ScreenDestination(
        AppDestinations.RECIPES,
        R.string.recipes,
        R.drawable.book_20px
    )

    data object ShoppingList: ScreenDestination(
        AppDestinations.SHOPPING_LIST,
        R.string.shopping_list,
        R.drawable.shopping_cart_24px
    )
}