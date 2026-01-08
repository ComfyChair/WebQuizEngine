package org.jenhan.engine.service.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class QuizCreationObject (
    @field:NotBlank(message = "Quiz title cannot be blank")
    val title: String,
    @field:NotBlank(message = "Quiz text cannot be blank")
    val text: String,
    @field:Size(min = 2, message = "There must be at least two answer options")
    val options: List<String>,
    @field:Min(0, message = "answer index must correspond to an option index")
    val answer: Int
)