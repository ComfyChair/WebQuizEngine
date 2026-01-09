package org.jenhan.engine.exceptionhandling

/**
 * Exception thrown when a user is not authorized to perform an operation.
 *
 * Used to indicate authentication failures (user not logged in) or authorization
 * failures (user lacks necessary permissions for the requested action).
 *
 * @param message Detailed description of the authorization failure
 */
class AuthorizationException(message: String) : RuntimeException(message)