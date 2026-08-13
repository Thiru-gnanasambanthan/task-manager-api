# Task Manager REST API

[![CI](https://github.com/Thiru-gnanasambanthan/task-manager-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Thiru-gnanasambanthan/task-manager-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

A fully functional task management backend (like a simplified Trello/Jira),
built from scratch to learn Java, Spring Boot, JPA, Spring Security, and
REST API design.

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Data model](#data-model)
- [API reference](#api-reference)
- [Running locally](#running-locally)
- [Running tests](#running-tests)
- [Key design decisions](#key-design-decisions)
- [Possible next steps](#possible-next-steps)
- [Contributing](#contributing)
- [License](#license)

## Features

- **JWT authentication** — register, login, stateless token-based auth
- **Projects** — full CRUD, scoped to the logged-in owner
- **Tasks** — full CRUD, nested under projects, filterable by status, validated (future-dated due dates only)
- **Comments** — nested under tasks, author-scoped deletion
- **Pagination** — every list endpoint supports `?page=&size=&sort=`
- **Soft delete** — nothing is physically removed; `deletedAt` marks rows as deleted instead
- **Audit fields** — `createdAt` / `updatedAt` auto-populated via Spring Data JPA auditing
- **Global exception handling** — consistent JSON error shape across the whole API
- **Ownership enforcement** — users can only see/modify their own data; unauthorized access returns 404, not 403, to avoid leaking the existence of other users' resources
- **Input validation** — Bean Validation (`@NotBlank`, `@Email`, `@FutureOrPresent`, etc.) on every request DTO
- **API documentation** — Swagger/OpenAPI UI available out of the box

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Data access | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt) |
| Database | H2 (dev, in-memory) / PostgreSQL (prod-ready) |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Build tool | Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test (MockMvc) |

## Architecture

Layered architecture — every request flows through the same chain, no layer
skips another:

```
Client
  │
  ▼
JWT Security Filter   (validates token on every request)
  │
  ▼
Controller             (HTTP request/response only)
  │
  ▼
Service                 (business logic, ownership checks, validation)
  │
  ▼
Repository              (Spring Data JPA)
  │
  ▼
Database
```

A global exception handler sits alongside every layer, catching errors and
converting them into a consistent JSON error shape instead of raw stack
traces or bare status codes.

## Project structure

```
src/main/java/com/taskmanager/
├── TaskManagerApplication.java   → entry point, JPA auditing enabled
├── model/        → JPA entities (BaseEntity, User, Project, Task, Comment) + enums
├── repository/   → Spring Data JPA interfaces
├── dto/          → request/response objects (entities never cross the API boundary)
├── security/     → JwtUtil, JwtAuthFilter, CustomUserDetailsService, SecurityConfig
├── service/      → business logic, ownership enforcement
├── controller/   → REST endpoints
└── exception/    → custom exceptions + GlobalExceptionHandler

src/test/java/com/taskmanager/
├── controller/   → integration tests (MockMvc, real Spring context)
└── service/      → unit tests (Mockito, isolated business logic)
```

## Data model

- A **User** owns Projects, Tasks, and Comments
- A **Project** contains many Tasks
- A **Task** belongs to one Project and has many Comments
- A **Comment** belongs to one Task and one author (User)

Every entity shares a common base (`BaseEntity`): a UUID primary key,
`createdAt` / `updatedAt` audit timestamps, and a nullable `deletedAt` for
soft deletes.

## API reference

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/auth/register` | Create a new user | No |
| POST | `/api/auth/login` | Log in, receive a JWT | No |
| GET | `/api/projects` | List my projects (paginated) | Yes |
| POST | `/api/projects` | Create a project | Yes |
| GET | `/api/projects/{id}` | Get one project | Yes |
| PUT | `/api/projects/{id}` | Update a project | Yes |
| DELETE | `/api/projects/{id}` | Soft-delete a project | Yes |
| GET | `/api/tasks` | List my tasks (paginated) | Yes |
| POST | `/api/tasks` | Create a task | Yes |
| GET | `/api/tasks/{id}` | Get one task | Yes |
| PUT | `/api/tasks/{id}` | Update a task | Yes |
| DELETE | `/api/tasks/{id}` | Soft-delete a task | Yes |
| GET | `/api/tasks/project/{projectId}?status=` | Tasks in a project, optional status filter | Yes |
| GET | `/api/tasks/{taskId}/comments` | List comments on a task | Yes |
| POST | `/api/tasks/{taskId}/comments` | Add a comment | Yes |
| DELETE | `/api/tasks/{taskId}/comments/{commentId}` | Delete my own comment | Yes |

All authenticated endpoints expect:
```
Authorization: Bearer <jwt-token>
```

## Running locally

Requires Java 17+ and Maven (or your IDE's built-in Maven support — no
separate Maven install needed in VS Code with the Java extension pack).

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`, backed by an in-memory H2
database — no external database setup required to get going.

- H2 console: `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:taskmanager`, user: `sa`, no password)
- Swagger UI: `http://localhost:8080/swagger-ui.html`

**Note:** the in-memory H2 database resets on every application restart —
this is expected during development. Switch to PostgreSQL (driver already
included in `pom.xml`) for data that persists across restarts.

Sample requests for every endpoint are provided in `requests.http`, usable
directly with the VS Code "REST Client" extension.

## Running tests

```bash
mvn test
```

Includes:
- **Integration tests** (`AuthControllerIntegrationTest`) — boot the real
  Spring context and hit actual endpoints via MockMvc
- **Unit tests** (`ProjectServiceTest`) — isolated business logic with
  mocked dependencies (Mockito), no Spring context or database involved

## Key design decisions

- **UUID primary keys** instead of auto-increment integers — safer to
  expose in URLs, doesn't leak row counts
- **DTOs everywhere** — entities never returned directly from a controller,
  so internal fields (like password hashes) can never leak
- **Soft delete** instead of physical deletes — preserves history, allows
  undoing a delete later
- **LAZY fetch** on every `@ManyToOne` relationship — avoids loading
  related rows unless actually needed
- **Enums for status/priority** — invalid values are rejected automatically
  at the database and validation level
- **404 instead of 403 for unauthorized access** — deliberately avoids
  confirming to an attacker that a resource with a given ID exists but
  belongs to someone else
- **Indexes** on `owner_id`, `project_id`, `status` (Task) and `task_id`
  (Comment) — the columns queried/filtered most often

## Possible next steps

- Role-based sharing (invite teammates to a project instead of strict
  single-owner access)
- Refresh tokens (current JWTs expire after 24h with no renewal path)
- Flyway/Liquibase migrations instead of `ddl-auto=update`
- Rate limiting on `/api/auth/**`

## Contributing

Contributions and suggestions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md)
for setup steps and code style notes.

## License

This project is licensed under the [MIT License](LICENSE).
