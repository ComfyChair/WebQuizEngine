package org.jenhan.engine.exceptionhandling

/**
 * Exception thrown when user registration fails.
 *
 * Used to indicate errors during the user registration process, such as duplicate email addresses,
 * that prevent a new user from being registered successfully.
 *
 * @param message Detailed description of why registration failed
 */
class RegistrationException(message: String): RuntimeException(message)