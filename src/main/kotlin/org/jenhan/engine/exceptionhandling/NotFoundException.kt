package org.jenhan.engine.exceptionhandling

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * Used to indicate that a quiz, user, or other entity does not exist in the database
 * when attempting to retrieve or operate on it by ID or other identifier.
 *
 * @param message Detailed description of what resource was not found
 */
class NotFoundException(message: String): RuntimeException(message)