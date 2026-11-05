package com.nimbus.iam.domain.model

import java.time.Instant
import java.util.UUID

enum class UserStatus {
    PENDING, ACTIVE, SUSPENDED, DEACTIVATED
}

data class Role(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val isSystem: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: Set<Role> = emptySet(),
    val lastLoginAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val authorities: Set<String>
        get() = roles.map { it.name }.toSet()

    fun isActive(): Boolean = status == UserStatus.ACTIVE

    fun canAuthenticate(): Boolean =
        status == UserStatus.ACTIVE && emailVerified
}

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val revokedAt: Instant? = null,
    val createdAt: Instant
) {
    fun isRevoked(): Boolean = revokedAt != null

    fun isExpired(now: Instant = Instant.now()): Boolean = now.isAfter(expiresAt)

    fun isValid(now: Instant = Instant.now()): Boolean = !isRevoked() && !isExpired(now)
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
