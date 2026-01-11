package org.jenhan.engine.security.registration

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.jenhan.engine.exceptions.RegistrationException

/**
 * REST controller for user registration operations.
 *
 * Provides endpoints for creating new user accounts with email and password validation.
 * Handles registration requests and delegates business logic to the RegistrationService.
 *
 * @property registrationService Service layer handling user registration logic
 */
@RestController
class RegistrationController(
    val registrationService: RegistrationService
)
{
    /**
     * Registers a new user account.
     *
     * **Endpoint:** `POST /api/register`
     *
     * Validates the registration request data and creates a new user account if validation passes.
     * Logs registration attempts and outcomes for auditing purposes.
     *
     * @param registrationRequest [RegistrationRequest] containing user email and password
     * @param result Binding result containing validation errors, if any
     * @return ResponseEntity with HTTP 200 OK on success, or HTTP 400 Bad Request with error message on validation failure
     * @throws RegistrationException if a user with the given email already exists (handled by global exception handler)
     */
    @PostMapping("/api/register")
    fun registerUser(
        @RequestBody @Valid registrationRequest: RegistrationRequest,
        result: BindingResult
    ) : ResponseEntity<Any> {
        LOGGER.info("Registration requested from ${registrationRequest.email}")
        if (result.hasErrors()) {
            LOGGER.error("Registration request is invalid")
            return ResponseEntity.badRequest().body(INVALID_CREDENTIALS)
        } else {
            registrationService.register(registrationRequest)
            LOGGER.info("Registration completed successfully")
            return ResponseEntity.ok(REGISTRATION_SUCCESSFUL)
        }
    }

    companion object {
        /** Logger instance for this controller */
        private val LOGGER = LoggerFactory.getLogger(RegistrationController::class.java)
        const val REGISTRATION_SUCCESSFUL = "Registration successful"
        const val INVALID_CREDENTIALS = "Invalid credentials: Email must be a valid email address and password has to contain > 4 characters."
    }
}