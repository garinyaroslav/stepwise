# Stepwise

Stepwise is a Spring Boot application designed for stage-by-stage tracking and management of academic projects, such as theses and coursework. It provides a RESTful API with JWT-based authentication, uses PostgreSQL with Hibernate for data persistence, and integrates MinIO for file storage.

## Features

- **User Authentication**: Secure JWT-based authentication for users (students, supervisors, etc.).
- **Project Management**: Track and manage academic projects with defined stages and deadlines.
- **File Storage**: Store project-related files (documents, presentations) using MinIO.
- **Database**: Persistent storage of user and project data using PostgreSQL with Hibernate ORM.
- **REST API**: Exposes endpoints for managing users, projects, stages, and files.

## Tech Stack

- **Spring Boot**: Backend framework for building the REST API.
- **Spring Security**: For JWT-based authentication and authorization.
- **Spring Data JPA**: For interacting with PostgreSQL via Hibernate.
- **PostgreSQL**: Relational database for storing user and project data.
- **MinIO**: Object storage for managing project files.
- **JWT (jjwt)**: For generating and validating JSON Web Tokens.
- **Lombok**: To reduce boilerplate code.

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL 15+**
- **MinIO** (local or remote instance)
- **Docker** (optional, for running PostgreSQL and MinIO locally)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/garinyaroslav/stepwise.git
cd stepwise
```
