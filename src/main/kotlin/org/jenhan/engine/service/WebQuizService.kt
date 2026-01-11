package org.jenhan.engine.service

import org.jenhan.engine.exceptions.AuthenticationException
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.exceptions.PermissionException
import org.jenhan.engine.exceptions.QuizCreationException
import org.jenhan.engine.model.Quiz
import org.jenhan.engine.model.QuizCompletion
import org.jenhan.engine.model.QuizRepository
import org.jenhan.engine.model.QuizUser
import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.service.dtos.SolutionFeedback
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired private val quizzes: QuizRepository,
    @Autowired private val userRepository: UserRepository,
) {

    /**
     * Retrieves a single quiz by its ID.
     *
     * @param quizId The unique identifier of the quiz
     * @return [QuizDTO] containing the quiz information
     * @throws NotFoundException if the quiz ID doesn't exist in the database
     */
    fun getQuiz(quizId: Int): QuizDTO {
        val optionalQuiz = quizzes.findById(quizId.toLong())
        val quiz = optionalQuiz.getOrNull()
            ?: throw NotFoundException("Quiz index $quizId not found")
        return quiz.toDTO()
    }


    /**
     * Retrieves a paginated list of all quizzes.
     *
     * @param page Zero-based page number
     * @return [Page] of [QuizDTO] objects, with 10 items per page
     */
    fun getQuizzes(page: Int): Page<QuizDTO> {
        val page: Page<Quiz> = quizzes.findAll(PageRequest.of(page, 10))
        return page.map { it.toDTO() }
    }

    /**
     * Evaluates a user's answer to a quiz and records successful completions.
     *
     * Checks if the [answerIds] matches the quiz's [Quiz.correctOptions] ids.
     * On a correct answer, saves a [QuizCompletion] record with timestamp to the [QuizUser]'s solved quizzes.
     *
     * @param userDetails Authentication details of the current user
     * @param quizId The ID of the quiz being answered
     * @param answerIds The user's selected answer
     * @return [ResponseEntity] containing [SolutionFeedback] with success status
     * @throws NotFoundException if the quiz ID doesn't exist
     * @throws AuthenticationException if user is not authenticated
     */
    fun evaluateAnswer(userDetails: UserDetails?, quizId: Int, answerIds: Set<Int>): ResponseEntity<SolutionFeedback> {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).getOrNull() ?: throw NotFoundException("id does not correspond to a quiz in the database")

        LOGGER.debug("Evaluating answer {} for quiz with correct option of {}", answerIds, quiz.correctOptions)
        return if (answerIds == quiz.correctOptions) {
            LOGGER.debug("Answer: correct")
            user.solvedQuizzes.add(QuizCompletion(quizId, LocalDateTime.now()))
            userRepository.save(user)
            ResponseEntity.ok().body(SolutionFeedback(true))
        } else {
            LOGGER.debug("Answer: wrong")
            ResponseEntity.ok().body(SolutionFeedback(false))
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
     * @return [QuizDTO] of the newly created quiz
     * @throws AuthenticationException if user is not authenticated
     */
    fun addQuiz(userDetails: UserDetails?, quizCO: QuizCreationObject) : QuizDTO {
        val user = getUser(userDetails)
        val newQuiz = quizCO.toQuiz(id = null, author = user) // id generation is handled by persistence layer
        println("So far, so good")
        quizzes.save(newQuiz)
        println("Even better")
        LOGGER.debug("Added quiz with id {} to quizzes", newQuiz.id)
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
     * @throws AuthenticationException if user is not authenticated
     * @throws PermissionException if user is not the quiz author
     */
    fun deleteQuiz(quizId: Int, userDetails: UserDetails?) {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).orElseThrow { NotFoundException("quiz with id $quizId does not exist") }
        if (user != quiz.author) throw PermissionException("user is not the author of this quiz and thus not allowed to delete it")
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
     * @throws AuthenticationException if user is not authenticated
     */
    fun getCompleted(userDetails: UserDetails?, page: Int): PageImpl<QuizCompletion> {
        val user = getUser(userDetails)
        val completions = userRepository.findCompletedQuizzesByUser(user)
        return completions.toPage(page,10,Sort.by("completedAt").descending())
    }

    /**
     * Helper method to retrieve the current authenticated user.
     *
     * @param userDetails Authentication details (nullable)
     * @return [QuizUser] entity of the authenticated user
     * @throws AuthenticationException if userDetails is null (user not authenticated)
     */
    private fun getUser(userDetails: UserDetails?): QuizUser {
        if (userDetails == null) throw AuthenticationException("not authenticated")
        return userRepository.findByEmail(userDetails.username)!!
    }

    companion object {
        /** Logger instance for this service */
        private val LOGGER = LoggerFactory.getLogger(WebQuizService::class.java)

        /**
         * Generic extension function that converts a List to [PageImpl].
         *
         * @param pageNo The number of the page to be returned
         * @param pageSize The number of items per page
         * @param sort The optional [Sort]
         * @return [PageImpl] of the List item type
         */
        fun <T> List<T>.toPage(pageNo: Int, pageSize: Int, sort: Sort = Sort.unsorted()): PageImpl<T> {
            return PageImpl(
                this,
                PageRequest.of(pageNo, pageSize, sort),
                this.size.toLong()
            )
        }

        /**
         * Extension function that converts a QuizCreationObject to a Quiz entity.
         *
         * @param author The [QuizUser] creating the quiz
         * @return [Quiz] entity with null ID (to be assigned by database upon persistence)
         * @throws QuizCreationException if any [QuizCreationObject.answer] index is out of [QuizCreationObject.options]' bounds
         */
        fun QuizCreationObject.toQuiz(id: Long?, author: QuizUser): Quiz {
            if (this.answer.all { it in this.options.indices.toSet() }) {
                return Quiz(id, author, title, text, options, answer)
            } else {
                throw QuizCreationException("answer indices must correspond to valid option indices")
            }
        }
    }
}