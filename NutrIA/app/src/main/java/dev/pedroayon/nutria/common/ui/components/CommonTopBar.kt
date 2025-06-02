package dev.pedroayon.nutria.common.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.common.ui.theme.backgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = title,
        actions = {
            actions()
            // Default "More" menu
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                val context = LocalContext.current

                DropdownMenuItem(
                    text = { Text("Juego") },
                    onClick = {
                        showMenu = false
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://exact-secondly-perch.ngrok-free.app/"))
                        context.startActivity(intent)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.logout)) },
                    onClick = {
                        showMenu = false
                        // Handle logout here
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    }
                )

            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}
