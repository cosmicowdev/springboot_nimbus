package com.nimbus.iam.infrastructure.persistence.mapper

import com.nimbus.iam.domain.model.RefreshToken
import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.infrastructure.persistence.entity.RefreshTokenEntity
import com.nimbus.iam.infrastructure.persistence.entity.RoleEntity
import com.nimbus.iam.infrastructure.persistence.entity.UserEntity
import com.nimbus.iam.infrastructure.persistence.entity.UserStatusEntity

object DomainMapper {

    fun toDomain(entity: UserEntity): User = User(
        id = entity.id,
        email = entity.email,
        passwordHash = entity.passwordHash,
        fullName = entity.fullName,
        status = entity.status.toDomain(),
        emailVerified = entity.emailVerified,
        roles = entity.roles.map(::toDomain).toSet(),
        lastLoginAt = entity.lastLoginAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toDomain(entity: RoleEntity): Role = Role(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        isSystem = entity.isSystem,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toDomain(entity: RefreshTokenEntity): RefreshToken = RefreshToken(
        id = entity.id,
        userId = entity.userId,
        tokenHash = entity.tokenHash,
        expiresAt = entity.expiresAt,
        revokedAt = entity.revokedAt,
        createdAt = entity.createdAt
    )

    fun toEntity(user: User): UserEntity = UserEntity(
        id = user.id,
        email = user.email,
        passwordHash = user.passwordHash,
        fullName = user.fullName,
        status = user.status.toEntity(),
        emailVerified = user.emailVerified,
        lastLoginAt = user.lastLoginAt,
        roles = user.roles.map(::toEntity).toMutableSet(),
        createdAt = user.createdAt,
        updatedAt = user.updatedAt
    )

    fun toEntity(role: Role): RoleEntity = RoleEntity(
        id = role.id,
        name = role.name,
        description = role.description,
        isSystem = role.isSystem,
        createdAt = role.createdAt,
        updatedAt = role.updatedAt
    )

    fun toEntity(token: RefreshToken): RefreshTokenEntity = RefreshTokenEntity(
        id = token.id,
        userId = token.userId,
        tokenHash = token.tokenHash,
        expiresAt = token.expiresAt,
        revokedAt = token.revokedAt,
        createdAt = token.createdAt
    )

    private fun UserStatusEntity.toDomain(): UserStatus = UserStatus.valueOf(this.name)
    private fun UserStatus.toEntity(): UserStatusEntity = UserStatusEntity.valueOf(this.name)
}
