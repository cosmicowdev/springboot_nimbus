package com.nimbus.iam.interfaces.api.dto

import com.nimbus.iam.application.dto.RoleDto
import com.nimbus.iam.application.dto.TokenResponse
import com.nimbus.iam.application.dto.UserDetail
import com.nimbus.iam.application.dto.UserSummary
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.domain.port.PageResult
import java.time.Instant
import java.util.UUID

data class UserSummaryResponse(
    val id: UUID,
    val email: String,
    val fullName: String,
    val status: UserStatus,
    val emailVerified: Boolean,
    val lastLoginAt: Instant?
) {
    companion object {
        fun from(summary: UserSummary) = UserSummaryResponse(
            id = summary.id,
            email = summary.email,
            fullName = summary.fullName,
            status = summary.status,
            emailVerified = summary.emailVerified,
            lastLoginAt = summary.lastLoginAt
        )
    }
}

data class UserDetailResponse(
    val id: UUID,
    val email: String,
    val fullName: String,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: Set<RoleResponse>,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(detail: UserDetail) = UserDetailResponse(
            id = detail.id,
            email = detail.email,
            fullName = detail.fullName,
            status = detail.status,
            emailVerified = detail.emailVerified,
            roles = detail.roles.map(RoleResponse::from).toSet(),
            lastLoginAt = detail.lastLoginAt,
            createdAt = detail.createdAt,
            updatedAt = detail.updatedAt
        )
    }
}

data class RoleResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val isSystem: Boolean
) {
    companion object {
        fun from(dto: RoleDto) = RoleResponse(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            isSystem = dto.isSystem
        )
    }
}

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
) {
    companion object {
        fun from(tokens: TokenResponse) = TokenResponseDto(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            tokenType = tokens.tokenType,
            expiresIn = tokens.expiresIn
        )
    }
}

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> from(result: PageResult<T>): PageResponse<T> = PageResponse(
            items = result.items,
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            hasNext = result.hasNext,
            hasPrevious = result.hasPrevious
        )
    }
}
