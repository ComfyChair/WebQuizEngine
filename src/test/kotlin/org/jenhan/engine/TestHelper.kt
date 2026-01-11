package org.jenhan.engine

import org.jenhan.engine.model.QuizCompletion
import org.jenhan.engine.model.QuizUser
import org.jenhan.engine.security.UserAdapter
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import org.jenhan.engine.service.dtos.Solution
import org.jenhan.engine.service.WebQuizService.Companion.toQuiz
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

class TestHelper private constructor() {

    val testUser = QuizUser(0,TEST_USER_NAME,TEST_USER_PASSWORD, authority = "ROLE_USER")
    val testUser1Details : UserDetails = UserAdapter(testUser)
    val testUser2 = QuizUser(1,TEST2_USER_NAME,TEST2_USER_PASSWORD, authority = "ROLE_USER")
    val testUser2Details : UserDetails = UserAdapter(testUser2)

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

    companion object {
        @Volatile
        private var instance: TestHelper? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TestHelper().also { instance = it }
            }

        const val TEST_USER_NAME = "testUser"
        const val TEST_USER_PASSWORD = "password"
        const val TEST2_USER_NAME = "someUser"
        const val TEST2_USER_PASSWORD = "secret"

        const val QUIZ1_TITLE = "The Java Logo"
        const val QUIZ1_TEXT = "What is depicted on the Java logo?"
        val QUIZ1_OPTIONS = listOf("Robot", "Tea leaf", "Cup of coffee", "Bug")
        const val QUIZ2_TITLE = "The Ultimate Question"
        const val QUIZ2_TEXT = "What is the answer to the Ultimate Question?"
        val QUIZ2_OPTIONS = listOf("Everything goes right","42","2+2=4","11011100")

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
    }
}