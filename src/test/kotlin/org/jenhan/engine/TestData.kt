package org.jenhan.engine

import org.jenhan.engine.model.QuizCompletion
import org.jenhan.engine.model.QuizUser
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import org.jenhan.engine.service.dtos.Solution
import org.jenhan.engine.service.WebQuizService.Companion.toQuiz
import java.time.LocalDateTime

class TestData {
    companion object {
        const val TEST_USER_NAME = "testUser"
        const val TEST_USER_PASSWORD = "password"
        const val TEST2_USER_NAME = "someUser"
        const val TEST2_USER_PASSWORD = "secret"

        val testUser = QuizUser(0,TEST_USER_NAME,TEST_USER_PASSWORD)
        val testUser2 = QuizUser(1,TEST2_USER_NAME,TEST2_USER_PASSWORD)

        const val QUIZ1_TITLE = "The Java Logo"
        const val QUIZ1_TEXT = "What is depicted on the Java logo?"
        val QUIZ1_OPTIONS = listOf("Robot", "Tea leaf", "Cup of coffee", "Bug")
        const val QUIZ2_TITLE = "The Ultimate Question"
        const val QUIZ2_TEXT = "What is the answer to the Ultimate Question?"
        val QUIZ2_OPTIONS = listOf("Everything goes right","42","2+2=4","11011100")

        val quiz1CreateDTO = QuizCreationObject(QUIZ1_TITLE, QUIZ1_TEXT,QUIZ1_OPTIONS, setOf(2))
        val quiz1DTO = QuizDTO(0,QUIZ1_TITLE,QUIZ1_TEXT,QUIZ1_OPTIONS)
        val quiz1 = quiz1CreateDTO.toQuiz(0L, testUser)
        val quiz2CreateDTO = QuizCreationObject(QUIZ2_TITLE, QUIZ2_TEXT,QUIZ2_OPTIONS, setOf(1))
        val quiz2DTO = QuizDTO(1,QUIZ2_TITLE,QUIZ2_TEXT,QUIZ2_OPTIONS)
        val quiz2 = quiz2CreateDTO.toQuiz(1L, testUser2)

        val quiz1SolutionCorrect = Solution(0, setOf(2))
        val quiz1SolutionWrong = Solution(0, setOf(2, 1))

        val quiz1Completion = QuizCompletion(quiz1DTO.id, LocalDateTime.of(2025, 1, 1, 0, 0))
        val quiz2Completion = QuizCompletion(quiz2DTO.id, LocalDateTime.of(2026, 1, 1, 0, 0))


    }
}