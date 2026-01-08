package org.jenhan.engine.auth

import org.jenhan.engine.auth.registration.RegistrationRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerMockMvcTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun `register user accepted for valid credentials`() {
        val request = RegistrationRequest(
            "someone@somewhere.com",
            "password"
        )
        val requestAsJson = Json.encodeToString(request)

        val requestBuilder = post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
    }

    @Test
    fun `register user declined for email lacking domain part`() {
        val requestAsJson = "{" +
                "\"email\": \"anyone@somewhere\"," +
                "\"password\": \"password\""+
                "}"

        val requestBuilder = post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register user declined for short password`() {
        val requestAsJson = "{\n" +
                "\"email\": \"anyone@somewhere.com\",\n" +
                "\"password\": \"pass\""+
                "\n}"

        val requestBuilder = post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAsJson)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
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

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
    }
}