package com.nimbus.iam.infrastructure.security

import com.nimbus.iam.config.SecurityConfigurationProperties
import com.nimbus.iam.domain.exception.InvalidTokenException
import com.nimbus.iam.domain.model.User
import com.nimbus.iam.domain.port.AccessTokenClaims
import com.nimbus.iam.domain.port.TokenSigner
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenSigner(
    private val properties: SecurityConfigurationProperties
) : TokenSigner {

    private val signingKey: SecretKey = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(properties.jwt.secret)
    )

    override fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(properties.jwt.accessTtlMinutes * 60)
        return Jwts.builder()
            .issuer(properties.jwt.issuer)
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("authorities", user.authorities)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    override fun generateRefreshToken(): String {
        val bytes = ByteArray(48)
        java.security.SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    override fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return Base64.getEncoder().encodeToString(digest.digest(rawToken.toByteArray()))
    }

    override fun validateAccessToken(token: String): AccessTokenClaims {
        val claims = parseClaims(token)
        return AccessTokenClaims(
            userId = UUID.fromString(claims.subject),
            email = claims.get("email", String::class.java),
            authorities = (claims.get("authorities", List::class.java) as List<*>)
                .filterIsInstance<String>()
                .toSet(),
            expiresAt = claims.expiration.toInstant()
        )
    }

    override fun extractUserId(accessToken: String): UUID =
        UUID.fromString(parseClaims(accessToken).subject)

    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.jwt.issuer)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw InvalidTokenException("access token has expired")
        } catch (e: JwtException) {
            throw InvalidTokenException(e.message ?: "malformed token")
        }
    }
}
