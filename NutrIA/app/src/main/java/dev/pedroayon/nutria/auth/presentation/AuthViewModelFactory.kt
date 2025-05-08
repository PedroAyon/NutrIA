package dev.pedroayon.nutria.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.credentials.CredentialManager
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val context: Context // Needs context for CredentialManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, CredentialManager.create(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}