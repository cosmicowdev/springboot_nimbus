package com.nimbus.iam.domain.port

import com.nimbus.iam.domain.model.RefreshToken
import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findAll(page: Int, size: Int, status: UserStatus?): PageResult<User>
    fun updateStatus(id: UUID, status: UserStatus, updatedAt: Instant): Optional<User>
    fun updateLastLogin(id: UUID, lastLoginAt: Instant): Optional<User>
    fun deleteById(id: UUID): Boolean
    fun existsByEmail(email: String): Boolean
}

interface RoleRepository {
    fun save(role: Role): Role
    fun findById(id: UUID): Optional<Role>
    fun findByName(name: String): Optional<Role>
    fun findAll(): List<Role>
    fun deleteById(id: UUID): Boolean
    fun existsByName(name: String): Boolean
}

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findByHash(tokenHash: String): Optional<RefreshToken>
    fun revoke(id: UUID, revokedAt: Instant): Optional<RefreshToken>
    fun revokeAllForUser(userId: UUID, revokedAt: Instant): Int
    fun deleteExpiredBefore(before: Instant): Int
}

interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun matches(rawPassword: String, hashed: String): Boolean
}

interface TokenSigner {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(): String
    fun hashToken(rawToken: String): String
    fun validateAccessToken(token: String): AccessTokenClaims
    fun extractUserId(accessToken: String): UUID
}

data class AccessTokenClaims(
    val userId: UUID,
    val email: String,
    val authorities: Set<String>,
    val expiresAt: Instant
)

data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    val hasNext: Boolean get() = page + 1 < totalPages
    val hasPrevious: Boolean get() = page > 0
}
