package com.nimbus.iam.interfaces.api.dto

import com.nimbus.iam.application.dto.ChangePasswordCommand
import com.nimbus.iam.application.dto.CreateRoleCommand
import com.nimbus.iam.application.dto.CreateUserCommand
import com.nimbus.iam.application.dto.LoginCommand
import com.nimbus.iam.application.dto.RegisterCommand
import com.nimbus.iam.application.dto.UpdateProfileCommand
import com.nimbus.iam.application.dto.UpdateUserCommand
import com.nimbus.iam.domain.model.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val password: String,
    @field:NotBlank @field:Size(max = 255) val fullName: String
) {
    fun toCommand() = RegisterCommand(email, password, fullName)
}

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
) {
    fun toCommand() = LoginCommand(email, password)
}

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class LogoutRequest(
    @field:NotBlank val refreshToken: String
)

data class UpdateProfileRequest(
    @field:Size(max = 255) val fullName: String?
) {
    fun toCommand() = UpdateProfileCommand(fullName)
}

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val newPassword: String
) {
    fun toCommand() = ChangePasswordCommand(currentPassword, newPassword)
}

data class CreateUserRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val password: String,
    @field:NotBlank @field:Size(max = 255) val fullName: String,
    @field:NotNull val status: UserStatus,
    @field:NotNull val roleNames: Set<@NotBlank String>
) {
    fun toCommand() = CreateUserCommand(email, password, fullName, status, roleNames)
}

data class UpdateUserRequest(
    @field:Size(max = 255) val fullName: String?,
    val status: UserStatus?,
    val roleNames: Set<@NotBlank String>?
) {
    fun toCommand() = UpdateUserCommand(fullName, status, roleNames)
}

data class CreateRoleRequest(
    @field:NotBlank @field:Size(max = 64) val name: String,
    @field:Size(max = 255) val description: String?
) {
    fun toCommand() = CreateRoleCommand(name, description)
}
