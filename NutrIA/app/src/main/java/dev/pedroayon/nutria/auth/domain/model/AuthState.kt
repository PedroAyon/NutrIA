package dev.pedroayon.nutria.auth.domain.model

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    data object InitialCheck : AuthState()
    data object Idle : AuthState()
    data object LoadingGoogle : AuthState()
    data object LoadingFirebase : AuthState()
    data class Authenticated(val user: FirebaseUser?) : AuthState()
    data class Error(val exception: Throwable?) : AuthState()
    data object Unauthenticated : AuthState()
}
