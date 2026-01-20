package org.jenhan.engine.security

import org.jenhan.engine.model.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security configuration for the Web Quiz Engine application.
 *
 * Configures authentication, authorization, and security policies including:
 * - Password encoding using [BCryptPasswordEncoder]
 * - HTTP Basic authentication
 * - Request authorization rules
 * - default CSRF protection (disable for testing with Postman or curl)
 *
 * @property userDetailsServiceImpl UserDetailsServiceImpl for loading user details during authentication
 */
@Configuration
class SecurityConfig(private val userDetailsServiceImpl: UserDetailsServiceImpl) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { matcherRegistry -> matcherRegistry
                .requestMatchers("/api/quizzes/**").authenticated()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                .anyRequest().denyAll()
            }
            .userDetailsService(userDetailsServiceImpl)
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() } // enable for production
            .build()
}