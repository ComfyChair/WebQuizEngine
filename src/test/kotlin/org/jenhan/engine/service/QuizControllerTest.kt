package org.jenhan.engine.service

import org.jenhan.engine.auth.SecurityConfig
import org.jenhan.engine.repositories.QuizUser
import org.jenhan.engine.auth.UserAdapter
import org.jenhan.engine.auth.UserDetailsServiceImpl
import org.jenhan.engine.repositories.UserRepository
import org.jenhan.engine.service.dtos.QuizCreationObject
import org.jenhan.engine.service.dtos.QuizDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(QuizController::class)
@Import(SecurityConfig::class)
internal class QuizControllerTest {
    @field:Autowired
    private lateinit var mockMvc: MockMvc
    @field:MockBean
    private lateinit var userRepository: UserRepository
    @field:MockBean
    private lateinit var userDetailService: UserDetailsServiceImpl
    @field:MockBean
    private lateinit var webQuizService: WebQuizService

    private val quiz1CreateDTO = QuizCreationObject(QUIZ1_TITLE, QUIZ1_TEXT,QUIZ1_OPTIONS, 2)
    private val quiz1 = QuizDTO(0,QUIZ1_TITLE,QUIZ1_TEXT,QUIZ1_OPTIONS)
    private val quiz2CreateDTO = QuizCreationObject(QUIZ2_TITLE, QUIZ2_TEXT,QUIZ2_OPTIONS,1)
    private val quiz2 = QuizDTO(1,QUIZ2_TITLE,QUIZ2_TEXT,QUIZ2_OPTIONS)

    private val testUser = QuizUser(0,TEST_USER_NAME,TEST_USER_PASSWORD)

    @Test
    fun `GET all quizzes denied for unauthorized`() {
        val requestBuilder = get("/api/quizzes")

        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "someUser", roles = ["USER"])
    fun `GET all quizzes accepted with MockUser`() {
        val requestBuilder = get("/api/quizzes?page=0")
        `when`(webQuizService.getQuizzes(0))
            .thenReturn(PageImpl(listOf(quiz1, quiz2), PageRequest.of(0, 10), 2))

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(0))
            .andExpect(jsonPath("$.content[0].title").value(quiz1.title))
            .andExpect(jsonPath("$.content[0].text").value(quiz1.text))
            .andExpect(jsonPath("$.content[0].options", hasSize<Any>(4)))
            .andExpect(jsonPath("$.content[1].id").value(1))
            .andExpect(jsonPath("$.content[1].title").value(quiz2.title))
            .andExpect(jsonPath("$.content[1].text").value(quiz2.text))
            .andExpect(jsonPath("$.content[1].options", hasSize<Any>(4)))
    }

    @Test
    fun `GET quiz by id denied without authentication`() {
        val requestBuilder = get("/api/quizzes/0")
        `when`(webQuizService.getQuiz(0)).thenReturn(quiz1)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "someUser", roles = ["USER"])
    fun `GET quiz by id accepted with MockUser`() {
        val requestBuilder = get("/api/quizzes/0")
        `when`(webQuizService.getQuiz(0)).thenReturn(quiz1)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.title").value(quiz1.title))
            .andExpect(jsonPath("$.text").value(quiz1.text))
            .andExpect(jsonPath("$.options", hasSize<Any>(4)))
    }

    @Test
    fun `POST new quiz accepted with mocked user`() {
        val content = Json.encodeToString(quiz1CreateDTO).toByteArray()
        val postProcessor = SecurityMockMvcRequestPostProcessors
            .user(testUser.email)
            .authorities(SimpleGrantedAuthority("USER"))
        val userDetails = UserAdapter(testUser)
        `when`(webQuizService.addQuiz(userDetails, quiz1CreateDTO))
            .thenReturn(quiz1)

        val requestBuilder = post("/api/quizzes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .with(postProcessor)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
    }

    @Test
    fun `POST new quiz declined without authentication`() {
        val userDetails = UserAdapter(testUser)
        `when`(webQuizService.addQuiz(userDetails, quiz1CreateDTO)).thenReturn(quiz1)

        val requestBuilder = get("/api/quizzes")

        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnauthorized)
    }

    companion object {
        const val TEST_USER_NAME = "testuser"
        const val TEST_USER_PASSWORD = "password"

        const val QUIZ1_TITLE = "The Java Logo"
        const val QUIZ1_TEXT = "What is depicted on the Java logo?"
        val QUIZ1_OPTIONS = listOf("Robot", "Tea leaf", "Cup of coffee", "Bug")
        const val QUIZ2_TITLE = "The Ultimate Question"
        const val QUIZ2_TEXT = "What is the answer to the Ultimate Question?"
        val QUIZ2_OPTIONS = listOf("Everything goes right","42","2+2=4","11011100")
    }
}