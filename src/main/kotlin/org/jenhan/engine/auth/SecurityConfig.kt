package org.jenhan.engine.auth

import org.jenhan.engine.repositories.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(private val userRepository: UserRepository) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { matcherRegistry -> matcherRegistry
                .requestMatchers("/api/quizzes/**").authenticated()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                .anyRequest().permitAll()
            }
            .userDetailsService(UserDetailsServiceImpl(userRepository))
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()
}