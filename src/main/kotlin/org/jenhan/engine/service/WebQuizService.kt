package org.jenhan.engine.service

import org.jenhan.engine.exceptionhandling.AuthorizationException
import org.jenhan.engine.exceptionhandling.NotFoundException
import org.jenhan.engine.exceptionhandling.QuizCreationException
import org.jenhan.engine.repositories.Quiz
import org.jenhan.engine.repositories.QuizCompletion
import org.jenhan.engine.repositories.QuizRepository
import org.jenhan.engine.repositories.QuizUser
import org.jenhan.engine.repositories.UserRepository
import org.jenhan.engine.service.dtos.SolveFeedback
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

/**
 * Service layer for managing web quiz operations.
 *
 * Handles quiz retrieval, creation, deletion, answer evaluation, and tracking user quiz completions.
 * Enforces authentication and authorization for protected operations.
 *
 * @property quizzes Repository for quiz data access
 * @property userRepository Repository for user data access
 */
@Service
class WebQuizService(
    private val quizzes: QuizRepository,
    private val userRepository: UserRepository,
) {

    /**
     * Retrieves a single quiz by its ID.
     *
     * @param quizId The unique identifier of the quiz
     * @return QuizDTO containing the quiz information
     * @throws NotFoundException if the quiz ID doesn't exist in the database
     */
    fun getQuiz(quizId: Int): QuizDTO {
        return quizzes.findById(quizId.toLong())
            .getOrNull()?.toDTO() ?: throw NotFoundException("Index $quizId out of bounds: 0..${quizzes.count() - 1}")
    }


    /**
     * Retrieves a paginated list of all quizzes.
     *
     * @param page Zero-based page number
     * @return Page of QuizDTO objects, with 10 items per page
     */
    fun getQuizzes(page: Int): Page<QuizDTO> {
        val page: Page<Quiz> = quizzes.findAll(PageRequest.of(page, 10))
        return page.map { it.toDTO() }
    }

    /**
     * Evaluates a user's answer to a quiz and records successful completions.
     *
     * Checks if the [answerId] matches the quiz's [Quiz.correctOptions] ids.
     * On a correct answer, saves a [QuizCompletion] record with timestamp to the [QuizUser]'s solved quizzes.
     *
     * @param userDetails Authentication details of the current user
     * @param quizId The ID of the quiz being answered
     * @param answerId The user's selected answer
     * @return ResponseEntity containing [SolveFeedback] with success status
     * @throws NotFoundException if the quiz ID doesn't exist
     * @throws AuthorizationException if user is not authenticated
     */
    fun evaluateAnswer(userDetails: UserDetails?, quizId: Int, answerId: Int?): ResponseEntity<SolveFeedback> {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).getOrNull() ?: throw NotFoundException("id does not correspond to a quiz in the database")

        LOGGER.debug("Evaluating answer {} for quiz with correct option of {}", answerId, quiz.correctOptions)
        return if (answerId in quiz.correctOptions) {
            LOGGER.debug("Answer: correct")
            user.solvedQuizzes.add(QuizCompletion(quizId, LocalDateTime.now()))
            userRepository.save(user)
            ResponseEntity.ok().body(SolveFeedback(true))
        } else {
            LOGGER.debug("Answer: wrong")
            ResponseEntity.ok().body(SolveFeedback(false))
        }
    }

    /**
     * Creates a new quiz authored by the authenticated user.
     *
     * Associates the quiz with the authenticated user as the author, persists it to the database,
     * and logs the creation event.
     *
     * @param userDetails Authentication details of the current user
     * @param quizCO Quiz creation object containing quiz details (title, text, options, answer)
     * @return QuizDTO of the newly created quiz
     * @throws AuthorizationException if user is not authenticated
     */
    fun addQuiz(userDetails: UserDetails?, quizCO: QuizCreationObject) : QuizDTO {
        val user = getUser(userDetails)
        val newQuiz = quizCO.toQuiz(user)
        quizzes.save(newQuiz)
        LOGGER.debug("Added quiz with id {} to quizzes: {}", newQuiz.id, quizzes)
        return newQuiz.toDTO()
    }

    /**
     * Deletes a quiz if the authenticated user is its author.
     *
     * Verifies that the authenticated user is the author of the quiz before deletion.
     *
     * @param quizId The ID of the quiz to delete
     * @param userDetails Authentication details of the current user
     * @throws NotFoundException if the quiz ID doesn't exist
     * @throws AuthorizationException if user is not authenticated or not the quiz author
     */
    fun deleteQuiz(quizId: Int, userDetails: UserDetails?) {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).orElseThrow { NotFoundException("quiz with id $quizId does not exist") }
        if (user != quiz.author) throw AuthorizationException("user is not the author of this quiz and thus not allowed to delete it")
        quizzes.delete(quiz)
    }

    /**
     * Retrieves a paginated list of quizzes completed by the authenticated user.
     *
     * Results are sorted by completion date in descending order (most recent first).
     *
     * @param userDetails Authentication details of the current user
     * @param page Zero-based page number
     * @return [PageImpl] of [QuizCompletion] objects, with 10 items per page
     * @throws AuthorizationException if user is not authenticated
     */
    fun getCompleted(userDetails: UserDetails?, page: Int): PageImpl<QuizCompletion> {
        val user = getUser(userDetails)
        val quizzes = userRepository.findCompletedQuizzesByUser(user)
        return PageImpl<QuizCompletion>( quizzes, PageRequest.of(
            page,
            10,
            Sort.by("competedAt").descending()
        ), quizzes.size.toLong())
    }

    /**
     * Helper method to retrieve the current authenticated user.
     *
     * @param userDetails Authentication details (nullable)
     * @return [QuizUser] entity of the authenticated user
     * @throws AuthorizationException if userDetails is null (user not authenticated)
     */
    private fun getUser(userDetails: UserDetails?): QuizUser {
        if (userDetails == null) throw AuthorizationException("not authenticated")
        return userRepository.findByEmail(userDetails.username)!!
    }

    companion object {
        /** Logger instance for this service */
        private val LOGGER = LoggerFactory.getLogger(WebQuizService::class.java)


        /**
         * Extension function that converts a QuizCreationObject to a Quiz entity.
         *
         * @param author The [QuizUser] creating the quiz
         * @return [Quiz] entity with null ID (to be assigned by database upon persistence)
         * @throws QuizCreationException if [QuizCreationObject.answer] index is out of [QuizCreationObject.options]' bounds
         */
        private fun QuizCreationObject.toQuiz(author: QuizUser): Quiz {
            if (answer !in 0..<options.size) throw QuizCreationException("answer index must correspond to an option index")
            return Quiz(null, author, title, text, options, listOf(answer))
        }
    }
}