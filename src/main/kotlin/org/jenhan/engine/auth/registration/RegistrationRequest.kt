package org.jenhan.engine.auth.registration

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

/**
 * Data transfer object for user registration requests.
 *
 * Contains user credentials with validation constraints to ensure data integrity.
 * Email must follow a valid format and password must meet minimum length requirements.
 *
 * @property email User's email address (must match pattern: characters@characters.characters)
 * @property password User's password (minimum 5 characters required)
 */
@Serializable
data class RegistrationRequest(
    @field:Pattern(regexp = ".+@.+\\..+" , message = "Must be a valid email address")
    val email: String,
    @field:Size(min = 5, message = "password must have at least 5 characters")
    val password: String
){
}
