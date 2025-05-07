package dev.pedroayon.nutria.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.pedroayon.nutria.auth.data.repository.AuthRepositoryImpl
import dev.pedroayon.nutria.auth.domain.model.AuthState
import dev.pedroayon.nutria.auth.domain.model.Credentials
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository
import dev.pedroayon.nutria.core.ui.components.LoadingScreen

@Composable
fun AppEntryHost(
    authRepository: AuthRepository = AuthRepositoryImpl()
) {

    var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }
    LaunchedEffect(Unit) {
        val dummyCredentials = Credentials(userId = 1, hashedPassword = "")
        authRepository.checkUserLoginStatus(dummyCredentials).collect { isLoggedIn ->
            authState = if (isLoggedIn) AuthState.Authenticated else AuthState.Unauthenticated
        }
    }

    when (authState) {
        AuthState.Loading -> {
            LoadingScreen("Checking login status")
        }
        AuthState.Authenticated -> {
            AppNavHost()
        }
        AuthState.Unauthenticated -> {
            AuthNavHost(
                onAuthenticationComplete = {
                    authState = AuthState.Authenticated
                }
            )
        }
    }
}