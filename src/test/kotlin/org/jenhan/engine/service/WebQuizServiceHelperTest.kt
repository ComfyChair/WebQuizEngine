package org.jenhan.engine.service

import org.jenhan.engine.TestData
import org.jenhan.engine.exceptions.QuizCreationException
import org.jenhan.engine.service.WebQuizService.Companion.toQuiz
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WebQuizServiceHelperTest {
    val testData = TestData.getInstance()

    @Test
    @Throws(QuizCreationException::class)
    fun `quiz creation from QuizCreationObjects throws exception for negative answer index`() { val original = testData.quiz1CreateDTO
        val negativeIndex = original.copy(answer = setOf(-1,1))

        assertThrows<QuizCreationException> {
            negativeIndex.toQuiz(null, testData.testUser)
        }
    }
    @Test
    @Throws(QuizCreationException::class)
    fun `quiz creation from QuizCreationObjects throws exception for too large answer index`() {
        val original = testData.quiz1CreateDTO
        val bigIndex = original.copy(answer = setOf(0, original.options.size))
        assertThrows<QuizCreationException> {
            bigIndex.toQuiz(null, testData.testUser)
        }
    }
}