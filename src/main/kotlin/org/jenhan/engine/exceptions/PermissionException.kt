package org.jenhan.engine.exceptions

/**
 * Exception thrown when a user is not authorized to perform an operation.
 *
 * Used to indicate authorization failures (user lacks necessary permissions for the requested action).
 *
 * @param message Detailed description of the authorization failure
 */
class PermissionException(message: String) : RuntimeException(message)