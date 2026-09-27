# Resource Booking System

A REST API where users can view resources (rooms/vehicles/equipment) and book them,
and admins can manage everything. Built for the Backend Developer assignment.

Stack: Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL (or Postgres),
Swagger/OpenAPI for docs.

## What's implemented

- `POST /auth/login` - returns a JWT
- Role based access - ADMIN and USER
  - ADMIN: full CRUD on resources and reservations
  - USER: read-only on resources, can create reservations and see only their own
- User identity for a reservation is always pulled from the JWT (`@AuthenticationPrincipal`),
  never from the request body, so you can't book something in someone else's name
- Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`
- Reservation price stored as `BigDecimal`
- Filtering reservations by `status`, `minPrice`, `maxPrice`
- Pagination (`page`, `size`) + optional sorting (`sortBy`, `sortDir`)
- Validation on request bodies + a global exception handler so errors come back
  as clean JSON instead of stack traces
- Seed data on startup (see below)
- Swagger UI for testing

## Project structure

```
src/main/java/com/resourcebooking
 ├── config/        -> SecurityConfig, OpenApiConfig, DataSeeder
 ├── controller/     -> AuthController, ResourceController, ReservationController
 ├── dto/            -> request/response classes
 ├── entity/         -> User, Resource, Reservation
 ├── enums/          -> Role, ReservationStatus
 ├── exception/      -> custom exceptions + GlobalExceptionHandler
 ├── repository/     -> JPA repos + the Specification for filtering
 └── security/       -> JwtUtil, JwtAuthFilter, CustomUserDetailsService
```

## How to run it

### 1. Database

Create an empty database first (tables get created automatically by Hibernate,
`ddl-auto: update`).

MySQL:
```sql
CREATE DATABASE resource_booking_db;
```

Postgres works too, you just need to point the env vars at it (see `.env.example`).

### 2. Environment variables

Copy `.env.example` and fill in your own values (or just set these directly in your
IDE run config / terminal):

| Variable | What it's for | Default |
|---|---|---|
| `DB_URL` | JDBC url | `jdbc:mysql://localhost:3306/resource_booking_db` |
| `DB_USERNAME` | db user | `root` |
| `DB_PASSWORD` | db password | `root` |
| `DB_DRIVER` | jdbc driver class | `com.mysql.cj.jdbc.Driver` |
| `DB_DIALECT` | hibernate dialect | `org.hibernate.dialect.MySQLDialect` |
| `JWT_SECRET` | secret key used to sign tokens | (has a placeholder default, change it) |
| `JWT_EXPIRATION_MS` | how long a token stays valid | `86400000` (24 hrs) |

To switch to Postgres, add the `postgresql` dependency is already in `pom.xml` (both drivers
are included), just change `DB_URL`, `DB_DRIVER` and `DB_DIALECT` to the postgres ones shown
in `.env.example`.

### 3. Run

```bash
mvn spring-boot:run
```

App comes up on `http://localhost:8080`.

On first run it seeds these users automatically (console prints them too):

| username | password | role |
|---|---|---|
| admin | admin123 | ADMIN |
| john  | user123  | USER |
| mary  | user123  | USER |

It also seeds 4 sample resources (2 rooms, 1 vehicle, 1 projector).

### 4. API docs

Swagger UI: `http://localhost:8080/swagger-ui.html`

Click "Authorize" and paste the token you get back from `/auth/login` (just the raw
token, the UI adds the `Bearer ` prefix itself).

A Postman collection is also included: `postman_collection.json` - import it into
Postman, it already has a `{{token}}` variable set up so once you hit the login
request, the collection's test script grabs the token automatically for the rest
of the requests.

## Example flow

1. `POST /auth/login` with `{"username": "john", "password": "user123"}` -> get token
2. `GET /api/resources` with header `Authorization: Bearer <token>` -> see all resources
3. `POST /api/reservations` with a body like:
   ```json
   {
     "resourceId": 1,
     "startTime": "2026-10-01T10:00:00",
     "endTime": "2026-10-01T12:00:00",
     "price": 500.00
   }
   ```
4. `GET /api/reservations` -> john only sees his own reservations. Log in as `admin`
   instead and you'll see everyone's.
5. Filtering/pagination example (admin):
   `GET /api/reservations?status=PENDING&minPrice=100&maxPrice=1000&page=0&size=5&sortBy=price&sortDir=asc`

## Notes

- Booking a resource for a time slot that overlaps an existing (non-cancelled) reservation
  on the same resource is rejected with a 409.
- The seed data (`admin`/`john`/`mary` + sample resources) only runs when the `prod` profile
  is **not** active. To disable it, run with `SPRING_PROFILES_ACTIVE=prod` (you'd obviously
  want your own real users at that point, not test accounts with known passwords).

## Things I'd improve with more time

- No refresh token, just a single long-lived access token for simplicity
- Could add rate limiting on `/auth/login`
- No automated tests yet (would add MockMvc/@SpringBootTest tests for auth, RBAC, ownership,
  filtering/pagination, and the overlap check)
