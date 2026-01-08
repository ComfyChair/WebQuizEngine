package org.jenhan.engine.auth

import org.jenhan.engine.repositories.QuizUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserAdapter(private val quizUser: QuizUser) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(quizUser.authority))
    }

    override fun getPassword(): String = requireNotNull(quizUser.pwHash)

    override fun getUsername(): String = requireNotNull(quizUser.email)

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}