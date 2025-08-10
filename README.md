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

## Prerequisites

- **Java 17** or higher
- **PostgreSQL 15+**
- **MinIO** (local or remote instance)
- **Docker** (optional, for running PostgreSQL and MinIO locally)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/garinyaroslav/stepwise.git
cd stepwise
```

### 2. Configuration

To configure the application, set the following environment variables. These can be set in your operating system, a `.env` file (if using a library like `dotenv-java`), or passed as JVM arguments. Default values are provided for development but should be replaced with secure values in production.

| Variable            | Description                      | Default Value (Dev)                                               |
| ------------------- | -------------------------------- | ----------------------------------------------------------------- |
| `DB_URL`            | JDBC URL for PostgreSQL database | `jdbc:postgresql://localhost:5432/stepwise_db`                    |
| `DB_USERNAME`       | Database username                | `admin`                                                           |
| `DB_PASSWORD`       | Database password                | `admin`                                                           |
| `SMTP_USERNAME`     | SMTP username for email service  | `test@mail.ru`                                                    |
| `SMTP_PASSWORD`     | SMTP password for email service  | `test_password`                                                   |
| `JWT_SECRET`        | Secret key for JWT signing       | `very-very-secure-secret-key-that-is-at-least-32-characters-long` |
| `STORAGE_URL`       | MinIO server URL                 | `http://localhost:9000`                                           |
| `STORAGE_AK`        | MinIO access key                 | `minioadmin`                                                      |
| `STORAGE_SK`        | MinIO secret key                 | `minioadmin`                                                      |
| `REFRESH_TOKEN_URL` | Client refres token url          | `http://app.com/reset?token=`                                     |

REFRESH_TOKEN_URL
### 3. Run the Application

Build and run the application using Gradle:

```bash
./gradlew build
java -jar build/libs/stepwise-0.0.1.jar
```

Alternatively, if using Docker:

```bash
docker-compose up
```
