package com.nimbus.iam.application.service

import com.nimbus.iam.application.dto.ChangePasswordCommand
import com.nimbus.iam.application.dto.CreateUserCommand
import com.nimbus.iam.application.dto.UpdateProfileCommand
import com.nimbus.iam.application.dto.UpdateUserCommand
import com.nimbus.iam.application.dto.UserDetail
import com.nimbus.iam.application.dto.UserSummary
import com.nimbus.iam.domain.exception.PasswordMismatchException
import com.nimbus.iam.domain.exception.RoleNotFoundException
import com.nimbus.iam.domain.exception.UserAlreadyExistsException
import com.nimbus.iam.domain.exception.UserNotFoundException
import com.nimbus.iam.domain.exception.WeakPasswordException
import com.nimbus.iam.domain.model.Role
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.domain.port.PageResult
import com.nimbus.iam.domain.port.PasswordHasher
import com.nimbus.iam.domain.port.RoleRepository
import com.nimbus.iam.domain.port.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordHasher: PasswordHasher
) {
    fun create(command: CreateUserCommand): UserDetail {
        if (userRepository.existsByEmail(command.email)) {
            throw UserAlreadyExistsException(command.email)
        }
        validatePasswordStrength(command.rawPassword)
        val now = Instant.now()
        val roles = resolveRoles(command.roleNames)
        val user = User(
            id = UUID.randomUUID(),
            email = command.email,
            passwordHash = passwordHasher.hash(command.rawPassword),
            fullName = command.fullName,
            status = command.status,
            emailVerified = command.status == UserStatus.ACTIVE,
            roles = roles,
            createdAt = now,
            updatedAt = now
        )
        return UserDetail.from(userRepository.save(user))
    }

    fun findById(id: UUID): UserDetail {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id.toString()) }
        return UserDetail.from(user)
    }

    fun findAll(page: Int, size: Int, status: UserStatus?): PageResult<UserSummary> {
        val result = userRepository.findAll(page, size, status)
        return PageResult(
            items = result.items.map(UserSummary::from),
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    fun update(id: UUID, command: UpdateUserCommand): UserDetail {
        val existing = userRepository.findById(id).orElseThrow { UserNotFoundException(id.toString()) }
        val now = Instant.now()
        val updated = existing.copy(
            fullName = command.fullName ?: existing.fullName,
            status = command.status ?: existing.status,
            roles = command.roleNames?.let { resolveRoles(it) } ?: existing.roles,
            updatedAt = now
        )
        return UserDetail.from(userRepository.save(updated))
    }

    fun updateStatus(id: UUID, status: UserStatus): UserDetail {
        val updated = userRepository.updateStatus(id, status, Instant.now())
            .orElseThrow { UserNotFoundException(id.toString()) }
        return UserDetail.from(updated)
    }

    fun delete(id: UUID) {
        if (!userRepository.deleteById(id)) {
            throw UserNotFoundException(id.toString())
        }
    }

    fun updateProfile(userId: UUID, command: UpdateProfileCommand): UserDetail {
        val existing = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId.toString()) }
        val updated = existing.copy(
            fullName = command.fullName ?: existing.fullName,
            updatedAt = Instant.now()
        )
        return UserDetail.from(userRepository.save(updated))
    }

    fun changePassword(userId: UUID, command: ChangePasswordCommand) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId.toString()) }
        if (!passwordHasher.matches(command.currentPassword, user.passwordHash)) {
            throw PasswordMismatchException()
        }
        validatePasswordStrength(command.newPassword)
        val updated = user.copy(
            passwordHash = passwordHasher.hash(command.newPassword),
            updatedAt = Instant.now()
        )
        userRepository.save(updated)
    }

    private fun resolveRoles(names: Set<String>): Set<Role> =
        names.map { name ->
            roleRepository.findByName(name).orElseThrow { RoleNotFoundException(name) }
        }.toSet()

    private fun validatePasswordStrength(password: String) {
        if (password.length < 8) {
            throw WeakPasswordException("must be at least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            throw WeakPasswordException("must contain at least one uppercase letter")
        }
        if (!password.any { it.isDigit() }) {
            throw WeakPasswordException("must contain at least one digit")
        }
    }
}
