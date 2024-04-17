package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.entities.user.AuthUserRequest
import com.belajar.api.kotlin.model.User
import com.belajar.api.kotlin.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var mockUser: User

    val name = "name"
    val email = "test@gmail.com"
    val password = "123pas"
    val createdAt = Date()
    private final val baseUrl = "http://localhost:8080"
    val url = "$baseUrl/api/auth"

    private fun createUser(name: String, email: String, password: String, createdAt: Date): User {
        return User().apply {
            this.name = name
            this.email = email
            this.password = password
            this.createdAt = createdAt
        }
    }

    @BeforeAll
    @Transactional
    fun setUp() {
        mockUser = createUser (name, email, password, createdAt)
        userRepository.save(mockUser)
    }

    @AfterAll
    @Transactional
    fun deleteUser() {
        userRepository.delete(mockUser)
    }

    @Test
    @Order(1)
    fun `should response 200 and new authentications`() {
        val request = AuthUserRequest(email, password)

        val response = mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.data").value("Login success!!"))
            .andReturn().response as MockHttpServletResponse

        val jwtCookie = response.getCookie("jwt")
        Assertions.assertNotNull(jwtCookie)
        Assertions.assertTrue(jwtCookie!!.isHttpOnly)
    }

    @Test
    @Order(2)
    fun `Should response 422 when login payload is unprocessable`() {
        val request = AuthUserRequest("invalid_email", "$%£&")

        mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(422))
            .andExpect(jsonPath("$.status").value("UNPROCESSABLE_ENTITY"))
            .andExpect(jsonPath("$.data[*].path").exists())
            .andExpect(jsonPath("$.data[*].message").exists())
    }

    @Test
    @Order(3)
    fun `Should response 422 when login with password don't match`() {
        val request = AuthUserRequest(email, "wrong")

        mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(422))
            .andExpect(jsonPath("$.status").value("UNPROCESSABLE_ENTITY"))
            .andExpect(jsonPath("$.data[0].path").value("password"))
            .andExpect(jsonPath("$.data[0].message").value("Password doesn't match"))
    }

    @Test
    @Order(4)
    fun `Should response 404 when login with email not found`() {
        val request = AuthUserRequest("notfound@gmail.com", password)

        mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.status").value("NOT_FOUND"))
            .andExpect(jsonPath("$.data").value("User Not Found"))
    }

    @Test
    @Order(5)
    fun `Should logout successfully and remove JWT cookie`() {
        val request = AuthUserRequest(email, password)

        val response = mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andReturn().response as MockHttpServletResponse

        val jwtCookie = response.getCookie("jwt")

        mockMvc.perform(delete(url)
            .cookie(jwtCookie)) // Mengirim cookie JWT dalam permintaan
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.data").value("Logout success!!"))
            .andExpect(cookie().maxAge("jwt", 0))
    }

    @Test
    @Order(6)
    fun `Should response 401 when logout with invalid jwt`() {
        val cookie = Cookie("jwt", "invalid_jwt")

        mockMvc.perform(delete(url)
            .cookie(cookie))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.data").value("Invalid JWT token"))
    }

    @Test
    @Order(7)
    fun `Should response 401 when logout with null jwt`() {

        mockMvc.perform(delete(url))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.data").value("JWT token is null"))
    }

}
