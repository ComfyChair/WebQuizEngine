package org.jenhan.engine.exceptionhandling

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity<CustomErrorMessage>(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(
        ConstraintViolationException::class,
        RegistrationException::class
    )
    fun handleBadRequestException(e: RuntimeException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity<CustomErrorMessage>(body, HttpStatus.BAD_REQUEST)
    }
    @ExceptionHandler(
        AuthorizationException::class,
    )
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<CustomErrorMessage> {
        val body = CustomErrorMessage(e.message ?: "Unknown error")
        return ResponseEntity<CustomErrorMessage>(body, HttpStatus.UNAUTHORIZED)
    }
}