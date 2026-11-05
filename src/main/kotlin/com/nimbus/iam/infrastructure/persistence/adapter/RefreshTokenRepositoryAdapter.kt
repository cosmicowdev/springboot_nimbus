package com.nimbus.iam.infrastructure.persistence.adapter

import com.nimbus.iam.domain.model.RefreshToken
import com.nimbus.iam.domain.port.RefreshTokenRepository
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toDomain
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toEntity
import com.nimbus.iam.infrastructure.persistence.repository.RefreshTokenJpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
class RefreshTokenRepositoryAdapter(
    private val jpa: RefreshTokenJpaRepository
) : RefreshTokenRepository {

    override fun save(token: RefreshToken): RefreshToken = toDomain(jpa.save(toEntity(token)))

    override fun findByHash(tokenHash: String): Optional<RefreshToken> =
        jpa.findByTokenHash(tokenHash).map(::toDomain)

    override fun revoke(id: UUID, revokedAt: Instant): Optional<RefreshToken> {
        jpa.revoke(id, revokedAt)
        jpa.flush()
        return jpa.findById(id).map(::toDomain)
    }

    override fun revokeAllForUser(userId: UUID, revokedAt: Instant): Int =
        jpa.revokeAllForUser(userId, revokedAt)

    override fun deleteExpiredBefore(before: Instant): Int =
        jpa.deleteExpiredBefore(before)
}
