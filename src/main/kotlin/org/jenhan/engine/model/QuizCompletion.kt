package org.jenhan.engine.model

import jakarta.persistence.Embeddable
import java.time.LocalDateTime

/**
 * Embeddable entity representing a completed quiz record.
 *
 * Stores the quiz ID and the timestamp when a user successfully completed the quiz.
 * This class is embedded within the [QuizUser] entity to track quiz completion history.
 * Being embeddable means instances are stored directly in the user's table rather than
 * in a separate table.
 *
 * @property id The ID of the completed quiz
 * @property completedAt Timestamp when the quiz was successfully completed
 */
@Embeddable
data class QuizCompletion(
    val id: Int?,
    val completedAt: LocalDateTime
)
