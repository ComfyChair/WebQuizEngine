package org.jenhan.engine.exceptionhandling

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
     * Handles NotFoundException by returning a 404 Not Found response.
     *
     * Triggered when a requested resource (quiz, user, etc.) cannot be found in the database.
     *
     * @param e The NotFoundException that was thrown
     * @return ResponseEntity with CustomErrorMessage and HTTP 404 status
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
     * @param e The RuntimeException that was thrown (ConstraintViolationException,
     *          QuizCreationException, or RegistrationException)
     * @return ResponseEntity with CustomErrorMessage and HTTP 400 status
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
     * Handles authorization exceptions by returning a 401 Unauthorized response.
     *
     * Triggered when a user is not authenticated or lacks necessary permissions
     * to perform the requested action.
     *
     * @param e The AuthorizationException that was thrown
     * @return ResponseEntity with CustomErrorMessage and HTTP 401 status
     */
    @ExceptionHandler(
        AuthorizationException::class,
    )
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }
}