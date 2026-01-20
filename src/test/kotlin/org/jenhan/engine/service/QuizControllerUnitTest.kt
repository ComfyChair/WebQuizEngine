package org.jenhan.engine.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.jenhan.engine.TestHelper
import org.jenhan.engine.TestHelper.Companion.toPage
import org.jenhan.engine.exceptions.AuthenticationException
import org.jenhan.engine.exceptions.NotFoundException
import org.jenhan.engine.exceptions.PermissionException
import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.security.SecurityConfig
import org.jenhan.engine.security.UserAdapter
import org.jenhan.engine.security.UserDetailsServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(QuizController::class)
@Import(SecurityConfig::class)
internal class QuizControllerUnitTest {
    @field:Autowired
    private lateinit var mockMvc: MockMvc
    @field:MockitoBean
    private lateinit var userDetailsServiceImpl: UserDetailsServiceImpl
    @field:MockitoBean
    private lateinit var webQuizService: WebQuizService

    private val mapper = ObjectMapper()
    private val testData = TestHelper.getInstance()

    /*
    GET all quizzes / single quiz
     */
    @Test
    fun `GET all quizzes denied when unauthorized`() {
        mockMvc.perform(get("/api/quizzes"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `GET all quizzes returns correct list with MockUser`() {
        `when`(webQuizService.getQuizzes(0))
            .thenReturn(PageImpl(listOf(testData.quiz1DTO, testData.quiz2DTO),
                PageRequest.of(0, 10), 2))

        mockMvc.perform(
            get("/api/quizzes?page=0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(0))
            .andExpect(jsonPath("$.content[0].title").value(testData.quiz1DTO.title))
            .andExpect(jsonPath("$.content[0].text").value(testData.quiz1DTO.text))
            .andExpect(jsonPath("$.content[0].options", hasSize<Any>(4)))
            .andExpect(jsonPath("$.content[1].id").value(1))
            .andExpect(jsonPath("$.content[1].title").value(testData.quiz2DTO.title))
            .andExpect(jsonPath("$.content[1].text").value(testData.quiz2DTO.text))
            .andExpect(jsonPath("$.content[1].options", hasSize<Any>(4)))
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `GET all quizzes with negative page RequestParam is bad request`() {
        mockMvc.perform(
            get("/api/quizzes?page=-1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET quiz by id denied without authentication`() {
        mockMvc.perform(get("/api/quizzes/0"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `GET quiz by id returns quiz DTO with MockUser`() {
        `when`(webQuizService.getQuiz(0)).thenReturn(testData.quiz1DTO)

        mockMvc.perform(
            get("/api/quizzes/0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.title").value(testData.quiz1DTO.title))
            .andExpect(jsonPath("$.text").value(testData.quiz1DTO.text))
            .andExpect(jsonPath("$.options", hasSize<Any>(4)))
            .andExpect(jsonPath("$.correctOptions").doesNotExist())
            .andExpect(jsonPath("$.answer").doesNotExist())
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `GET quiz with invalid id is NOTFOUND`() {
        `when`(webQuizService.getQuiz(anyInt())).thenThrow(NotFoundException("not found"))

        mockMvc.perform(
            get("/api/quizzes/1"))
            .andExpect(status().isNotFound)
    }

    /*
    POST new quiz
     */

    @Test
    fun `POST new quiz accepted with mocked user`() {
        val content = mapper.writeValueAsString(testData.quiz1CreateDTO).toByteArray()
        val postProcessor = SecurityMockMvcRequestPostProcessors
            .user(testData.testUser.email)
            .authorities(SimpleGrantedAuthority("USER"))
        val userDetails = UserAdapter(testData.testUser)
        `when`(webQuizService.addQuiz(userDetails, testData.quiz1CreateDTO))
            .thenReturn(testData.quiz1DTO)

        mockMvc.perform(
            post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(postProcessor)
                .with(csrf())
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `POST new quiz declined without authentication`() {
        val content = mapper.writeValueAsString(testData.quiz1CreateDTO).toByteArray()

        mockMvc.perform(
            post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(status().isUnauthorized)
    }

    /*
    DELETE quiz
     */

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `DELETE quiz with correct credentials yields NO CONTENT`() {
        mockMvc.perform(
            delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `DELETE quiz without Permission is FORBIDDEN`() {
        `when`(webQuizService.deleteQuiz(anyInt(), any(UserDetails::class.java)))
            .thenThrow(PermissionException("not allowed"))
        mockMvc.perform(
            delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(status().isForbidden)
    }

    @Test
    fun `DELETE quiz without authentication is UNAUTHORIZED`() {
        `when`(webQuizService.deleteQuiz(1, null))
            .thenThrow(AuthenticationException("not authenticated"))
        mockMvc.perform(
            delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `DELETE quiz with wrong quiz id yields NOT FOUND`() {
        `when`(webQuizService.deleteQuiz(anyInt(), any(UserDetails::class.java)))
            .thenThrow(NotFoundException("not found"))
        mockMvc.perform(
            delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(status().isNotFound)
    }

    /*
    POST solve quiz
     */

    @Test
    fun `POST solve declined without authentication`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()
        mockMvc.perform(
            post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `POST solve with correct answer`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()
        mockMvc.perform(
            post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `POST solve with wrong answer`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionWrong).toByteArray()
        mockMvc.perform(
            post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `POST solve with invalid id is NOT FOUND`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()

        `when`(webQuizService.evaluateAnswer(
            any(UserDetails::class.java), anyInt(), anySet() ))
            .thenThrow(NotFoundException("not found"))

        mockMvc.perform(
            post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(status().isNotFound)
    }

    /*
    GET completed quizzes
     */

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `GET completed quizzes with valid credentials returns correct JSON`() {
        val quizList = listOf(testData.quiz1Completion,testData.quiz2Completion).sortedByDescending { it.completedAt }
        `when`(webQuizService.getCompleted(
            any(UserDetails::class.java), anyInt()))
            .thenReturn(quizList.toPage(0,10))

        mockMvc.perform(get("/api/quizzes/completed")
            .with(csrf())
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.page").exists())
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[1].id").value(0))
    }

    @Test
    fun `GET completed quizzes without credentials is UNAUTHORIZED`() {
        val quizList = listOf(testData.quiz1Completion,testData.quiz2Completion).sortedByDescending { it.completedAt }
        `when`(webQuizService.getCompleted(
            any(UserDetails::class.java), anyInt()))
            .thenReturn(quizList.toPage(0,10))

        mockMvc.perform(get("/api/quizzes/completed")
            .with(csrf())
        ).andExpect(status().isUnauthorized)
    }
}