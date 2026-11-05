package com.nimbus.iam.infrastructure.security

import com.nimbus.iam.config.SecurityConfigurationProperties
import com.nimbus.iam.domain.port.UserRepository
import com.nimbus.iam.domain.exception.UserNotFoundException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException(email) }
        return SpringUser.builder()
            .username(user.email)
            .password(user.passwordHash)
            .authorities(user.authorities.map { SimpleGrantedAuthority(it) })
            .accountLocked(user.status != com.nimbus.iam.domain.model.UserStatus.ACTIVE)
            .disabled(!user.emailVerified)
            .build()
    }
}
