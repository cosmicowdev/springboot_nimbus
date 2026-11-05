# Nimbus IAM

> A production-grade **Identity and Access Management (IAM / Identity Provider)** service built with **Spring Boot** and **Kotlin**.

Nimbus IAM provides authentication, authorization, user lifecycle management, and role-based access control (RBAC) for downstream applications. It is designed around **Clean Architecture** so domain logic stays decoupled from frameworks and infrastructure.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Overview](#api-overview)
- [Error Handling](#error-handling)
- [Security](#security)
- [License](#license)

---

## Features

- **Email/Password authentication** with bcrypt password hashing.
- **JWT access & refresh tokens** (HMAC-SHA256) with configurable expiration.
- **User lifecycle**: create, read, update, deactivate, and delete users.
- **Role-Based Access Control (RBAC)**: roles and scoped permissions.
- **Account verification** via signed tokens (verification + password reset flows).
- **Global error handling** in the **RFC 7807 Problem Details** format.
- **Strict DTO validation** — JPA entities are never exposed over the wire.
- **Clean Architecture**: Controller → Service → Domain → Repository boundaries.

## Tech Stack

| Concern              | Technology                                   |
|----------------------|----------------------------------------------|
| Language             | Kotlin (JVM 21)                              |
| Framework            | Spring Boot 3.3                              |
| Persistence          | Spring Data JPA / Hibernate                  |
| Database             | PostgreSQL                                   |
| Security             | Spring Security + JWT (`jjwt`)               |
| Validation           | Jakarta Bean Validation                      |
| Build                | Gradle (Kotlin DSL)                          |
| Testing              | JUnit 5, MockK, AssertJ, Testcontainers      |

## Architecture

The service follows a layered Clean Architecture:

```
┌──────────────────────────────────────────────────┐
│  Interface Layer   (Controllers, DTOs, Mappers)  │  → HTTP boundary
├──────────────────────────────────────────────────┤
│  Application Layer  (Use-cases / Services)       │  → Business rules
├──────────────────────────────────────────────────┤
│  Domain Layer       (Entities, Ports, Exceptions)│  → Pure Kotlin
├──────────────────────────────────────────────────┤
│  Infrastructure    (JPA, Security, Adapters)     │  → Frameworks
└──────────────────────────────────────────────────┘
```

- **Domain** has zero framework dependencies.
- **Application** depends only on Domain and port interfaces.
- **Infrastructure** implements ports using Spring Data JPA, Spring Security, and JWT.
- **Interface** exposes REST endpoints and maps DTOs ↔ domain models.

## Project Structure

```
nimbus-iam/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/...
├── gradlew
├── gradlew.bat
├── .gitignore
├── README.md
└── src/
    └── main/
        └── kotlin/
            └── com/nimbus/iam/
                ├── NimbusIamApplication.kt
                ├── config/...
                ├── domain/...
                ├── application/...
                ├── infrastructure/...
                └── interfaces/...
```

## Getting Started

### Prerequisites

- JDK 21
- A PostgreSQL database (or use the Supabase instance provided by the Bolt environment)

### Run

```bash
./gradlew bootRun
```

The service starts on `http://localhost:8080`.

## Configuration

Externalized via `application.yml` / `application-{profile}.yml`. Secrets are injected through environment variables — never committed.

| Property                          | Description                          |
|-----------------------------------|--------------------------------------|
| `spring.datasource.url`           | JDBC URL                             |
| `spring.datasource.username`      | DB user                              |
| `spring.datasource.password`      | DB password                          |
| `nimbus.security.jwt.secret`      | HMAC signing key                     |
| `nimbus.security.jwt.access-ttl`  | Access token TTL (minutes)           |
| `nimbus.security.jwt.refresh-ttl` | Refresh token TTL (days)             |
| `nimbus.security.api-keys.root`   | Bootstrap root API key (dev only)    |

## API Overview

| Method | Path                      | Description                       |
|--------|---------------------------|-----------------------------------|
| POST   | `/api/v1/auth/register`   | Register a new user               |
| POST   | `/api/v1/auth/login`      | Authenticate and receive tokens   |
| POST   | `/api/v1/auth/refresh`    | Exchange a refresh token          |
| POST   | `/api/v1/auth/logout`     | Revoke a refresh token            |
| GET    | `/api/v1/me`              | Current authenticated user        |
| PATCH  | `/api/v1/me`              | Update own profile                |
| POST   | `/api/v1/me/change-password` | Change own password             |
| GET    | `/api/v1/admin/users`     | List users (admin)                |
| GET    | `/api/v1/admin/users/{id}`| Get a user (admin)                |
| POST   | `/api/v1/admin/users`     | Create a user (admin)             |
| PATCH  | `/api/v1/admin/users/{id}`| Update a user (admin)             |
| DELETE | `/api/v1/admin/users/{id}`| Delete a user (admin)             |
| GET    | `/api/v1/admin/roles`     | List roles (admin)                |
| POST   | `/api/v1/admin/roles`     | Create a role (admin)             |

## Error Handling

All errors are returned as **RFC 7807 Problem Details** (`application/problem+json`):

```json
{
  "type": "https://nimbus.iam/errors/user-not-found",
  "title": "User Not Found",
  "status": 404,
  "detail": "No user exists with identifier 'abc-123'.",
  "instance": "/api/v1/admin/users/abc-123",
  "timestamp": "2026-10-28T12:00:00Z"
}
```

Validation errors include an `errors` extension listing each invalid field.

## Security

- Passwords are hashed with BCrypt (configurable strength).
- JWTs are signed with HMAC-SHA256; refresh tokens are opaque, stored, and revocable.
- Endpoints under `/api/v1/admin/**` require the `ROLE_ADMIN` authority.
- The `/api/v1/me/**` endpoints require any authenticated user.
- Database entities are never serialized; DTOs are the sole wire contract.

## License

Released under the MIT License.
