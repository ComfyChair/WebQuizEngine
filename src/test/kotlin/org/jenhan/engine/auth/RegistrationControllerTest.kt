package org.jenhan.engine.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jenhan.engine.security.registration.RegistrationController
import org.jenhan.engine.security.registration.RegistrationRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
class RegistrationControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun `register user accepted for valid credentials`() {
        val request = RegistrationRequest(
            "someone@somewhere.com",
            "password"
        )
        val objectMapper = jacksonObjectMapper()
        val requestAsJson = objectMapper.writeValueAsString(request)
        println("Request: $requestAsJson")

        mockMvc.perform(post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)
            .with(csrf())
        ).andExpect(status().isOk)
            .andExpect(content().string(RegistrationController.REGISTRATION_SUCCESSFUL))
    }

    @Test
    fun `register user declined for email lacking domain part`() {
        val requestAsJson = "{" +
                "\"email\": \"anyone@somewhere\"," +
                "\"password\": \"password\""+
                "}"

        mockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson)
                .with(csrf())
        ).andExpect(status().isBadRequest)
            .andExpect(content().string(RegistrationController.INVALID_CREDENTIALS))
    }

    @Test
    fun `register user declined for short password`() {
        val requestAsJson = "{\n" +
                "\"email\": \"anyone@somewhere.com\",\n" +
                "\"password\": \"pass\""+
                "\n}"

        mockMvc.perform(post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)
            .with(csrf())
            ).andExpect(status().isBadRequest)
            .andExpect(content().string(RegistrationController.INVALID_CREDENTIALS))
    }

    @Test
    fun `register user declined if email already in use`() {
        val requestAsJson = "{\n" +
                "\"email\": \"anyone@somewhere.com\",\n" +
                "\"password\": \"password\""+
                "\n}"

        val requestBuilder = post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)
            .with(csrf())

        // First request is valid
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
        // Second request with same email is declined
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
    }
}