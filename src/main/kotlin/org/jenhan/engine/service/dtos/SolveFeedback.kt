package org.jenhan.engine.service.dtos

data class SolveFeedback(val success: Boolean) {
    val feedback: String = if (success) {
        "Congratulations, you're right!"
    } else {
        "Wrong answer! Please, try again."
    }
}