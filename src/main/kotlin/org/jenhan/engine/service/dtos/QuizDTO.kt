package org.jenhan.engine.service.dtos

import kotlinx.serialization.Serializable

@Serializable
class QuizDTO (
    val id: Int,
    val title: String,
    val text: String,
    val options: List<String>,
) {
}