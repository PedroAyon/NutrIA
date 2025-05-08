package dev.pedroayon.nutria.auth.data.repository

import android.content.Context
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.auth.domain.model.SignInResult
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context // Application context
) : AuthRepository {

    override fun getCurrentUser(): Flow<FirebaseUser?> {
        return flowOf(firebaseAuth.currentUser)
    }

    override fun getGoogleSignInOptions(): GetGoogleIdOption {
        return GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
    }

    override fun authenticateWithFirebase(idToken: String): Flow<SignInResult> = callbackFlow {
        send(SignInResult.Loading)
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            send(SignInResult.Success(authResult.user))
        } catch (e: Exception) {
            send(SignInResult.Error(e))
        }
        awaitClose { }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }
}