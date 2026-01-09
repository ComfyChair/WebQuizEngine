package org.jenhan.engine.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

/**
 * Repository interface for Quiz entity data access.
 *
 * Provides CRUD operations, pagination, and sorting capabilities for managing quiz entities.
 * Extends both PagingAndSortingRepository and CrudRepository to provide comprehensive
 * data access functionality including paginated retrieval of quizzes.
 */
interface QuizRepository : PagingAndSortingRepository<Quiz, Long>, CrudRepository<Quiz, Long>