package org.jenhan.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.web.config.EnableSpringDataWebSupport

/**
 * Main application class for the Web Quiz Engine.
 *
 * Spring Boot application that provides a RESTful API for creating, managing,
 * and solving quizzes with user authentication and completion tracking.
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class WebQuizEngine


/**
 * Application entry point.
 *
 * Bootstraps and launches the Spring Boot Web Quiz Engine application.
 *
 * @param args Command-line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<WebQuizEngine>(*args)
}
