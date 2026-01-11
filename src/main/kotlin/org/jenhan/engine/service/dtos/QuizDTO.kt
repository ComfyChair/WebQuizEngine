package org.jenhan.engine.service.dtos

/**
 * Data transfer object representing a quiz for client communication.
 *
 * Contains the essential quiz information without exposing sensitive data like correct answers
 * or author information. Used for serialization in API responses.
 *
 * @property id Unique identifier of the quiz
 * @property title The quiz title
 * @property text The quiz question or description text
 * @property options List of available answer options presented to the user
 */
data class QuizDTO (
    val id: Int?,
    val title: String,
    val text: String,
    val options: List<String>,
) {
}