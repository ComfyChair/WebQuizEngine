package org.jenhan.engine.service

import org.jenhan.engine.TestData
import org.jenhan.engine.exceptions.AuthenticationException
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.model.QuizRepository
import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.service.WebQuizService.Companion.toPage
import org.jenhan.engine.service.WebQuizService.Companion.toQuiz
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@SpringBootTest
internal class WebQuizServiceTest {
    @field:Autowired
    private lateinit var quizService: WebQuizService
    @field:MockitoBean
    private lateinit var quizRepository: QuizRepository
    @field:MockitoBean
    private lateinit var userRepository: UserRepository

    private val testData = TestData.getInstance()

    @BeforeEach
    fun stubDB() {
        `when`(quizRepository.findById(0)).thenReturn(Optional.of(testData.quiz1))
        `when`(quizRepository.findById(1)).thenReturn(Optional.of(testData.quiz2))
        `when`(quizRepository.findAll(any(PageRequest::class.java)))
            .thenReturn(listOf(testData.quiz1, testData.quiz2).toPage(0,10))

        `when`(userRepository.findByEmail(testData.testUser.email)).thenReturn(testData.testUser)
        `when`(userRepository.findByEmail(testData.testUser2.email)).thenReturn(testData.testUser2)
    }

    /*
    GET services tests
     */

    @ParameterizedTest
    @ValueSource(ints = [-1, -5, -10, Int.MIN_VALUE])
    @Throws(NotFoundException::class)
    fun `get quiz for inValid quiz id throws NotFoundException`(id: Int) {
        assertThrows<NotFoundException> {
            quizService.getQuiz(id)
            verify(quizRepository).findById(id.toLong())
        }
    }
    @Test
    @Throws(NotFoundException::class)
    fun `GET quizzes for valid quiz id returns quiz DTO`() {
        assertDoesNotThrow {
            val quiz = quizService.getQuiz(0)
            verify(quizRepository).findById(0L)
            assertNotNull(quiz)
            assertEquals(testData.quiz1DTO, quiz)
        }
        assertDoesNotThrow {
            val quiz = quizService.getQuiz(1)
            verify(quizRepository).findById(1L)
            assertNotNull(quiz)
            assertEquals(testData.quiz2DTO, quiz)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 10, Int.MAX_VALUE])
    fun `GET quizzes submits corresponding PageRequest to repository`(page: Int) {
        quizService.getQuizzes(page)
        verify(quizRepository).findAll(PageRequest.of(page, 10))
    }

    @Test
    fun `GET completed for valid user calls repository`() {
        quizService.getCompleted(testData.testUser1Details,0)
        verify(userRepository).findCompletedQuizzesByUser(testData.testUser)

        quizService.getCompleted(testData.testUser2Details,0)
        verify(userRepository).findCompletedQuizzesByUser(testData.testUser2)
    }

    @Test
    fun `GET completed for unauthenticated throws exception`() {
        assertThrows<AuthenticationException> { quizService.getCompleted(null, 0) }
    }

    /*
     POST services tests
     */
    @Test
    fun `POST add quiz for unauthenticated throws exception`() {
        assertThrows<AuthenticationException> { quizService.addQuiz(null, testData.quiz1CreateDTO) }
    }
    @Test
    fun `POST add quiz saves quiz to repository`() {
        quizService.addQuiz(testData.testUser1Details, testData.quiz1CreateDTO)
        verify(quizRepository).save(testData.quiz1CreateDTO.toQuiz(null, testData.testUser))
    }
}