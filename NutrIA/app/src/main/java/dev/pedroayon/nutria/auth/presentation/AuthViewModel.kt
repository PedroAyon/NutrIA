package dev.pedroayon.nutria.auth.presentation

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import dev.pedroayon.nutria.auth.domain.model.AuthState
import dev.pedroayon.nutria.auth.domain.model.SignInResult
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


private const val TAG = "AuthViewModel"


class AuthViewModel(
    private val authRepository: AuthRepository,
    private val credentialManager: CredentialManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.InitialCheck)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        authRepository.getCurrentUser()
            .onEach { user ->
                _currentUser.value = user
                _uiState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
            .launchIn(viewModelScope)
    }

    fun triggerGoogleSignIn(activity: Activity) {
        viewModelScope.launch {
            if (_uiState.value == AuthState.Idle || _uiState.value == AuthState.Unauthenticated || _uiState.value is AuthState.Error) {
                _uiState.value = AuthState.LoadingGoogle

                try {
                    val googleIdTokenOption = authRepository.getGoogleSignInOptions()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdTokenOption)
                        .build()

                    val result = credentialManager.getCredential(activity, request)
                    val credential = result.credential

                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        _uiState.value = AuthState.LoadingFirebase
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        authRepository.authenticateWithFirebase(googleIdTokenCredential.idToken)
                            .collect { firebaseSignInResult ->
                                when (firebaseSignInResult) {
                                    is SignInResult.Success -> _uiState.value =
                                        AuthState.Authenticated(firebaseSignInResult.user)

                                    is SignInResult.Error -> _uiState.value =
                                        AuthState.Error(firebaseSignInResult.exception)

                                    SignInResult.Loading -> {} // Already in LoadingFirebase
                                    SignInResult.Idle -> {}
                                }
                            }
                    } else {
                        val errorMessage =
                            "Received unexpected credential type: ${credential::class.java.name}"
                        _uiState.value = AuthState.Error(Exception(errorMessage))
                        Log.e(TAG, errorMessage)
                    }

                } catch (e: GetCredentialException) {
                    Log.e(TAG, "Credential Manager GetCredentialException", e)
                    _uiState.value = AuthState.Error(e)
                } catch (e: Exception) {
                    Log.e(TAG, "General error during sign in process", e)
                    if (_uiState.value !is AuthState.Error) {
                        _uiState.value = AuthState.Error(e)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logOut()
            _uiState.value = AuthState.Unauthenticated
        }
    }

    fun resetUiState() {
        _uiState.value = _currentUser.value?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
    }
}