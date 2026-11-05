package com.nimbus.iam.domain.exception

import java.util.UUID

abstract class DomainException(
    val code: String,
    message: String,
    val title: String,
    val status: Int,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class UserNotFoundException(identifier: String) : DomainException(
    code = "user-not-found",
    title = "User Not Found",
    status = 404,
    message = "No user exists with identifier '$identifier'."
)

class UserAlreadyExistsException(email: String) : DomainException(
    code = "user-already-exists",
    title = "User Already Exists",
    status = 409,
    message = "A user is already registered with email '$email'."
)

class RoleNotFoundException(identifier: String) : DomainException(
    code = "role-not-found",
    title = "Role Not Found",
    status = 404,
    message = "No role exists with identifier '$identifier'."
)

class RoleAlreadyExistsException(name: String) : DomainException(
    code = "role-already-exists",
    title = "Role Already Exists",
    status = 409,
    message = "A role named '$name' already exists."
)

class SystemRoleMutationException(roleId: UUID) : DomainException(
    code = "system-role-mutation",
    title = "System Role Protected",
    status = 403,
    message = "Role '$roleId' is system-managed and cannot be modified or deleted."
)

class InvalidCredentialsException : DomainException(
    code = "invalid-credentials",
    title = "Invalid Credentials",
    status = 401,
    message = "The provided credentials are invalid."
)

class AccountNotActiveException(status: String) : DomainException(
    code = "account-not-active",
    title = "Account Not Active",
    status = 403,
    message = "Account is not active (current status: $status)."
)

class EmailNotVerifiedException : DomainException(
    code = "email-not-verified",
    title = "Email Not Verified",
    status = 403,
    message = "Email verification is required before signing in."
)

class InvalidTokenException(reason: String) : DomainException(
    code = "invalid-token",
    title = "Invalid Token",
    status = 401,
    message = "The token is invalid: $reason."
)

class TokenRevokedException : DomainException(
    code = "token-revoked",
    title = "Token Revoked",
    status = 401,
    message = "The provided refresh token has been revoked."
)

class TokenExpiredException : DomainException(
    code = "token-expired",
    title = "Token Expired",
    status = 401,
    message = "The provided refresh token has expired."
)

class PasswordMismatchException : DomainException(
    code = "password-mismatch",
    title = "Password Mismatch",
    status = 400,
    message = "The current password does not match."
)

class WeakPasswordException(reason: String) : DomainException(
    code = "weak-password",
    title = "Weak Password",
    status = 400,
    message = "The password does not meet security requirements: $reason."
)

class AccessDeniedException(resource: String) : DomainException(
    code = "access-denied",
    title = "Access Denied",
    status = 403,
    message = "You do not have permission to access $resource."
)
