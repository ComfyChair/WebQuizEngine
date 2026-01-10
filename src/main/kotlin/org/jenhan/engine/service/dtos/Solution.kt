package org.jenhan.engine.service.dtos

import kotlinx.serialization.Serializable
import org.jenhan.engine.model.Quiz

/**
 * Multiple choice answer for quiz solving attempts. Serves as a data transfer object.
 *
 * @property quizId Identifies the [Quiz] by its id
 * @property answer Set of chosen indices within [Quiz.options]
 */
@Serializable
data class Solution(
    val quizId: Int,
    val answer: Set<Int>
)