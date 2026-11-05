package com.nimbus.iam.infrastructure.persistence.adapter

import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.domain.port.PageResult
import com.nimbus.iam.domain.port.UserRepository
import com.nimbus.iam.infrastructure.persistence.entity.UserStatusEntity
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toDomain
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toEntity
import com.nimbus.iam.infrastructure.persistence.repository.UserJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val jpa: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        val saved = jpa.save(toEntity(user))
        return toDomain(saved)
    }

    override fun findById(id: UUID): Optional<User> = jpa.findById(id).map(::toDomain)

    override fun findByEmail(email: String): Optional<User> = jpa.findByEmail(email).map(::toDomain)

    override fun findAll(page: Int, size: Int, status: UserStatus?): PageResult<User> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val domainStatus = status?.let { UserStatusEntity.valueOf(it.name) }
        val result = jpa.findAllByStatus(domainStatus, pageable)
        return PageResult(
            items = result.content.map(::toDomain),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    override fun updateStatus(id: UUID, status: UserStatus, updatedAt: Instant): Optional<User> {
        jpa.updateStatus(id, UserStatusEntity.valueOf(status.name), updatedAt)
        jpa.flush()
        return jpa.findById(id).map(::toDomain)
    }

    override fun updateLastLogin(id: UUID, lastLoginAt: Instant): Optional<User> {
        jpa.updateLastLogin(id, lastLoginAt)
        jpa.flush()
        return jpa.findById(id).map(::toDomain)
    }

    override fun deleteById(id: UUID): Boolean = jpa.findById(id)
        .map { entity ->
            jpa.delete(entity)
            true
        }
        .orElse(false)

    override fun existsByEmail(email: String): Boolean = jpa.existsByEmail(email)
}
