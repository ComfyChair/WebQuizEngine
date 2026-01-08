package org.jenhan.engine.auth.registration

import org.jenhan.engine.repositories.QuizUser
import org.jenhan.engine.repositories.UserRepository
import org.jenhan.engine.exceptionhandling.RegistrationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegistrationService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder) {
    fun register(registrationRequest: RegistrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.email) ) throw RegistrationException("a user account for this email has already been created")
        val user = registrationRequest.toUser()
        userRepository.save(user)
    }

    private fun RegistrationRequest.toUser(): QuizUser {
        val pwHash = encoder.encode(password)
        return QuizUser(null, email, pwHash!!, "ROLE_USER")
    }
}