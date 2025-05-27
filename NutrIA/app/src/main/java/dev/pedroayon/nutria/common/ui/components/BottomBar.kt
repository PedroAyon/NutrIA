package dev.pedroayon.nutria.common.ui.components

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Teleport
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.items.dropletbutton.DropletButton
import dev.pedroayon.nutria.common.ui.navigation.ScreenDestination

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        ScreenDestination.Chat,
        ScreenDestination.RecipeBook,
        ScreenDestination.ShoppingList
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

//    NavigationBar {
//        items.forEach { screen ->
//            NavigationBarItem(
//                icon = screen.icon,
//                label = { Text(stringResource(id = screen.labelRes)) },
//                selected = currentRoute == screen.route,
//                onClick = {
//                    navController.navigate(screen.route) {
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                }
//            )
//        }
//    }

    var selectedItem by remember { mutableIntStateOf(0) }

    AnimatedNavigationBar(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(70.dp),
        selectedIndex = selectedItem,
        ballColor = MaterialTheme.colorScheme.primary,
//        barColor = MaterialTheme.colorScheme.surfaceVariant,
        barColor = MaterialTheme.colorScheme.surfaceVariant,
//        cornerRadius = shapeCornerRadius(25.dp),
        ballAnimation = Teleport(tween(Duration, easing = LinearEasing)),
        indentAnimation = Height(
            indentWidth = 56.dp,
            indentHeight = 15.dp,
            animationSpec = tween(
                DoubleDuration,
                easing = { OvershootInterpolator().getInterpolation(it) })
        )
    ) {
        items.forEachIndexed { index, it ->
            DropletButton(
                modifier = Modifier.fillMaxSize(),
                isSelected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(it.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = it.icon,
                iconColor = MaterialTheme.colorScheme.secondary,
                dropletColor = MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = Duration, easing = LinearEasing)
            )
        }
    }
}

const val Duration = 500
const val DoubleDuration = 1000