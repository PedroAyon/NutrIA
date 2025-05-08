package dev.pedroayon.nutria.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dev.pedroayon.nutria.auth.domain.model.SignInResult

interface AuthRepository {
    fun getCurrentUser(): Flow<FirebaseUser?>

    fun authenticateWithFirebase(idToken: String): Flow<SignInResult>

    fun getGoogleSignInOptions(): GetGoogleIdOption

    fun logOut()
}