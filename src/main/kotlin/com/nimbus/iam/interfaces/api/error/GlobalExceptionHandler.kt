package com.nimbus.iam.interfaces.api.error

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbus.iam.domain.exception.DomainException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException as SpringAccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProblemDetail(
    @JsonProperty("type") val type: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("status") val status: Int,
    @JsonProperty("detail") val detail: String?,
    @JsonProperty("instance") val instance: String?,
    @JsonProperty("timestamp") val timestamp: Instant = Instant.now(),
    @JsonProperty("errors") val errors: List<FieldViolation>? = null
) {
    data class FieldViolation(
        @JsonProperty("field") val field: String,
        @JsonProperty("message") val message: String
    )

    companion object {
        private const val ERROR_BASE = "https://nimbus.iam/errors/"

        fun of(
            code: String,
            title: String,
            status: Int,
            detail: String?,
            instance: String?,
            errors: List<FieldViolation>? = null
        ): ProblemDetail = ProblemDetail(
            type = ERROR_BASE + code,
            title = title,
            status = status,
            detail = detail,
            instance = instance,
            errors = errors
        )
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.of(
            code = ex.code,
            title = ex.title,
            status = ex.status,
            detail = ex.message,
            instance = request.requestURI
        )
        return ResponseEntity.status(ex.status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val violations = ex.bindingResult.fieldErrors.map { error: FieldError ->
            ProblemDetail.FieldViolation(
                field = error.field,
                message = error.defaultMessage ?: "invalid value"
            )
        }
        val problem = ProblemDetail.of(
            code = "validation-error",
            title = "Validation Failed",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "The request payload contains ${violations.size} validation error(s).",
            instance = request.requestURI,
            errors = violations
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.of(
            code = "unauthenticated",
            title = "Unauthenticated",
            status = HttpStatus.UNAUTHORIZED.value(),
            detail = "Authentication is required to access this resource.",
            instance = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }

    @ExceptionHandler(SpringAccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: SpringAccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.of(
            code = "access-denied",
            title = "Access Denied",
            status = HttpStatus.FORBIDDEN.value(),
            detail = "You do not have permission to access this resource.",
            instance = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.of(
            code = "internal-error",
            title = "Internal Server Error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = "An unexpected error occurred. Please try again later.",
            instance = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem)
    }
}
