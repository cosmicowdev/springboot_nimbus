package com.nimbus.iam.interfaces.api.controller

import com.nimbus.iam.application.service.UserService
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.interfaces.api.dto.CreateUserRequest
import com.nimbus.iam.interfaces.api.dto.PageResponse
import com.nimbus.iam.interfaces.api.dto.UpdateUserRequest
import com.nimbus.iam.interfaces.api.dto.UserDetailResponse
import com.nimbus.iam.interfaces.api.dto.UserSummaryResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val userService: UserService
) {
    @GetMapping
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam status: UserStatus?
    ): ResponseEntity<PageResponse<UserSummaryResponse>> {
        val result = userService.findAll(page, size, status)
        val response = PageResponse(
            items = result.items.map(UserSummaryResponse::from),
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            hasNext = result.hasNext,
            hasPrevious = result.hasPrevious
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserDetailResponse> {
        val detail = userService.findById(id)
        return ResponseEntity.ok(UserDetailResponse.from(detail))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserDetailResponse {
        val detail = userService.create(request.toCommand())
        return UserDetailResponse.from(detail)
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDetailResponse> {
        val detail = userService.update(id, request.toCommand())
        return ResponseEntity.ok(UserDetailResponse.from(detail))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: UUID) {
        userService.delete(id)
    }
}
