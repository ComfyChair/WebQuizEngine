package org.jenhan.engine.model

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

/**
 * Repository interface for QuizUser entity data access.
 *
 * Provides CRUD operations, pagination, sorting capabilities, and custom queries
 * for managing quiz users and their completed quizzes.
 *
 * Extends both PagingAndSortingRepository and CrudRepository to provide comprehensive
 * data access functionality for QuizUser entities.
 */
interface UserRepository: PagingAndSortingRepository<QuizUser, Long>, CrudRepository<QuizUser, Long> {
    fun findByEmail(email: String): QuizUser?
    fun existsByEmail(email: String): Boolean

    @Query("""
            SELECT quizUser.solvedQuizzes as solvedQuizzes
            FROM QuizUser quizUser
            """)
    fun findCompletedQuizzesByUser(user: QuizUser, pageable: Pageable): Page<QuizCompletion>
}
