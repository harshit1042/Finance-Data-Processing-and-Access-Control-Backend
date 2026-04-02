# Finance Data Processing and Access Control Backend

Spring Boot backend for finance records, dashboard summaries, and role-based access control (RBAC).

## 1) What’s included (mapped to assignment)
- **User & role management:** `POST/GET/PUT /api/users` (ADMIN only), roles: `VIEWER`, `ANALYST`, `ADMIN`, status: `ACTIVE/INACTIVE`
- **Financial records:** CRUD + filtering + pagination
  - Create: `POST /api/records` (ADMIN)
  - List: `GET /api/records` (ADMIN/ANALYST)
  - Update: `PUT /api/records/{id}` (ADMIN)
  - Delete: `DELETE /api/records/{id}` (ADMIN)
- **Dashboard summary analytics:** `GET /api/dashboard/summary` (ADMIN/ANALYST/VIEWER)
  - Total income/expense/net
  - Category-wise totals
  - Recent activity
  - Monthly trends
- **Access control:** enforced via `@RoleAccess` + AOP aspect (`RoleAccessAspect`)
- **Validation & error handling:** Jakarta Bean Validation + `GlobalExceptionHandler`
- **Persistence:** MySQL (Docker) for runtime; H2 (test profile) for tests

## 2) Tech Stack
- Java 17
- Spring Boot 3
- Spring Web, Data JPA, Validation, AOP, Security
- **JWT** stateless authentication
- **MySQL 8** in Docker (Compose; persistent `mysql_data` volume)
- H2 (tests only)
- Swagger/OpenAPI (Bearer JWT)

## 3) Architecture (high level)
Project packages are organized by responsibility:
- `controller/`: REST endpoints + request/response DTO mapping
- `service/`: business rules (filters, validations, dashboard aggregation, auth login)
- `repository/`: Spring Data JPA repositories
- `model/`: JPA entities (`User`, `FinancialRecord`)
- `dto/`: API request/response models
- `security/`: JWT filter + token creation + `RequestContext` (current user/role)
- `exception/`: consistent API error responses
- `config/`: security config + seed/demo sync

## 4) Database Schema (what tables exist)
### `users`
- `id` (PK, bigint, auto)
- `username` (unique, varchar)
- `password_hash` (varchar; BCrypt hash)
- `role` (enum: VIEWER/ANALYST/ADMIN)
- `status` (enum: ACTIVE/INACTIVE)
- `created_at` (timestamp / Instant)

### `financial_records`
- `id` (PK, bigint, auto)
- `amount` (decimal)
- `type` (enum: INCOME/EXPENSE)
- `category` (varchar)
- `date` (date)
- `notes` (varchar, nullable)
- `created_at` (timestamp / Instant)

There are no direct JPA relationships between `users` and `financial_records`; access is enforced at the API layer by role checks.

## 5) Run the Project (Step-by-step)
### Prerequisites
- Docker installed and running
- Java/Maven installed

### Step 1: Start MySQL (Docker)
Compose uses host port **3307** to avoid clashing with any local MySQL on 3306.

```bash
cd /Users/harshit/Project
docker compose up -d
```

DB in `application.yml`:
- host: `localhost:3307`
- user: `root`
- password: `root`
- database: `finance_db`

### Step 2: Start the Spring Boot API

```bash
mvn spring-boot:run
```

API base URL:
- `http://localhost:8080`

### Step 3: Open API docs (optional)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 6) Authentication (JWT) and Demo Credentials
### Login (public)
`POST /api/auth/login`

Body:
```json
{ "username": "admin", "password": "password" }
```

Response returns `accessToken`.

### Seeded demo accounts
On every application start, these usernames are synced to password `password` (BCrypt):
- `admin` / `password` / ADMIN
- `analyst` / `password` / ANALYST
- `viewer` / `password` / VIEWER

### Using JWT in requests
For protected endpoints send:
- `Authorization: Bearer <accessToken>`

## RBAC (Access Control)
Role-based rules are enforced at the API layer via `@RoleAccess` (AOP aspect).

| Endpoint | ADMIN | ANALYST | VIEWER |
|---------|:-----:|:-------:|:------:|
| `POST /api/users` | Yes | No | No |
| `GET /api/users` | Yes | No | No |
| `PUT /api/users/{id}` | Yes | No | No |
| `POST /api/records` | Yes | No | No |
| `GET /api/records` | Yes | Yes | No |
| `PUT /api/records/{id}` | Yes | No | No |
| `DELETE /api/records/{id}` | Yes | No | No |
| `GET /api/dashboard/summary` | Yes | Yes | Yes |

Notes:
- If a user is `INACTIVE`, login is blocked (403).
- If a user hits an endpoint they don’t have access to, the API returns **403**.

## 7) Pagination (GET /api/records)
Query parameters:
- `page` (0-based in request; default `0`)
- `size` (default `20`, max `100`)

Response contains:
- `content` (list of records)
- `totalElements`, `totalPages`
- `pageSize`
- `pageNumber` (1-based in the response for user friendliness)

## 8) API Call Examples (quick list)
### Auth
1. Login:
   - `POST /api/auth/login`

### Users (ADMIN only)
2. List users:
   - `GET /api/users`
3. Create user (viewer/analyst/admin):
   - `POST /api/users`
4. Update user:
   - `PUT /api/users/{id}`

### Records
5. Create record:
   - `POST /api/records`
6. List records (paginated + filters):
   - `GET /api/records?page=0&size=20&type=INCOME&category=Salary&fromDate=2026-01-01&toDate=2026-12-31`
7. Update record:
   - `PUT /api/records/{id}`
8. Delete record:
   - `DELETE /api/records/{id}`

### Dashboard Summary (VIEWER allowed)
9. Dashboard summary:
   - `GET /api/dashboard/summary?monthsBack=6`

## 9) Postman
### Import OpenAPI (needs the app running)
Postman → Import → Link:
- `http://localhost:8080/v3/api-docs`

### Import collection from this repo
File:
- `postman/Finance-Data-API.postman_collection.json`

Postman → Import → File → select it.

Then:
- Run `Auth -> Login (saves token)` once; other requests use the saved `{{accessToken}}`.

## 10) Assumptions / Notes
- JWT refresh tokens are not implemented.
- MySQL stores data in the Docker named volume; `ddl-auto: update` updates schema without clearing the DB.
- Tests use H2 in-memory via `@ActiveProfiles("test")`.

## Tradeoffs / Design Decisions
- **Demo accounts sync on startup:** `admin`, `analyst`, and `viewer` are synced to password `password` on every app start to keep local evaluation stable.
  - If you change those users manually, they may be overwritten on restart (users created via `/api/users` are not affected).
- **Hard delete for records:** `DELETE /api/records/{id}` permanently removes the row (no soft-delete).
- **JWT loads user from DB per request:** ensures role/status changes take effect immediately, at the cost of one DB lookup per request.
- **Pagination response convention:** request param `page` is 0-based (Spring), but `pageNumber` in the response is 1-based for easier UI display.
