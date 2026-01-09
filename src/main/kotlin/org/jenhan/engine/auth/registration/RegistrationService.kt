package org.jenhan.engine.auth.registration

import org.jenhan.engine.repositories.QuizUser
import org.jenhan.engine.repositories.UserRepository
import org.jenhan.engine.exceptionhandling.RegistrationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Service for handling user registration operations.
 *
 * Manages the creation of new user accounts, including email validation,
 * password hashing, and persisting user data to the database.
 *
 * @property userRepository Repository for user data access and persistence
 * @property encoder Password encoder for securely hashing user passwords
 */
@Service
class RegistrationService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder) {

    /**
     * Registers a new user account.
     *
     * Validates that the email is not already registered, hashes the password using BCrypt,
     * and creates a new QuizUser with the "ROLE_USER" authority.
     *
     * @param registrationRequest Registration details containing email and password
     * @throws RegistrationException if a user with the given email already exists
     */
    fun register(registrationRequest: RegistrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.email) ) throw RegistrationException("a user account for this email has already been created")
        val user = registrationRequest.toUser()
        userRepository.save(user)
    }

    /**
     * Converts a RegistrationRequest to a QuizUser entity.
     *
     * Extension function that creates a new QuizUser with a BCrypt-hashed password
     * and the default "ROLE_USER" authority.
     *
     * @return QuizUser entity ready for persistence
     */
    private fun RegistrationRequest.toUser(): QuizUser {
        val pwHash = encoder.encode(password)
        return QuizUser(null, email, pwHash!!, "ROLE_USER")
    }
}