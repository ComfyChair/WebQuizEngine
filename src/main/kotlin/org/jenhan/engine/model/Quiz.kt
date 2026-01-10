package org.jenhan.engine.model

import org.jenhan.engine.service.dtos.QuizDTO
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode

/**
 * Entity representing a quiz in the Web Quiz Engine system.
 *
 * Stores all quiz information including the question, answer options, correct answers,
 * and the author. Uses JPA for persistence with auto-generated primary keys.
 * The correct answers are stored separately and not exposed to clients via the DTO conversion.
 *
 * @property id Auto-generated unique identifier for the quiz (null before persistence)
 * @property author The QuizUser who created this quiz (eagerly fetched)
 * @property title The quiz title
 * @property text The quiz question or description text
 * @property options List of available answer options presented to users (eagerly fetched)
 * @property correctOptions List of zero-based indices representing correct answers in the options list (eagerly fetched using SUBSELECT)
 */
@Entity
data class Quiz(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val author: QuizUser,
    val title: String = "",
    val text: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    val options: List<String> = emptyList(),
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    val correctOptions: Set<Int> = emptySet(),
) {
    fun toDTO() : QuizDTO {
        return QuizDTO(id!!.toInt(), title, text, options)
    }
}