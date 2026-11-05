package com.nimbus.iam.infrastructure.persistence.repository

import com.nimbus.iam.infrastructure.persistence.entity.RoleEntity
import com.nimbus.iam.infrastructure.persistence.entity.UserEntity
import com.nimbus.iam.infrastructure.persistence.entity.UserStatusEntity
import com.nimbus.iam.infrastructure.persistence.entity.RefreshTokenEntity
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM UserEntity u WHERE (:status IS NULL OR u.status = :status)")
    fun findAllByStatus(@Param("status") status: UserStatusEntity?, pageable: Pageable): Page<UserEntity>

    @Modifying
    @Query("UPDATE UserEntity u SET u.status = :status, u.updatedAt = :updatedAt WHERE u.id = :id")
    fun updateStatus(@Param("id") id: UUID, @Param("status") status: UserStatusEntity, @Param("updatedAt") updatedAt: Instant): Int

    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :id")
    fun updateLastLogin(@Param("id") id: UUID, @Param("lastLoginAt") lastLoginAt: Instant): Int
}

interface RoleJpaRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: String): Optional<RoleEntity>
    fun existsByName(name: String): Boolean
}

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshTokenEntity>

    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :revokedAt WHERE t.id = :id AND t.revokedAt IS NULL")
    fun revoke(@Param("id") id: UUID, @Param("revokedAt") revokedAt: Instant): Int

    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :revokedAt WHERE t.userId = :userId AND t.revokedAt IS NULL")
    fun revokeAllForUser(@Param("userId") userId: UUID, @Param("revokedAt") revokedAt: Instant): Int

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :before AND t.revokedAt IS NOT NULL")
    fun deleteExpiredBefore(@Param("before") before: Instant): Int
}
