package org.jenhan.engine.exceptions

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Global exception handler for the Web Quiz Engine application.
 *
 * Provides centralized exception handling across all controllers, converting exceptions
 * into appropriate HTTP responses with standardized error messages. Uses Spring's
 * @ControllerAdvice to intercept exceptions thrown by any controller.
 */
@ControllerAdvice
class ControllerExceptionHandler {

    /**
     * Handles exceptions due to missing resources by returning a 404 Not Found response.
     *
     * Triggered when a requested resource (quiz, user, etc.) cannot be found in the database.
     *
     * @param e The [NotFoundException] that was thrown
     * @return [ResponseEntity] with [CustomErrorMessage] and HTTP 404 status
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }

    /**
     * Handles validation and business logic exceptions by returning a 400 Bad Request response.
     *
     * Triggered when request data fails validation constraints, quiz creation fails,
     * or user registration encounters an error.
     *
     * @param e The [RuntimeException] that was thrown ([ConstraintViolationException],
     *          [QuizCreationException], or [RegistrationException])
     * @return [ResponseEntity] with [CustomErrorMessage] and HTTP 400 status
     */
    @ExceptionHandler(
        ConstraintViolationException::class,
        QuizCreationException::class,
        RegistrationException::class
    )
    fun handleBadRequestException(e: RuntimeException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handles authentication exceptions by returning a 401 Unauthorized response.
     *
     * Triggered when an unauthenticated user tries to access a secured endpoint.
     *
     * @param e The [AuthenticationException] that was thrown
     * @return [ResponseEntity] with [CustomErrorMessage] and HTTP 401 status
     */
    @ExceptionHandler(
        AuthenticationException::class,
    )
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Handles authorization exceptions by returning a 403 Forbidden response.
     *
     * Triggered when an authenticated user lacks necessary permissions
     * to perform the requested action.
     *
     * @param e The [PermissionException] that was thrown
     * @return [ResponseEntity] with [CustomErrorMessage] and HTTP 403 status
     */
    @ExceptionHandler(
        PermissionException::class,
    )
    fun handlePermissionException(e: PermissionException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.FORBIDDEN)
    }
}