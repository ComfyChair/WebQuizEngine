package org.jenhan.engine.service

import org.jenhan.engine.model.QuizCompletion
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import org.jenhan.engine.exceptions.AuthenticationException
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.service.dtos.SolutionFeedback
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.jenhan.engine.service.dtos.Solution
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*


/**
 * REST controller for quiz-related operations.
 *
 * Provides endpoints for retrieving, creating, deleting quizzes, submitting answers,
 * and accessing quiz completion history. All endpoints are mapped under `/api/quizzes`.
 *
 * @property webQuizService Service layer handling business logic for quiz operations
 */
@RestController
@RequestMapping(value = ["/api/quizzes"])
class QuizController(private val webQuizService: WebQuizService) {

    /**
     * Retrieves a paginated list of all quizzes.
     *
     * **Endpoint:** `GET /api/quizzes`
     *
     * @param page Zero-based page number, defaults to 0
     * @return [Page] of [QuizDTO] objects containing 10 items per page
     */
    @GetMapping("")
    fun getQuizzes(
        @RequestParam @Min(0) page: Int = 0
    ) = webQuizService.getQuizzes(page)

    /**
     * Creates a new quiz authored by the authenticated user.
     *
     * **Endpoint:** `POST /api/quizzes`
     *
     * @param userDetails Authentication details of the current user (injected by Spring Security)
     * @param quiz [QuizCreationObject] containing title, text, options, and correct answer
     * @return [QuizDTO] of the newly created quiz
     * @throws AuthenticationException if user is not authenticated
     */
    @PostMapping("")
    fun postNewQuiz(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody @Valid quiz: QuizCreationObject
    ) = webQuizService.addQuiz(userDetails, quiz)

    /**
     * Retrieves a single quiz by its ID.
     *
     * **Endpoint:** `GET /api/quizzes/{id}`
     *
     * @param quizId The unique identifier of the quiz (must be >= 0)
     * @return [QuizDTO] containing the quiz information
     * @throws NotFoundException if the quiz ID doesn't exist
     */
    @GetMapping("/{id}")
    fun getQuiz(@PathVariable("id") quizId: Int)
            = webQuizService.getQuiz(quizId)


    /**
     * Submits an answer to a quiz for evaluation.
     *
     * **Endpoint:** `POST /api/quizzes/{id}/solve`
     *
     * Evaluates the submitted answer against the quiz's correct options. If correct,
     * records a QuizCompletion entry for the authenticated user.
     *
     * @param userDetails Authentication details of the current user (injected by Spring Security)
     * @param quizId The ID of the quiz being answered (must be >= 0)
     * @param answer [Solution] containing quiz id and multiple choice answer
     * @return [ResponseEntity] with [SolutionFeedback] indicating whether the answer was correct
     * @throws NotFoundException if the quiz ID doesn't exist
     * @throws AuthenticationException if user is not authenticated
     */
    @PostMapping("/{id}/solve")
    fun postAnswer(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable("id") quizId: Int,
        @RequestBody answer: Solution
    ) = webQuizService.evaluateAnswer(userDetails, quizId, answer.answer)

    /**
     * Deletes a quiz if the authenticated user is its author.
     *
     * **Endpoint:** `DELETE /api/quizzes/{id}`
     *
     * **Response Status:** 204 No Content on success
     *
     * @param userDetails Authentication details of the current user (injected by Spring Security)
     * @param quizId The ID of the quiz to delete (must be >= 0)
     * @throws NotFoundException if the quiz ID doesn't exist
     * @throws AuthenticationException if user is not authenticated or not the quiz author
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteQuiz(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable("id") quizId: Int,
    ) {
        LOGGER.debug("Delete request for quiz id {}", quizId)
        webQuizService.deleteQuiz(quizId, userDetails)
    }

    /**
     * Retrieves a paginated list of quizzes completed by the authenticated user.
     *
     * **Endpoint:** `GET /api/quizzes/completed`
     *
     * Results are sorted by completion date in descending order (most recent first).
     *
     * @param userDetails Authentication details of the current user (injected by Spring Security)
     * @param page Zero-based page number (must be >= 0)
     * @return [ResponseEntity] containing a [Page] of [QuizCompletion] objects with 10 items per page
     * @throws AuthenticationException if user is not authenticated
     */
    @GetMapping("/completed")
    fun getCompletedQuizzes(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam @Min(0) page: Int = 0,
    ): ResponseEntity<PageImpl<QuizCompletion>> {
        LOGGER.debug("Getting completed quizzes page {} for user {}", page, userDetails?.username)
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(webQuizService.getCompleted(userDetails, page))
    }

    companion object {
        /** Logger instance for this controller */
        private val LOGGER = LoggerFactory.getLogger(QuizController::class.java)
    }
}