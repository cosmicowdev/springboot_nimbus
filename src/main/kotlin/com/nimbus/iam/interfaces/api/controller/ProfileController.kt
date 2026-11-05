package com.nimbus.iam.interfaces.api.controller

import com.nimbus.iam.application.service.UserService
import com.nimbus.iam.infrastructure.security.AuthenticatedPrincipal
import com.nimbus.iam.interfaces.api.dto.ChangePasswordRequest
import com.nimbus.iam.interfaces.api.dto.UpdateProfileRequest
import com.nimbus.iam.interfaces.api.dto.UserDetailResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me")
class ProfileController(
    private val userService: UserService
) {
    @GetMapping
    fun getCurrentUser(@AuthenticationPrincipal principal: AuthenticatedPrincipal): ResponseEntity<UserDetailResponse> {
        val detail = userService.findById(principal.userId)
        return ResponseEntity.ok(UserDetailResponse.from(detail))
    }

    @PatchMapping
    fun updateProfile(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserDetailResponse> {
        val detail = userService.updateProfile(principal.userId, request.toCommand())
        return ResponseEntity.ok(UserDetailResponse.from(detail))
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal principal: AuthenticatedPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest
    ) {
        userService.changePassword(principal.userId, request.toCommand())
    }
}
