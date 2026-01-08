package org.jenhan.engine.auth.registration

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    @field:Pattern(regexp = ".+@.+\\..+" , message = "Must be a valid email address")
    val email: String,
    @field:Size(min = 5, message = "password must have at least 5 characters")
    val password: String
){
}
