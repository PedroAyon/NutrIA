package dev.pedroayon.nutria.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.pedroayon.nutria.auth.ui.LoginScreen
import dev.pedroayon.nutria.auth.ui.ProfileFormScreen
import dev.pedroayon.nutria.auth.ui.SignupScreen

@Composable
fun AuthNavHost(onAuthenticationComplete: () -> Unit) {
    val authNavController = rememberNavController()

    NavHost(
        navController = authNavController,
        startDestination = AuthDestinations.LOGIN
    ) {
        composable(AuthDestinations.LOGIN) {
            LoginScreen(
                onLogin = {
                    onAuthenticationComplete()
                },
                onNavigateToSignup = {
                    authNavController.navigate(AuthDestinations.SIGN_UP)
                }
            )
        }
        composable(AuthDestinations.SIGN_UP) {
            SignupScreen(
                onSignupComplete = {
                    authNavController.navigate(AuthDestinations.PROFILE_FORM)
                },
                onNavigateBackToLogin = {
                    authNavController.popBackStack()
                }
            )
        }
        composable(AuthDestinations.PROFILE_FORM) {
            ProfileFormScreen(
                onComplete = {
                    onAuthenticationComplete()
                }
            )
        }
    }
}