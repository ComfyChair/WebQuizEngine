package org.jenhan.engine.repositories

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
    val correctOptions: List<Int> = emptyList(),
) {
    fun toDTO() : QuizDTO {
        return QuizDTO(id!!.toInt(), title, text, options)
    }
}