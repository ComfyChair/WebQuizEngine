package org.jenhan.engine.security

import org.jenhan.engine.model.UserRepository
import org.jenhan.engine.model.QuizUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Custom implementation of Spring Security's [UserDetailsService].
 *
 * Provides user authentication by loading user details from the database based on email address.
 * Integrates the application's [QuizUser] entities with Spring Security's authentication mechanism
 * by wrapping them in [UserAdapter] objects.
 *
 * @property repository Repository for accessing [QuizUser] data
 */
@Service
class UserDetailsServiceImpl(private val repository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = repository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return UserAdapter(user)
    }
}