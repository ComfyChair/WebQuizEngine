package org.jenhan.engine.exceptionhandling

/**
 * Exception thrown when quiz creation fails due to validation or business rule violations.
 *
 * Used to indicate errors during the quiz creation process, such as invalid answer id,
 * that prevent a quiz from being created successfully.
 *
 * @param message Detailed description of why quiz creation failed
 */
class QuizCreationException(message: String) : RuntimeException(message)