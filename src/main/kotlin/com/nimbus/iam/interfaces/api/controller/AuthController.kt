package com.nimbus.iam.interfaces.api.controller

import com.nimbus.iam.application.service.AuthService
import com.nimbus.iam.interfaces.api.dto.LoginRequest
import com.nimbus.iam.interfaces.api.dto.LogoutRequest
import com.nimbus.iam.interfaces.api.dto.RefreshRequest
import com.nimbus.iam.interfaces.api.dto.RegisterRequest
import com.nimbus.iam.interfaces.api.dto.TokenResponseDto
import com.nimbus.iam.interfaces.api.dto.UserSummaryResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): UserSummaryResponse {
        val summary = authService.register(request.toCommand())
        return UserSummaryResponse.from(summary)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponseDto> {
        val tokens = authService.login(request.toCommand())
        return ResponseEntity.ok(TokenResponseDto.from(tokens))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<TokenResponseDto> {
        val tokens = authService.refresh(request.refreshToken)
        return ResponseEntity.ok(TokenResponseDto.from(tokens))
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Valid @RequestBody request: LogoutRequest) {
        authService.logout(request.refreshToken)
    }
}
