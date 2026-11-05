package com.nimbus.iam.application.dto

import com.nimbus.iam.domain.model.AuthTokens
import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import java.time.Instant
import java.util.UUID

data class RegisterCommand(
    val email: String,
    val rawPassword: String,
    val fullName: String
)

data class LoginCommand(
    val email: String,
    val rawPassword: String
)

data class CreateUserCommand(
    val email: String,
    val rawPassword: String,
    val fullName: String,
    val status: UserStatus,
    val roleNames: Set<String>
)

data class UpdateUserCommand(
    val fullName: String?,
    val status: UserStatus?,
    val roleNames: Set<String>?
)

data class UpdateProfileCommand(
    val fullName: String?
)

data class ChangePasswordCommand(
    val currentPassword: String,
    val newPassword: String
)

data class CreateRoleCommand(
    val name: String,
    val description: String?
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
) {
    companion object {
        fun from(tokens: AuthTokens) = TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            tokenType = tokens.tokenType,
            expiresIn = tokens.expiresIn
        )
    }
}

data class UserSummary(
    val id: UUID,
    val email: String,
    val fullName: String,
    val status: UserStatus,
    val emailVerified: Boolean,
    val lastLoginAt: Instant?
) {
    companion object {
        fun from(user: User) = UserSummary(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            status = user.status,
            emailVerified = user.emailVerified,
            lastLoginAt = user.lastLoginAt
        )
    }
}

data class UserDetail(
    val id: UUID,
    val email: String,
    val fullName: String,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: Set<RoleDto>,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(user: User) = UserDetail(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            status = user.status,
            emailVerified = user.emailVerified,
            roles = user.roles.map(RoleDto::from).toSet(),
            lastLoginAt = user.lastLoginAt,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}

data class RoleDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val isSystem: Boolean
) {
    companion object {
        fun from(role: Role) = RoleDto(
            id = role.id,
            name = role.name,
            description = role.description,
            isSystem = role.isSystem
        )
    }
}
