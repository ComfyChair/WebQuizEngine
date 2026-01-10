package org.jenhan.engine.model

import jakarta.persistence.*

/**
 * Entity representing a user in the Web Quiz Engine system.
 *
 * Stores user authentication information, roles, and tracks both quizzes authored by the user
 * and quizzes they have completed. Uses JPA for persistence with auto-generated primary keys.
 *
 * @property id Auto-generated unique identifier for the user (null before persistence)
 * @property email User's email address, used for authentication and identification
 * @property pwHash Hashed password for secure authentication
 * @property authority User's role/authority for authorization (e.g., "ROLE_USER", "ROLE_ADMIN")
 * @property authoredQuizzes Mutable list of quizzes created by this user
 * @property solvedQuizzes Mutable list of quiz completions tracking which quizzes the user has solved
 */
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