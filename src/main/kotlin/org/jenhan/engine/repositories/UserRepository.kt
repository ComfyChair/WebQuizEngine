package org.jenhan.engine.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface UserRepository: PagingAndSortingRepository<QuizUser, Long>, CrudRepository<QuizUser, Long> {
    fun findByEmail(email: String): QuizUser?
    fun existsByEmail(email: String): Boolean

    @Query("""
            SELECT quizUser.solvedQuizzes as solvedQuizzes
            FROM QuizUser quizUser
            """)
    fun findCompletedQuizzesByUser(user: QuizUser): List<QuizCompletion>
}
