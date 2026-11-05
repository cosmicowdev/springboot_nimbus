package com.nimbus.iam.application.service

import com.nimbus.iam.application.dto.CreateRoleCommand
import com.nimbus.iam.application.dto.RoleDto
import com.nimbus.iam.domain.exception.RoleAlreadyExistsException
import com.nimbus.iam.domain.exception.RoleNotFoundException
import com.nimbus.iam.domain.exception.SystemRoleMutationException
import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.port.RoleRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    fun create(command: CreateRoleCommand): RoleDto {
        if (roleRepository.existsByName(command.name)) {
            throw RoleAlreadyExistsException(command.name)
        }
        val now = Instant.now()
        val role = Role(
            id = java.util.UUID.randomUUID(),
            name = command.name,
            description = command.description,
            isSystem = false,
            createdAt = now,
            updatedAt = now
        )
        return RoleDto.from(roleRepository.save(role))
    }

    fun findAll(): List<RoleDto> = roleRepository.findAll().map(RoleDto::from)

    fun delete(id: java.util.UUID) {
        val role = roleRepository.findById(id).orElseThrow { RoleNotFoundException(id.toString()) }
        if (role.isSystem) {
            throw SystemRoleMutationException(role.id)
        }
        roleRepository.deleteById(id)
    }
}
