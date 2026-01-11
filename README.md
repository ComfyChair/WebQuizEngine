![MIT License](https://img.shields.io/badge/license-MIT-green)
# WebQuizEngine

A backend-driven quiz engine with REST API, user authentication, and persistent storage, written in Kotlin.

This project implements a production-oriented backend service that allows users to register, authenticate, create quizzes,
solve them, and track results.

Built as part of the JetBrains Academy Kotlin Backend Deleoper course, to get hands-on experience with RESTful APIs
and server-side Kotlin development on the JVM.

---

## 🧠 Overview

WebQuizEngine is a RESTful backend service for managing and solving quizzes.

Authenticated users can:
- create and delete quizzes,
- solve quizzes created by others,
- submit answers and receive immediate feedback,
- track completed quizzes.

The project demonstrates core backend skills including **REST API design, authentication and database persistence**.

---

## 🚀 Features

- 🔐 **User authentication & authorization**
    - User registration and login
    - Protected endpoints
- 📦 **REST API** for quiz management
- 📝 Quiz creation and deletion
- ✅ Quiz solving with answer validation
- 📊 Tracking of completed quizzes per user
- 🗄️ **Persistent storage** using JPA / Hibernate
- ⚙️ Layered backend architecture

----


## 🛠️ Tech Stack

- **Language:** Kotlin (JVM)
- **Framework:** Spring Boot
- **Build Tool:** Gradle (Kotlin DSL)
- **Persistence:** JPA / Hibernate
- **Database:** H2
- **Security:** Spring Security (HTTP Basic)
- **Serialization:** Jackson
- **API Style:** REST (JSON)

---

## 🧩 Architecture Overview

The application follows a layered architecture:
* controller → REST endpoints
* service    → business logic
* repository → data access (JPA)
* model → entities / DTOs

----

## 📡 API Endpoints
### Authentication
| Method | Endpoint        | Description            |
|--------|-----------------|------------------------|
| POST   | `/api/register` | Register a new user    |


### Quizzes
| Method | Endpoint               | Description                     |
|--------|------------------------|---------------------------------|
| POST   | `/api/quizzes`         | Create a new quiz (auth)        |
| GET    | `/api/quizzes`         | Get all quizzes (paginated)     |
| GET    | `/api/quizzes/{id}`    | Get quiz by id                  |
| DELETE | `/api/quizzes/{id}`    | Delete quiz (owner only)        |


### Solving Quizzes
| Method | Endpoint                        | Description                  |
|--------|---------------------------------|------------------------------|
| POST   | `/api/quizzes/{id}/solve`       | Submit answer                |
| GET    | `/api/quizzes/completed`        | Get completed quizzes (auth) |

---

## 🧾 Example Requests

### Register User
curl -X POST http://localhost:8889/api/register \\ \
     -H "Content-Type: application/json" \\ \
     -d '{"email":"user@example.com","password":"secret"}'

### Create Quiz
curl -X POST http://localhost:8889/api/quizzes \\ \
-u user@example.com:secret \\ \
-H "Content-Type: application/json" \\ \
-d '{
"title": "Math Quiz",
"text": "What is 2 + 2?",
"options": ["3", "4", "5"],
"answer": [1]
}'

### Solve Quiz
curl -X POST http://localhost:8889/api/quizzes/1/solve \\ \
-u user@example.com:secret \\ \
-H "Content-Type: application/json" \\  \
-d '{"answer":[1]}'

### DELETE Quiz
curl -X DELETE http://localhost:8889/api/quizzes/1 \\ \
-u user@example.com:secret

----

## 🗄️ Data Persistence

* Entities are mapped using JPA annotations
* Hibernate handles ORM and schema generation
* Relational database used for:
  * users
  * quizzes

The persistence layer is abstracted through repositories to keep business logic independent of the database.

----
## 🧪 Testing

* Unit tests for service layer
* Integration tests for REST endpoints
* Technologies: 
  * JUnit 5
  * Mockito
  * MockMvc 

----

## 📈 What I Learned

* Designing RESTful APIs with Kotlin and Spring Boot
* Implementing authentication and access control
* Using JPA/Hibernate for persistence
* Structuring backend applications cleanly
* Building production-style backend services on the JVM
