package org.jenhan.engine

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.collection.IsCollectionWithSize
import org.jenhan.engine.TestHelper.Companion.toPage
import org.jenhan.engine.model.QuizRepository
import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.security.SecurityConfig
import org.jenhan.engine.service.WebQuizService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@SpringBootTest
@Import(SecurityConfig::class)
@AutoConfigureMockMvc
internal class QuizControllerIntegrationTest {
    @field:Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var webQuizService: WebQuizService
    @MockitoBean
    private lateinit var userRepository: UserRepository
    @MockitoBean
    private lateinit var quizRepository: QuizRepository

    private val mapper = ObjectMapper()
    private val testData = TestHelper.getInstance()

    @BeforeEach
    fun stubDB() {
        `when`(quizRepository.findById(0)).thenReturn(Optional.of(testData.quiz1))
        `when`(quizRepository.findById(1)).thenReturn(Optional.of(testData.quiz2))
        `when`(quizRepository.findAll(any(PageRequest::class.java)))
            .thenReturn(listOf(testData.quiz1, testData.quiz2).toPage(0, 10))

        `when`(userRepository.findByEmail(testData.testUser.email)).thenReturn(testData.testUser)
        `when`(userRepository.findByEmail(testData.testUser2.email)).thenReturn(testData.testUser2)

        val quizCompletions = listOf(testData.quiz1Completion, testData.quiz2Completion).sortedByDescending { it.completedAt }

        `when`(userRepository
                .findCompletedQuizzesByUser(testData.testUser, PageRequest.of(0,10,
                    Sort.by("completedAt").descending()
                ))
        ).thenReturn(quizCompletions.toPage(0, 10))
    }

    /*
    GET all quizzes / single quiz
     */
    @Test
    fun `GET all quizzes denied when unauthorized`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/quizzes"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `GET all quizzes returns correct list with MockUser`() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/quizzes?page=0")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].title").value(testData.quiz1DTO.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].text").value(testData.quiz1DTO.text))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].options", IsCollectionWithSize.hasSize<Any>(4)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].title").value(testData.quiz2DTO.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].text").value(testData.quiz2DTO.text))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].options", IsCollectionWithSize.hasSize<Any>(4)))
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `GET all quizzes with negative page RequestParam is bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/quizzes?page=-1")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `GET quiz by id denied without authentication`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/quizzes/0"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `GET quiz by id returns quiz DTO with MockUser`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/quizzes/0")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(testData.quiz1DTO.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(testData.quiz1DTO.text))
            .andExpect(MockMvcResultMatchers.jsonPath("$.options", IsCollectionWithSize.hasSize<Any>(4)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.correctOptions").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.answer").doesNotExist())
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `GET quiz with invalid id is NOTFOUND`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/quizzes/2")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    /*
    POST new quiz
     */

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `POST new quiz accepted with mocked user`() {
        val content = mapper.writeValueAsString(testData.quiz1CreateDTO).toByteArray()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `POST new quiz declined without authentication`() {
        val content = mapper.writeValueAsString(testData.quiz1CreateDTO).toByteArray()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    /*
    DELETE quiz
     */

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `DELETE quiz with correct credentials yields NO CONTENT`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/quizzes/0")
                .with(csrf())
        ). andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `DELETE quiz without Permission is FORBIDDEN`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `DELETE quiz without authentication is UNAUTHORIZED`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/quizzes/1")
                .with(csrf())
        ). andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `DELETE quiz with wrong quiz id yields NOT FOUND`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/quizzes/2")
                .with(csrf())
        ). andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    /*
    POST solve quiz
     */

    @Test
    fun `POST solve declined without authentication`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `POST solve with correct answer`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `POST solve with wrong answer`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionWrong).toByteArray()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `POST solve with invalid id is NOT FOUND`() {
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes/2/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    /*
    GET completed quizzes
     */

    @Test
    @WithMockUser(username = TestHelper.TEST_USER_NAME, roles = ["USER"])
    fun `GET completed quizzes with valid credentials returns correct JSON`() {
        //given : quiz was solved
        val content = mapper.writeValueAsString(testData.quiz1SolutionCorrect).toByteArray()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/quizzes/0/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
        // expect completed
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/quizzes/completed")
            .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageable").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(0))
    }

    @Test
    fun `GET completed quizzes without credentials is UNAUTHORIZED`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/quizzes/completed")
            .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}