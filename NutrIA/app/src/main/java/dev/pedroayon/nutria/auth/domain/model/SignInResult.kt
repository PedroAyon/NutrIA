package dev.pedroayon.nutria.auth.domain.model

import com.google.firebase.auth.FirebaseUser

sealed class SignInResult {
    data class Success(val user: FirebaseUser?) : SignInResult()
    data class Error(val exception: Throwable?) : SignInResult()
    data object Loading : SignInResult()
    data object Idle : SignInResult()
}