package org.jenhan.engine.service.dtos

/**
 * Data transfer object representing feedback for a quiz answer submission.
 *
 * Provides both a boolean success indicator and a human-readable feedback message
 * based on whether the submitted answer was correct.
 *
 * @property success Boolean indicating whether the submitted answer was correct
 * @property feedback Human-readable message
 */
data class SolutionFeedback(val success: Boolean) {
    val feedback: String = if (success) {
        "Congratulations, you're right!"
    } else {
        "Wrong answer! Please, try again."
    }
}