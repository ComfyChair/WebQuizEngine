package org.jenhan.engine.repositories

import jakarta.persistence.*

@Entity
data class QuizUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var email: String = "",
    var pwHash: String= "",
    var authority: String = "",
    @OneToMany
    @Column(name = "author_id")
    val authoredQuizzes: MutableList<Quiz> = ArrayList(),
    @ElementCollection
    val solvedQuizzes: MutableList<QuizCompletion> = ArrayList(),
)