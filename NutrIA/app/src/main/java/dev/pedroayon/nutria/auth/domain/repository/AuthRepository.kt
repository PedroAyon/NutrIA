package dev.pedroayon.nutria.auth.domain.repository

import dev.pedroayon.nutria.auth.domain.model.Credentials
import kotlinx.coroutines.flow.Flow


/**
 * Defines the contract for an authentication repository using Kotlin Flow.
 * All operations return a Flow<Boolean> to emit async results.
 */
interface AuthRepository {
    /**
     * Checks whether the given credentials correspond to a logged-in user.
     * Emits `true` if the user is logged in or credentials are valid, `false` otherwise.
     */
    fun checkUserLoginStatus(credentials: Credentials): Flow<Boolean>

    /**
     * Attempts to log in with the provided credentials.
     * Emits `true` on successful login, `false` on failure.
     */
    fun login(credentials: Credentials): Flow<Boolean>

    /**
     * Attempts to sign up a new user with the provided credentials.
     * Emits `true` on successful registration, `false` on failure.
     */
    fun signup(credentials: Credentials): Flow<Boolean>
}
