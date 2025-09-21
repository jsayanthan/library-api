# Library API

This is my implementation of the library assessment. I focused on getting the core rules correct, keeping the code readable, and proving the important paths with tests instead of over–engineering. It uses Spring Boot 3 / Java 17, H2 for quick local work, PostgreSQL for real persistence, MapStruct for mapping, auditing for traceability, and a couple of small AOP aspects (logging + page size guard). Swagger/OpenAPI is on by default so you can just run and try it.

### Quick Start (Local H2)
Run directly (H2 in‑memory is the default profile `local`):
```
./mvnw spring-boot:run
```
Or explicitly specify the profile (functionally the same, just makes it obvious during review):
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Then open:
* API docs (Swagger UI): http://localhost:8080/swagger-ui.html  (alt path: /swagger-ui/index.html)
* OpenAPI JSON: http://localhost:8080/api-docs/library
* H2 Console: http://localhost:8080/h2-console  (JDBC URL: `jdbc:h2:mem:library;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`, user: `sa`, pass: `sa`)

To run with PostgreSQL instead:
```
docker run --rm -d -p 5432:5432 --name library-pg -e POSTGRES_DB=library -e POSTGRES_USER=library -e POSTGRES_PASSWORD=library postgres:16
PowerShell> $env:DB_HOST="localhost"; $env:DB_USER="library"; $env:DB_PASSWORD="library"; $env:DB_NAME="library"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Stop Postgres: `docker stop library-pg`

### Auditing Header (X-User)
All write operations accept an optional `X-User` header used for auditing (stored in `created_by` / `updated_by`). If you omit it the value defaults to `system`. Example:
```
curl -X POST http://localhost:8080/api/v1/borrowers \
  -H "Content-Type: application/json" \
  -H "X-User: Sayanthan" \
  -d '{"name":"Sayanthan","email":"sayanthan@example.com"}'
```

---
## 1. Assessment Requirements Mapping
| Requirement | Implementation Summary |
|-------------|------------------------|
| Register borrower | `POST /api/v1/borrowers` with validation & unique email check |
| Register new book | `POST /api/v1/books` (adds a physical copy + catalog resolution by normalized ISBN) |
| List all books | `GET /api/v1/books` with search & pagination + page size guard aspect |
| Borrow a book | `POST /api/v1/loans` atomic borrow preventing double-loan |
| Return a book | `POST /api/v1/loans/return` validates active loan |
| Multiple copies same ISBN | Modeled via `BookCatalog (ISBN)` + many `Book` copies |
| Data validation & errors | Bean Validation + custom uniqueness validators + structured `ApiError` |
| Configurable environments | Profiles: `local` (H2), `dev` (PostgreSQL), `prod`, `test` |
| Package manager | Maven with reproducible plugin configuration |
| Database choice & rationale | PostgreSQL (robust constraints, future indexing) + H2 for fast local dev/tests |
| Documentation | This README + generated OpenAPI at `/swagger-ui.html` |
| Assumptions documented | See Section 10 |
| Nice-to-have: Testing & coverage | Controller + validation + service tests; JaCoCo HTML report |
| Nice-to-have: Clean code & architecture | Layered, mappers, DTO segregation, aspects, auditing |
| Nice-to-have: 12-Factor alignment | Externalized config, stateless services, dev/prod parity considerations |

---
## 2. High-Level Architecture
```
Client → Controller (DTOs) → Service (Business rules) → Repository (JPA) → Database
                              │
                              ├─ MapStruct (entity ↔ dto)
                              ├─ Validation (Bean + custom uniqueness)
                              ├─ AOP (logging, page-size guard)
                              └─ Auditing (created/updated metadata)
