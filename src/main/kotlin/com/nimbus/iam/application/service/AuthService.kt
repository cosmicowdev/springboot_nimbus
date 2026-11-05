package com.nimbus.iam.application.service

import com.nimbus.iam.application.dto.LoginCommand
import com.nimbus.iam.application.dto.RegisterCommand
import com.nimbus.iam.application.dto.TokenResponse
import com.nimbus.iam.domain.exception.AccountNotActiveException
import com.nimbus.iam.domain.exception.EmailNotVerifiedException
import com.nimbus.iam.domain.exception.InvalidCredentialsException
import com.nimbus.iam.domain.exception.InvalidTokenException
import com.nimbus.iam.domain.exception.TokenExpiredException
import com.nimbus.iam.domain.exception.TokenRevokedException
import com.nimbus.iam.domain.exception.UserAlreadyExistsException
import com.nimbus.iam.domain.exception.UserNotFoundException
import com.nimbus.iam.domain.model.AuthTokens
import com.nimbus.iam.domain.model.RefreshToken
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.model.UserStatus
import com.nimbus.iam.domain.port.PasswordHasher
import com.nimbus.iam.domain.port.RefreshTokenRepository
import com.nimbus.iam.domain.port.TokenSigner
import com.nimbus.iam.domain.port.UserRepository
import com.nimbus.iam.application.dto.UserSummary
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenSigner: TokenSigner,
    @Value("\${nimbus.security.jwt.refresh-ttl-days:7}") private val refreshTtlDays: Long,
    @Value("\${nimbus.security.jwt.access-ttl-minutes:15}") private val accessTtlMinutes: Long
) {
    fun register(command: RegisterCommand): UserSummary {
        if (userRepository.existsByEmail(command.email)) {
            throw UserAlreadyExistsException(command.email)
        }

        val now = Instant.now()
        val user = User(
            id = UUID.randomUUID(),
            email = command.email,
            passwordHash = passwordHasher.hash(command.rawPassword),
            fullName = command.fullName,
            status = UserStatus.PENDING,
            emailVerified = false,
            roles = emptySet(),
            createdAt = now,
            updatedAt = now
        )
        val saved = userRepository.save(user)
        return UserSummary.from(saved)
    }

    fun login(command: LoginCommand): TokenResponse {
        val user = userRepository.findByEmail(command.email)
            .orElseThrow { InvalidCredentialsException() }

        if (!passwordHasher.matches(command.rawPassword, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        if (user.status != UserStatus.ACTIVE) {
            throw AccountNotActiveException(user.status.name)
        }
        if (!user.emailVerified) {
            throw EmailNotVerifiedException()
        }

        userRepository.updateLastLogin(user.id, Instant.now())
        val tokens = issueTokens(user)
        return TokenResponse.from(tokens)
    }

    fun refresh(rawRefreshToken: String): TokenResponse {
        val tokenHash = tokenSigner.hashToken(rawRefreshToken)
        val stored = refreshTokenRepository.findByHash(tokenHash)
            .orElseThrow { InvalidTokenException("refresh token not recognized") }

        if (stored.isRevoked()) throw TokenRevokedException()
        if (stored.isExpired()) throw TokenExpiredException()

        refreshTokenRepository.revoke(stored.id, Instant.now())
        val user = userRepository.findById(stored.userId)
            .orElseThrow { UserNotFoundException(stored.userId.toString()) }

        if (user.status != UserStatus.ACTIVE) {
            throw AccountNotActiveException(user.status.name)
        }

        val tokens = issueTokens(user)
        return TokenResponse.from(tokens)
    }

    fun logout(rawRefreshToken: String) {
        val tokenHash = tokenSigner.hashToken(rawRefreshToken)
        val stored = refreshTokenRepository.findByHash(tokenHash)
        if (stored.isPresent) {
            refreshTokenRepository.revoke(stored.get().id, Instant.now())
        }
    }

    fun parseAccessToken(accessToken: String): AccessTokenClaims =
        tokenSigner.validateAccessToken(accessToken)

    fun loadUserForToken(claims: AccessTokenClaims): User =
        userRepository.findById(claims.userId)
            .orElseThrow { UserNotFoundException(claims.userId.toString()) }

    private fun issueTokens(user: User): AuthTokens {
        val accessToken = tokenSigner.generateAccessToken(user)
        val rawRefresh = tokenSigner.generateRefreshToken()
        val now = Instant.now()
        refreshTokenRepository.save(
            RefreshToken(
                id = UUID.randomUUID(),
                userId = user.id,
                tokenHash = tokenSigner.hashToken(rawRefresh),
                expiresAt = now.plusSeconds(refreshTtlDays * 86400),
                createdAt = now
            )
        )
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = rawRefresh,
            expiresIn = accessTtlMinutes * 60
        )
    }
}
