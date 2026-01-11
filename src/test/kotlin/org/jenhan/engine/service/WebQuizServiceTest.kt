package org.jenhan.engine.service

import org.jenhan.engine.TestData.Companion.quiz1
import org.jenhan.engine.TestData.Companion.quiz2
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.model.QuizRepository
import org.jenhan.engine.service.WebQuizService.Companion.toPage
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun stubDB() {
        `when`(quizRepository.findById(0)).thenReturn(Optional.of(quiz1))
        `when`(quizRepository.findById(1)).thenReturn(Optional.of(quiz2))
        `when`(quizRepository.findAll(any(PageRequest::class.java)))
            .thenReturn(listOf(quiz1, quiz2).toPage(0,10))
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, -5, -10, Int.MIN_VALUE])
    @Throws(NotFoundException::class)
    fun getQuizForInValidID(id: Int) {
        assertThrows<NotFoundException> {
            quizService.getQuiz(id)
            verify(quizRepository).findById(id.toLong())
        }
    }
    @ParameterizedTest
    @ValueSource(ints = [0, 1])
    @Throws(NotFoundException::class)
    fun getQuizForValidID(id: Int) {
        assertDoesNotThrow {
            val quiz = quizService.getQuiz(id)
            verify(quizRepository).findById(id.toLong())
            assertNotNull(quiz)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 10, Int.MAX_VALUE])
    fun getPagedQuizzes(page: Int) {
        quizService.getQuizzes(page)
        verify(quizRepository).findAll(PageRequest.of(page, 10))
    }
}