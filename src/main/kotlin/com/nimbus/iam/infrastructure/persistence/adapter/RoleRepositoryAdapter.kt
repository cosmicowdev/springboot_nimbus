package com.nimbus.iam.infrastructure.persistence.adapter

import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.port.RoleRepository
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toDomain
import com.nimbus.iam.infrastructure.persistence.mapper.DomainMapper.toEntity
import com.nimbus.iam.infrastructure.persistence.repository.RoleJpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
class RoleRepositoryAdapter(
    private val jpa: RoleJpaRepository
) : RoleRepository {

    override fun save(role: Role): Role = toDomain(jpa.save(toEntity(role)))

    override fun findById(id: UUID): Optional<Role> = jpa.findById(id).map(::toDomain)

    override fun findByName(name: String): Optional<Role> = jpa.findByName(name).map(::toDomain)

    override fun findAll(): List<Role> = jpa.findAll().map(::toDomain)

    override fun deleteById(id: UUID): Boolean = jpa.findById(id)
        .map { entity ->
            jpa.delete(entity)
            true
        }
        .orElse(false)

    override fun existsByName(name: String): Boolean = jpa.existsByName(name)
}
