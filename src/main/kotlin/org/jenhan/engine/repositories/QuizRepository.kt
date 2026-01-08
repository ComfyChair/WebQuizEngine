package org.jenhan.engine.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface QuizRepository : PagingAndSortingRepository<Quiz, Long>, CrudRepository<Quiz, Long>