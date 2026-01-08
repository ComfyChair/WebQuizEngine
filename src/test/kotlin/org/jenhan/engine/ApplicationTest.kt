package org.jenhan.engine

import org.jenhan.engine.service.QuizController
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    @Autowired
    private lateinit var quizController: QuizController

    @Test
    fun contextLoads(){
        assertNotNull(applicationContext)
    }
    @Test
    fun controllerPresent(){
        quizController.getQuizzes(0)
        assertNotNull(quizController)
    }
}