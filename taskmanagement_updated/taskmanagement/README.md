# Multi-Tenant Task Management System

Spring Boot REST API for multiple organizations (tenants) sharing one application and one H2 database. Tenant ownership is derived from a signed JWT and enforced in service and repository operations.

## Technology
- Java 17+
- Spring Boot 4
- Spring Security (stateless JWT authentication)
- Spring Data JPA / Hibernate
- H2 in-memory database
- JJWT
- Maven

## Package structure
All application packages are under `com.techpulse.taskmanagement`: `controller`, `dto`, `service`, `entity`, `enums`, `repository`, `config`, `filter`, `security`, `tenant`, and `exception`.

## Tenant isolation design
1. `POST /tenant/register` creates a tenant and its first ADMIN user.
2. `POST /auth/login` authenticates the email/password and issues a signed JWT.
3. JWT claims include `sub` (email), `tenant_id`, `role`, `iat`, and `exp`.
4. `JwtAuthenticationFilter` extends `OncePerRequestFilter`, validates the signature and expiry, reads the signed tenant and role claims, and resolves the user using both email and tenant ID.
5. The filter sets `TenantContext` for the request. Its `finally` block always clears the ThreadLocal to prevent request-thread reuse from leaking tenant data.
6. Services obtain tenant ID only from `TenantContext`; create-task and create-user request bodies do not accept tenant IDs.
7. Repository operations that access tenant-owned users/tasks use tenant-qualified methods. Task update and assignment operations first locate the task within the current tenant. Assignment also locates the target user within that tenant.
8. `GET /tasks`: ADMIN and MANAGER receive tasks for their tenant; USER receives only tasks assigned to their own user ID.

**Email rule:** The supplied API accepts only email/password at login and does not accept a tenant identifier. Therefore this implementation treats email as globally unique across all tenants. The global unique constraint and duplicate-email check make login unambiguous. If the product must allow the same email in multiple tenants, the login contract must be extended to identify the tenant (for example, tenant slug) and uniqueness must become `(tenant_id, email)`.

## JWT example payload
The following is illustrative; IDs and timestamps vary:

```json
{
  "sub": "manager@acme.test",
  "tenant_id": 101,
  "role": "MANAGER",
  "iat": 1790000000,
  "exp": 1790003600
}
```

Do not put a tenant ID in API request parameters or request bodies. The server derives it from the validated JWT.

## Endpoint matrix
| Method | Endpoint | Role | Purpose |
|---|---|---|---|
| POST | `/tenant/register` | Public | Create tenant and first ADMIN |
| POST | `/auth/login` | Public | Authenticate and return JWT |
| POST | `/users` | ADMIN | Create MANAGER or USER in current tenant |
| POST | `/tasks` | MANAGER | Create task in current tenant |
| PUT | `/tasks/{id}/assign` | MANAGER | Assign to a user in same tenant |
| PUT | `/tasks/{id}/status` | USER | Update status of own assigned task |
| GET | `/tasks` | ADMIN/MANAGER/USER | Return role-appropriate tenant tasks |

## Run locally
1. Install JDK 17 or newer and Maven (or use the included Maven wrapper).
2. Set a strong Base64-encoded JWT secret (at least 256 bits for HS256):
   - PowerShell: `$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))`
   - Bash: `export JWT_SECRET="$(openssl rand -base64 32)"`
3. From the project root run:
   - Windows: `mvnw.cmd spring-boot:run`
   - macOS/Linux: `./mvnw spring-boot:run`
4. API base URL: `http://localhost:8080`.

The H2 database is in-memory and resets when the application stops. H2 console is disabled by default. For local development only, enable the `dev` profile (`SPRING_PROFILES_ACTIVE=dev`) to expose `/h2-console`; do not enable this profile in a production deployment.

## Security filter flow
`HTTP request -> SecurityFilterChain -> JwtAuthenticationFilter -> validate signature/expiry -> read email, tenant_id, role -> load user by email + tenant_id -> set Authentication + TenantContext -> controller -> service -> tenant-qualified repository -> response -> clear TenantContext`.

## Tests and coverage
Run all tests:
- Windows: `mvnw.cmd clean test`
- macOS/Linux: `./mvnw clean test`

JaCoCo creates an HTML report at `target/site/jacoco/index.html`. The added tests cover service success paths and important failure/isolation cases. Run the report and add further controller/filter/repository integration tests until the assignment's required **80% overall coverage** is actually achieved. Do not claim 80% until the generated report confirms it.

## Submission checklist
- [ ] Run `clean test` successfully.
- [ ] Confirm JaCoCo overall line/instruction coverage is at least 80%.
- [ ] Record the required 5-minute project explanation video.
- [ ] Push the project and README to GitHub.
