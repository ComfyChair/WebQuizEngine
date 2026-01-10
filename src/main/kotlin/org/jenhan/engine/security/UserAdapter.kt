package org.jenhan.engine.security

import org.jenhan.engine.model.QuizUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Adapter class that wraps a [QuizUser] entity to provide Spring Security's [UserDetails] interface.
 *
 * Converts the application's [QuizUser] model into the format required by Spring Security
 * for authentication and authorization. All account status methods return true,
 * indicating that accounts are always active and non-expired.
 *
 * @property quizUser The QuizUser entity to adapt for Spring Security
 */
class UserAdapter(private val quizUser: QuizUser) : UserDetails {
    /**
     * Returns the authorities granted to the user.
     *
     * Converts the user's single authority string into a collection of GrantedAuthority objects
     * required by Spring Security.
     *
     * @return Collection containing the user's authority (e.g., "ROLE_USER")
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(quizUser.authority))
    }

    /**
     * Returns the password hash used for authentication.
     *
     * @return The [QuizUser]'s hashed password
     */
    override fun getPassword(): String = requireNotNull(quizUser.pwHash)

    /**
     * Returns the username used for authentication.
     *
     * @return The [QuizUser]'s email address
     */
    override fun getUsername(): String = requireNotNull(quizUser.email)

    /**
     * Indicates whether the user's account has expired.
     *
     * @return Always true (accounts never expire in this implementation)
     */
    override fun isAccountNonExpired(): Boolean = true

    /**
     * Indicates whether the user's account is locked.
     *
     * @return Always true (accounts are never locked in this implementation)
     */
    override fun isAccountNonLocked(): Boolean = true


    /**
     * Indicates whether the user's credentials have expired.
     *
     * @return Always true (credentials never expire in this implementation)
     */
    override fun isCredentialsNonExpired(): Boolean = true


    /**
     * Indicates whether the user's account is enabled.
     *
     * @return Always true (all accounts are enabled in this implementation)
     */
    override fun isEnabled(): Boolean = true
}