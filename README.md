# Finance Data Processing and Access Control Backend

Spring Boot backend for finance records, dashboard summaries, and role-based access control (RBAC).

### Quick start (~60 seconds)
```bash
cd /path/to/Project
docker compose up -d
mvn spring-boot:run
```
Then open Swagger: `http://localhost:8080/swagger-ui.html` — or call `POST /api/auth/login` (see samples below), then use `Authorization: Bearer <accessToken>` on protected routes.

## 1) What’s included
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
- `config/`: security setup and startup seeding

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

### HTTP status codes (common)

| Code | When |
|------|------|
| `200` | Success (GET/PUT with body) |
| `201` | Created (`POST /api/users`, `POST /api/records`) |
| `204` | Success, no body (`DELETE /api/records/{id}`) |
| `400` | Validation failed (field errors in `details`) |
| `401` | Missing/invalid JWT, or bad login credentials |
| `403` | Valid JWT but wrong role, or inactive user on login |
| `404` | User or record id not found |
| `409` | Username already exists (`POST /api/users`) |
| `500` | Unexpected server error |

## 7) Pagination (GET /api/records)
Query parameters:
- `page` (0-based in request; default `0`)
- `size` (default `20`, max `100`)

Response contains:
- `content` (list of records)
- `totalElements`, `totalPages`
- `pageSize`
- `pageNumber` (1-based in the response)

## 8) API samples (request / response)

All protected routes need header: `Authorization: Bearer <accessToken>` unless noted.

---

### `POST /api/auth/login` (public — no JWT)

**Request**
```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "password"
}
```

**Response `200`**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "username": "admin",
  "role": "ADMIN"
}
```

**Response `401` (bad credentials)**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 401,
  "error": "Invalid username or password"
}
```

```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 403,
  "error": "User is inactive"
}
```

---

### `GET /api/users` (ADMIN)

**Request**
```http
GET /api/users
Authorization: Bearer <adminAccessToken>
```

**Response `200`**
```json
[
  {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "status": "ACTIVE",
    "createdAt": "2026-04-02T06:00:00.000Z"
  }
]
```

**Response `403` (wrong role)**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 403,
  "error": "You are not allowed to perform this action"
}
```

---

### `POST /api/users` (ADMIN)

**Request**
```http
POST /api/users
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "username": "viewer1",
  "password": "password12",
  "role": "VIEWER",
  "status": "ACTIVE"
}
```

**Response `201`**
```json
{
  "id": 5,
  "username": "viewer1",
  "role": "VIEWER",
  "status": "ACTIVE",
  "createdAt": "2026-04-02T06:23:13.259859Z"
}
```

**Response `409` (username exists)**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 409,
  "error": "Username already exists"
}
```

**Response `400` (validation)**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 400,
  "error": "Validation failed",
  "details": {
    "password": "password must be 8–72 characters"
  }
}
```

---

### `PUT /api/users/{id}` (ADMIN)

**Request**
```http
PUT /api/users/5
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "role": "ANALYST",
  "status": "ACTIVE",
  "newPassword": null
}
```

To reset password, set `"newPassword": "newpass12"` (min 8 chars).

**Response `200`**
```json
{
  "id": 5,
  "username": "viewer1",
  "role": "ANALYST",
  "status": "ACTIVE",
  "createdAt": "2026-04-02T06:23:13.259859Z"
}
```

**Response `404`**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 404,
  "error": "User not found"
}
```

---

### `POST /api/records` (ADMIN)

**Request**
```http
POST /api/records
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "amount": 99.50,
  "type": "EXPENSE",
  "category": "Food",
  "date": "2026-04-01",
  "notes": "Lunch"
}
```

`type` is `INCOME` or `EXPENSE`.

**Response `201`**
```json
{
  "id": 4,
  "amount": 99.50,
  "type": "EXPENSE",
  "category": "Food",
  "date": "2026-04-01",
  "notes": "Lunch",
  "createdAt": "2026-04-02T06:30:00.000Z"
}
```

---

### `GET /api/records` (ADMIN/ANALYST)

**Request**
```http
GET /api/records?page=0&size=20&type=INCOME&category=Salary&fromDate=2026-01-01&toDate=2026-12-31
Authorization: Bearer <analystOrAdminAccessToken>
```

Query params optional: `type`, `category`, `fromDate`, `toDate`, `page`, `size`.

**Response `200`**
```json
{
  "content": [
    {
      "id": 1,
      "amount": 5000.00,
      "type": "INCOME",
      "category": "Salary",
      "date": "2026-03-13",
      "notes": "Monthly salary",
      "createdAt": "2026-04-02T05:42:02.359849Z"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "pageSize": 20,
  "pageNumber": 1
}
```

Note: request `page` is **0-based**; `pageNumber` in the body is **1-based**.

---

### `PUT /api/records/{id}` (ADMIN)

**Request**
```http
PUT /api/records/1
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "amount": 5100.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-03-13",
  "notes": "Updated note"
}
```

**Response `200`**
```json
{
  "id": 1,
  "amount": 5100.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-03-13",
  "notes": "Updated note",
  "createdAt": "2026-04-02T05:42:02.359849Z"
}
```

**Response `404`**
```json
{
  "timestamp": "2026-04-02T12:00:00.000Z",
  "status": 404,
  "error": "Record not found"
}
```

---

### `DELETE /api/records/{id}` (ADMIN)

**Request**
```http
DELETE /api/records/1
Authorization: Bearer <adminAccessToken>
```

**Response `204`** — empty body.

---

### `GET /api/dashboard/summary` (ADMIN / ANALYST / VIEWER)

**Request**
```http
GET /api/dashboard/summary?monthsBack=6
Authorization: Bearer <anyAllowedRoleAccessToken>
```

**Response `200`**
```json
{
  "totalIncome": 5000.00,
  "totalExpense": 1200.00,
  "netBalance": 3800.00,
  "categoryTotals": [
    { "category": "Salary", "amount": 5000.00 },
    { "category": "Rent", "amount": 1200.00 }
  ],
  "recentActivity": [
    {
      "id": 1,
      "amount": 5000.00,
      "type": "INCOME",
      "category": "Salary",
      "date": "2026-03-13",
      "notes": "Monthly salary",
      "createdAt": "2026-04-02T05:42:02.359849Z"
    }
  ],
  "monthlyTrends": [
    {
      "month": "2026-04",
      "income": 5000.00,
      "expense": 1200.00,
      "net": 3800.00
    }
  ]
}
```

Values depend on data in `financial_records`.

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
