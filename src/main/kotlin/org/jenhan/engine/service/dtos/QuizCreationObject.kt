package org.jenhan.engine.service.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

/**
 * Data transfer object for creating a new quiz.
 *
 * Contains all necessary information to create a quiz, including validation constraints
 * to ensure data integrity. The correct answer is specified by its index in the options list.
 *
 * @property title The quiz title (must not be blank)
 * @property text The quiz question or description text (must not be blank)
 * @property options List of available answer options (minimum 2 options required)
 * @property answer Zero-based index of the correct answer in the options list (must be >= 0)
 */
@Serializable
data class QuizCreationObject (
    @field:NotBlank(message = "Quiz title cannot be blank")
    val title: String,
    @field:NotBlank(message = "Quiz text cannot be blank")
    val text: String,
    @field:Size(min = 2, message = "There must be at least two answer options")
    val options: List<String>,
    @field:Size(min = 1, message = "There must be at least one correct option")
    val answer: Set<Int>
)