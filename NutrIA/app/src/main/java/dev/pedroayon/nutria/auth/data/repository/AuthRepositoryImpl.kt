package dev.pedroayon.nutria.auth.data.repository

import dev.pedroayon.nutria.auth.domain.model.Credentials
import dev.pedroayon.nutria.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl : AuthRepository {
    override fun checkUserLoginStatus(credentials: Credentials): Flow<Boolean> = flow {
        // Simulate network or computation delay
        delay(1000)
        // TODO: Implement real check against cache or backend
        emit(false)
    }

    override fun login(credentials: Credentials): Flow<Boolean> = flow {
        // TODO: Implement login logic (e.g., call api.login, save token)
        emit(false)
    }

    override fun signup(credentials: Credentials): Flow<Boolean> = flow {
        // TODO: Implement signup logic (e.g., call api.signup)
        emit(false)
    }
}
