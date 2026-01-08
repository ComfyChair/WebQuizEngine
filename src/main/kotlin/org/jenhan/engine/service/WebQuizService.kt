package org.jenhan.engine.service

import org.jenhan.engine.exceptionhandling.AuthorizationException
import org.jenhan.engine.exceptionhandling.NotFoundException
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

@Service
class WebQuizService(
    private val quizzes: QuizRepository,
    private val userRepository: UserRepository,
) {

    fun getQuiz(quizId: Int): QuizDTO {
        return quizzes.findById(quizId.toLong())
            .getOrNull()?.toDTO() ?: throw NotFoundException("Index $quizId out of bounds: 0..${quizzes.count() - 1}")
    }

    fun getQuizzes(page: Int): Page<QuizDTO> {
        val page: Page<Quiz> = quizzes.findAll(PageRequest.of(page, 10))
        return page.map { it.toDTO() }
    }

    fun evaluateAnswer(userDetails: UserDetails?, quizId: Int, answerId: String?): ResponseEntity<SolveFeedback> {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).getOrNull() ?: throw NotFoundException("id does not correspond to a quiz in the database")
        val answers = answerId?.toIntOrNull()

        LOGGER.info("Evaluating answer {} for quiz with correct option of {}", answerId, quiz.correctOptions)
        return if (answers in quiz.correctOptions) {
            LOGGER.info("Answer: correct")
            user.solvedQuizzes.add(QuizCompletion(quizId, LocalDateTime.now()))
            userRepository.save(user)
            ResponseEntity.ok().body(SolveFeedback(true))
        } else {
            LOGGER.info("Answer: wrong")
            ResponseEntity.ok().body(SolveFeedback(false))
        }
    }

    fun addQuiz(userDetails: UserDetails?, quizCO: QuizCreationObject) : QuizDTO {
        val user = getUser(userDetails)
        val newQuiz = quizCO.toQuiz(user)
        quizzes.save(newQuiz)
        LOGGER.info("Added quiz with id {} to quizzes: {}", newQuiz.id, quizzes)
        return newQuiz.toDTO()
    }

    fun deleteQuiz(quizId: Int, userDetails: UserDetails?) {
        val user = getUser(userDetails)
        val quiz = quizzes.findById(quizId.toLong()).orElseThrow { NotFoundException("quiz with id $quizId does not exist") }
        if (user != quiz.author) throw AuthorizationException("user is not the author of this quiz and thus not allowed to delete it")
        quizzes.delete(quiz)
    }

    fun getCompleted(userDetails: UserDetails?, page: Int): PageImpl<QuizCompletion> {
        val user = getUser(userDetails)
        val quizzes = userRepository.findCompletedQuizzesByUser(user)
        return PageImpl<QuizCompletion>( quizzes, PageRequest.of(
            page,
            10,
            Sort.by("competedAt").descending()
        ), quizzes.size.toLong())
    }

    private fun getUser(userDetails: UserDetails?): QuizUser {
        if (userDetails == null) throw AuthorizationException("not authenticated")
        return userRepository.findByEmail(userDetails.username)!!
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WebQuizService::class.java)

        private fun QuizCreationObject.toQuiz(author: QuizUser): Quiz {
            return Quiz(null, author, title, text, options, listOf(answer))
        }
    }
}