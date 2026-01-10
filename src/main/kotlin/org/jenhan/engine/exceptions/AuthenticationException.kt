package org.jenhan.engine.exceptions

/**
 * Exception thrown when an unauthenticated user tries to perform a secure operation.
 *
 * Used to indicate authentication failures (user not logged in).
 *
 * @param message Detailed description of the authorization failure
 */
class AuthenticationException(message: String) : RuntimeException(message)