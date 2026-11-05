package com.nimbus.iam.infrastructure.persistence.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id val id: UUID,
    @Column(nullable = false, unique = true) val email: String,
    @Column(name = "password_hash", nullable = false) val passwordHash: String,
    @Column(name = "full_name", nullable = false) val fullName: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val status: UserStatusEntity,
    @Column(name = "email_verified", nullable = false) val emailVerified: Boolean,
    @Column(name = "last_login_at") val lastLoginAt: Instant? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<RoleEntity> = mutableSetOf(),
    @Column(name = "created_at", nullable = false) val createdAt: Instant,
    @Column(name = "updated_at", nullable = false) val updatedAt: Instant
)

enum class UserStatusEntity { PENDING, ACTIVE, SUSPENDED, DEACTIVATED }

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id val id: UUID,
    @Column(nullable = false, unique = true) val name: String,
    val description: String? = null,
    @Column(name = "is_system", nullable = false) val isSystem: Boolean,
    @Column(name = "created_at", nullable = false) val createdAt: Instant,
    @Column(name = "updated_at", nullable = false) val updatedAt: Instant
)

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true) val tokenHash: String,
    @Column(name = "expires_at", nullable = false) val expiresAt: Instant,
    @Column(name = "revoked_at") val revokedAt: Instant? = null,
    @Column(name = "created_at", nullable = false) val createdAt: Instant
)
