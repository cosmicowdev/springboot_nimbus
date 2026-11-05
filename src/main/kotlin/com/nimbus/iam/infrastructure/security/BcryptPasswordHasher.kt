package com.nimbus.iam.infrastructure.security

import com.nimbus.iam.config.SecurityConfigurationProperties
import com.nimbus.iam.domain.port.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BcryptPasswordHasher(
    properties: SecurityConfigurationProperties
) : PasswordHasher {

    private val encoder = BCryptPasswordEncoder(properties.password.bcryptStrength)

    override fun hash(rawPassword: String): String = encoder.encode(rawPassword)

    override fun matches(rawPassword: String, hashed: String): Boolean =
        encoder.matches(rawPassword, hashed)
}
