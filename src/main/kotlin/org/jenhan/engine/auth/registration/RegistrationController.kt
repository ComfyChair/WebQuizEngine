package org.jenhan.engine.auth.registration

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RegistrationController(
    val registrationService: RegistrationService
)
{
    @PostMapping("/api/register")
    fun registerUser(
        @RequestBody @Valid registrationRequest: RegistrationRequest,
        result: BindingResult
    ) : ResponseEntity<Any> {
        LOGGER.info("Registration requested from ${registrationRequest.email}")
        if (result.hasErrors()) {
            LOGGER.error("Registration request is invalid")
            return ResponseEntity.badRequest().body("Invalid email address")
        } else {
            registrationService.register(registrationRequest)
            LOGGER.info("Registration completed successfully")
            return ResponseEntity(HttpStatus.OK)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RegistrationController::class.java)
    }
}