package com.nimbus.iam.config

import org.springframework.boot.context.properties.ConfigurationProperties

data class CorsProperties(
    val allowedOrigins: List<String> = listOf(),
    val allowedMethods: List<String> = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"),
    val allowedHeaders: String = "*",
    val allowCredentials: Boolean = true,
    val maxAge: Long = 3600
)

data class PasswordProperties(
    val bcryptStrength: Int = 12
)

data class JwtProperties(
    val secret: String = "",
    val accessTtlMinutes: Long = 15,
    val refreshTtlDays: Long = 7,
    val issuer: String = "nimbus-iam"
)

data class SecurityProperties(
    val jwt: JwtProperties = JwtProperties(),
    val cors: CorsProperties = CorsProperties(),
    val password: PasswordProperties = PasswordProperties()
)

@ConfigurationProperties(prefix = "nimbus.security")
class SecurityConfigurationProperties : SecurityProperties()
