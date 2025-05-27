package dev.pedroayon.nutria.common.ui.navigation

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

    data object RecipeBook : ScreenDestination(
        AppDestinations.RECIPE_BOOK,
        R.string.recipes,
        R.drawable.book_20px
    )

    data object Recipe : ScreenDestination(
        AppDestinations.RECIPE,
        R.string.recipes,
        R.drawable.book_20px
    )

    data object ShoppingList: ScreenDestination(
        AppDestinations.SHOPPING_LIST,
        R.string.shopping_list,
        R.drawable.shopping_cart_24px
    )
}