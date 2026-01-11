package org.jenhan.engine.security

import org.jenhan.engine.TestHelper
import org.jenhan.engine.model.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class UserDetailsServiceImplTest {
    @field:MockitoBean
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    private val testData = TestHelper.getInstance()

    @BeforeEach
    fun stubDB() {
        `when`(userRepository.findByEmail(testData.testUser.email)).thenReturn(testData.testUser)
        `when`(userRepository.findByEmail(testData.testUser2.email)).thenReturn(testData.testUser2)
    }
    @Test
    fun `loading user by unknown user name throws UsernameNotFoundException`() {
        assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername("unknwonUser@email.com")
        }
    }
    @Test
    fun `loading known user returns correct UserDetails`() {
        val result = userDetailsService.loadUserByUsername(testData.testUser.email)
        assertEquals(UserAdapter::class.java, result.javaClass)
        assertEquals(testData.testUser.email, result.username)
        assertEquals(testData.testUser.pwHash, result.password)
        assertEquals(listOf(SimpleGrantedAuthority("ROLE_USER")), result.authorities)
        assertTrue(result.isAccountNonExpired)
        assertTrue(result.isAccountNonLocked)
        assertTrue(result.isCredentialsNonExpired)
        assertTrue(result.isEnabled)
    }
}