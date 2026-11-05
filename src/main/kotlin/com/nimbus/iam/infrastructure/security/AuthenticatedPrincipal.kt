package com.nimbus.iam.infrastructure.security

import com.nimbus.iam.domain.model.User
import java.util.UUID

data class AuthenticatedPrincipal(
    val userId: UUID,
    val email: String,
    val authorities: Set<String>
) {
    companion object {
        fun from(user: User) = AuthenticatedPrincipal(
            userId = user.id,
            email = user.email,
            authorities = user.authorities
        )
    }
}
