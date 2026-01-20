package org.jenhan.engine.service

import org.jenhan.engine.TestHelper
import org.jenhan.engine.TestHelper.Companion.toPage
import org.jenhan.engine.exceptions.AuthenticationException
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.exceptions.PermissionException
import org.jenhan.engine.model.QuizRepository
import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.service.WebQuizService.Companion.toQuiz
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    private val testData = TestHelper.getInstance()



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
    Querying quizzes and completions
     */

    @ParameterizedTest
    @ValueSource(ints = [-1, -5, -10, Int.MIN_VALUE])
    @Throws(NotFoundException::class)
    fun `getQuiz for inValid quiz id throws NotFoundException`(id: Int) {
        assertThrows<NotFoundException> {
            quizService.getQuiz(id)
            verify(quizRepository).findById(id.toLong())
        }
    }
    @Test
    @Throws(NotFoundException::class)
    fun `getQuiz for valid quiz id returns quiz DTO`() {
        val quiz1 = quizService.getQuiz(0)
        verify(quizRepository).findById(0L)
        assertEquals(testData.quiz1DTO, quiz1)

        val quiz2 = quizService.getQuiz(1)
        verify(quizRepository).findById(1L)
        assertEquals(testData.quiz2DTO, quiz2)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 10, Int.MAX_VALUE])
    fun `getQuizzes submits corresponding PageRequest to repository`(page: Int) {
        quizService.getQuizzes(page)
        verify(quizRepository).findAll(PageRequest.of(page, 10))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 10, Int.MAX_VALUE])
    fun `getCompleted for valid user calls repository`(page: Int) {
        val pageRequest = PageRequest.of(page,10, Sort.Direction.DESC, "solved")

        quizService.getCompleted(testData.testUser1Details,page)
        verify(userRepository).findCompletedQuizzesByUser(testData.testUser, pageRequest)

        quizService.getCompleted(testData.testUser2Details,page)
        verify(userRepository).findCompletedQuizzesByUser(testData.testUser2, pageRequest)
    }

    @Test
    fun `getCompleted for unauthenticated throws exception`() {
        assertThrows<AuthenticationException> {
            quizService.getCompleted(null, 0)
        }
    }

    /*
    Adding and deleting quizzes
     */

    @Test
    fun `addQuiz for unauthenticated throws exception`() {
        assertThrows<AuthenticationException> { quizService.addQuiz(null, testData.quiz1CreateDTO) }
    }
    @Test
    fun `addQuiz saves quiz to repository`() {
        quizService.addQuiz(testData.testUser1Details, testData.quiz1CreateDTO)
        verify(quizRepository).save(testData.quiz1CreateDTO.toQuiz(null, testData.testUser))
    }

    @Test
    fun `deleteQuiz by author deletes quiz from repository`() {
        quizService.deleteQuiz(0, testData.testUser1Details)
        verify(quizRepository).delete(testData.quiz1)
    }

    @Test
    fun `deleteQuiz by another user throws PermissionException`() {
        assertThrows<PermissionException> {
            quizService.deleteQuiz(0, testData.testUser2Details)
        }
    }

    @Test
    fun `deleteQuiz by unauthenticated user throws AuthenticationException`() {
        assertThrows<AuthenticationException> {
            quizService.deleteQuiz(0, null)
        }
    }

    /*
    Solving quizzes
     */
    @Test
    fun `evaluateAnswer by invalid user throws AuthenticationException`() {
        assertThrows<AuthenticationException> {
            quizService.evaluateAnswer(null, 0, setOf(0))
        }
    }
    @Test
    fun `evaluateAnswer with wrong answer gives success=false`() {
        val responseEntity = quizService.evaluateAnswer(testData.testUser1Details, 0, setOf(2,0))
        assertNotNull(responseEntity.body)
        assertEquals(false, responseEntity.body?.success)
    }
    @Test
    fun `evaluateAnswer with correct answer gives success=true`() {
        val responseEntity = quizService.evaluateAnswer(testData.testUser1Details, 0, setOf(2))
        assertNotNull(responseEntity.body)
        assertEquals(true, responseEntity.body?.success)
    }
    @Test
    fun `evaluateAnswer with invalid quiz id throws NotFoundException`() {
        assertThrows<NotFoundException> {
            quizService.evaluateAnswer(testData.testUser1Details, 2, setOf(2))
        }
    }
}