package org.jenhan.engine.repositories

import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class QuizCompletion(
    val id: Int,
    val completedAt: LocalDateTime
)