```

### Key Packages
- `domain.entity` – JPA entities (`BookCatalog`, `Book`, `Borrower`, `Loan`) with auditing & optimistic locking.
- `domain.repository` – Spring Data repositories (leverage derived queries + Specs for search).
- `service` – Borrow, return, registration orchestration & invariants.
- `web.controller` – REST endpoints, thin; returns DTOs only.
- `validation` – Custom constraint annotations (unique email / ISBN semantics & metadata match enforcement).
- `mapper` – MapStruct interfaces for DTO ↔ entity translation.
- `aop` – Cross-cutting aspects: request logging & page size limiting.
- `config` – OpenAPI grouping & other infra beans.
- `core` – Auditing support and utility classes.

---
## 3. Domain Modeling Highlights
| Concept | Purpose | Notable Choices |
|---------|---------|-----------------|
| BookCatalog | Single row per ISBN | Natural key (ISBN) avoids join table & enforces global uniqueness |
| Book | Physical copy | FK to `BookCatalog`; allows many copies per ISBN |
| Borrower | Library member | Unique email with validation & index potential |
| Loan | Borrow event | Active loan defined by `returnedAt IS NULL`; optimistic locking guards race conditions |

Normalization of ISBN (strip hyphens/spaces) ensures consistent uniqueness checks.

---
## 4. Concurrency & Data Integrity
Borrow flow performs guarded update to ensure only one active borrow per copy, combined with optimistic locking (`@Version`) to detect rare concurrent modification anomalies. Validation prevents duplicate active loans; service layer resolves catalog vs copy responsibilities cleanly.

---
## 5. Validation Strategy
- Bean Validation annotations on DTOs (size, format, required fields).
- Custom constraints:
  - `@UniqueEmail` – Pre-query to short-circuit duplicate borrower creation.
  - `@UniqueIsbn` / metadata check – Ensures same ISBN always maps to identical title/author (consistency rule from requirements).
- Centralized error response format: `{ code, message, path }` enabling clients to react to semantic errors.

---
## 6. Cross-Cutting Concerns
| Concern | Mechanism | Notes |
|---------|-----------|-------|
| Logging | `LoggingAspect` (AOP) | Method entry/exit timing for controllers/services (structured for extensibility). |
| Page Size Guard | `PageSizeLimitAspect` | Enforces an upper bound to prevent accidental large queries. |
| Auditing | `@EnableJpaAuditing` + AuditorAware | Populates created/updated timestamps & user (system placeholder). |

---
## 7. OpenAPI / Swagger
`OpenApiConfig` registers a grouped spec. Swagger UI available at:
```
http://localhost:8080/swagger-ui.html
```
or (Springdoc 2.x path variant):
```
http://localhost:8080/swagger-ui/index.html
```
OpenAPI document (JSON): `/v3/api-docs/library` (group name may appear as configured).

---
## 8. Technology Stack Rationale
| Tech | Reason |
|------|--------|
| Spring Boot 3 (Java 17) | Modern baseline, records / pattern matching potential, native-ready. |
| Spring Data JPA | Rapid persistence + derived query support. |
| H2 (local / tests) | Fast startup, no external dependency for quick iteration. |
| PostgreSQL (dev/prod) | Strong transactional guarantees, indexing & JSON support future-proofing. |
| Flyway | Deterministic schema evolution & repeatable migrations. |
| MapStruct | Compile-time mapping → eliminates reflection & manual mapping noise. |
| Lombok | Reduce boilerplate in entities/DTOs; readability. |
| Springdoc OpenAPI | Automatic, accurate API documentation. |
| AOP (Spring) | Non-invasive cross-cutting implementations (logging, guardrails). |
| JaCoCo + Surefire | Coverage visibility and HTML report for reviewer confidence. |

---
## 9. Running the Application
### Prerequisites
- JDK 17+
- Maven Wrapper (included)

### Profiles
| Profile | DB | Usage |
|---------|----|-------|
| `local` | In-memory H2 | Fast exploration & demos (default) |
| `dev` | PostgreSQL | Persistent development data |
| `test` | H2 (isolated) | Automated test execution |
| `prod` | PostgreSQL | Production settings (stricter constraints/logging tweaks) |

### Quick Start (H2)
```
./mvnw spring-boot:run
```
Swagger UI: http://localhost:8080/swagger-ui.html

### Run with PostgreSQL (manual example)
PowerShell example:
```
docker run --rm -d -p 5432:5432 --name library-pg -e POSTGRES_DB=library -e POSTGRES_USER=library -e POSTGRES_PASSWORD=library postgres:16
$env:DB_HOST="localhost"; $env:DB_USER="library"; $env:DB_PASSWORD="library"; $env:DB_NAME="library"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Stop & clean:
```
docker stop library-pg
```

---
## 10. Assumptions & Business Rules
- Borrower can hold multiple different book copies simultaneously.
- Only one active loan per specific physical copy; multiple copies per ISBN allowed.
- ISBN metadata (title/author) must be consistent once established.
- Time stored in UTC; clock drift out-of-scope.
- Soft deletes not required; historical loans retained.
- Validation rejects inconsistent ISBN metadata and duplicate borrower email.

---
## 11. Error Handling & Status Codes (Representative)
| Scenario | Status | Code |
|----------|-------|------|
| Duplicate email | 400 | EMAIL_ALREADY_EXISTS |
| Duplicate ISBN (new copy metadata mismatch) | 400 | ISBN_METADATA_MISMATCH |
| Book already borrowed | 409 | BOOK_ALREADY_BORROWED |
| Borrower not found | 404 | BORROWER_NOT_FOUND |
| Book not found | 404 | BOOK_NOT_FOUND |
| Active loan missing on return | 404 | ACTIVE_LOAN_NOT_FOUND |
| Page size above limit | 400 | PAGE_SIZE_LIMIT |

---
## 12. Testing & Coverage
- Service and controller layer tests validating success & failure paths.
- Validation tests for uniqueness and field constraints.
- Coverage via JaCoCo (HTML: `target/site/jacoco/index.html`). Non-business boilerplate (DTOs/entities/mappers) excluded to focus percentages on logic.
- Strategy: Lean tests proving invariants, not overfitting implementation.

---
## 13. Build & Reports
Generate package + coverage + reports:
```
./mvnw clean verify
```
Artifacts:
- JAR: `target/library-api-<version>.jar`
- JaCoCo HTML: `target/site/jacoco/index.html`
- Surefire report: `target/site/surefire-report.html`

---
### (Optional Improvements Not Implemented Yet)
Time boxed for the assessment: left out integration tests with real Postgres, security, and metrics/tracing. Those would be the first follow‑ups.
