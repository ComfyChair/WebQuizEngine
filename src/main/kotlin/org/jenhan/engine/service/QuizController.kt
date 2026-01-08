package org.jenhan.engine.service

import org.jenhan.engine.repositories.QuizCompletion
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.SolveFeedback
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/quizzes"])
class QuizController(private val webQuizService: WebQuizService) {

    @GetMapping("")
    fun getQuizzes(
        @RequestParam @Min(0) page: Int
    ) = webQuizService.getQuizzes(page)

    @PostMapping("")
    fun postNewQuiz(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody @Valid quiz: QuizCreationObject
    ) = webQuizService.addQuiz(userDetails, quiz)

    @GetMapping("/{id}")
    fun getQuiz(@PathVariable("id") @Min(0) quizId: Int)
            = webQuizService.getQuiz(quizId)

    @PostMapping("/{id}/solve")
    fun postAnswer(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable("id") @Min(0) quizId: Int,
        @RequestParam answer: Map<String, String>
    ): ResponseEntity<SolveFeedback> {
        LOGGER.info("Posting answer {} for quiz id {}", answer["answer"], quizId)
        return webQuizService.evaluateAnswer(userDetails, quizId, answer["answer"])
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteQuiz(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable("id") @Min(0) quizId: Int,
    ) {
        LOGGER.info("Delete request for quiz id {}", quizId)
        webQuizService.deleteQuiz(quizId, userDetails)
    }

    @GetMapping("/completed")
    fun getCompletedQuizzes(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam @Min(0) page: Int,
    ): ResponseEntity<PageImpl<QuizCompletion>> {
        LOGGER.info("Getting completed quizzes page {} for user {}", page, userDetails?.username)
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(webQuizService.getCompleted(userDetails, page))
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(QuizController::class.java)
    }
}