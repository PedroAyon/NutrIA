package dev.pedroayon.nutria.auth.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dev.pedroayon.nutria.auth.data.repository.AuthRepositoryImpl
import dev.pedroayon.nutria.auth.domain.model.AuthState
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository
import dev.pedroayon.nutria.auth.presentation.AuthViewModel
import dev.pedroayon.nutria.auth.presentation.AuthViewModelFactory
import dev.pedroayon.nutria.core.ui.components.SplashScreen
import dev.pedroayon.nutria.core.ui.navigation.AppNavHost
import dev.pedroayon.nutria.core.ui.theme.NutriIATheme


class AuthActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepositoryImpl(FirebaseAuth.getInstance(), applicationContext)

        setContent {
            NutriIATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val uiState by authViewModel.uiState.collectAsState()

                    LaunchedEffect(uiState) {
                        when (uiState) {
                            is AuthState.Authenticated -> {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                            AuthState.Unauthenticated, AuthState.Idle -> {
                                if (navController.currentDestination?.route == "splash") {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                            is AuthState.Error -> {
                                val exception = (uiState as AuthState.Error).exception
                                Toast.makeText(
                                    this@AuthActivity,
                                    "Error: ${exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                authViewModel.resetUiState()
                                if (navController.currentDestination?.route == "splash") {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                            AuthState.InitialCheck, AuthState.LoadingGoogle, AuthState.LoadingFirebase -> {
                                // Stay on current screen
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen()
                        }
                        composable("login") {
                            LoginScreen(viewModel = authViewModel)
                        }
                        composable("home") {
                            AppNavHost()
                        }
                    }
                }
            }
        }
    }
}
