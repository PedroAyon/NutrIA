package dev.pedroayon.nutria.auth.domain.model

/**
 * Simple data class holding authentication credentials.
 * @property userId unique identifier for the user (e.g. email or username).
 * @property hashedPassword password hashed on the client before sending or storing.
 */
data class Credentials(
    val userId: Int,
    val hashedPassword: String
)