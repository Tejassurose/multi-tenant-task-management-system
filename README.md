# Multi-Tenant Task Management System

A secure, backend-only, multi-tenant Task Management System developed using **Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, and H2 Database**.

The application follows a SaaS-style architecture in which multiple organizations (tenants) share a single backend application and a single database while maintaining strict data isolation.

Each tenant can manage its own users and tasks. Users can access only the data belonging to their organization, according to their assigned roles and permissions.

---

## 1. Project Objectives

The main objective of this project is to develop a secure multi-tenant task management backend with:

- Multiple organizations using one application and one database.
- JWT-based authentication and authorization.
- Tenant identification through JWT claims.
- Role-based access control.
- Strict tenant-level data isolation.
- Repository-level tenant filtering.
- Secure task creation, assignment, and status updates.

**Important security principle:** A tenant ID is never accepted from the request body or request parameters for determining the current tenant. The tenant ID is extracted from the authenticated JWT.

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.0.0 | Backend application framework |
| Spring Security | Authentication and authorization |
| JWT (JJWT 0.13.0) | Token generation and validation |
| Spring Data JPA | Database operations |
| Hibernate | ORM implementation |
| H2 Database | Single shared database |
| Maven | Dependency and build management |
| Swagger / OpenAPI | API documentation and testing |
| BCrypt | Password hashing |

---

## 3. Application Architecture

The application follows a layered architecture.

```text
Client (Swagger / API Client)
           |
           v
       Controller
           |
           v
        Service
           |
           v
       Repository
           |
           v
       H2 Database
```

Security is implemented using Spring Security and a custom JWT authentication filter.

### Main Layers

- **Controller Layer:** Exposes REST APIs and handles incoming HTTP requests.
- **Service Layer:** Contains business logic, role-related operations, and tenant validation.
- **Repository Layer:** Performs database operations with tenant-specific query filtering.
- **Entity Layer:** Defines the database entities and their relationships.
- **Security Layer:** Handles JWT generation, validation, and authorization.
- **Tenant Layer:** Maintains the current tenant context throughout the request lifecycle.
- **DTO Layer:** Defines request and response objects to avoid exposing internal entities directly.

---

## 4. Multi-Tenancy Design

This application uses a **single database with a shared schema**.

Multiple organizations share the same database tables, but every user and task belongs to a particular tenant.

### Database Entities

#### Tenant

| Field | Description |
|---|---|
| id | Primary key and tenant identifier |
| name | Organization name |
| created_at | Tenant creation timestamp |

#### User

| Field | Description |
|---|---|
| id | User primary key |
| email | User email |
| password | BCrypt-encoded password |
| role | ADMIN, MANAGER, or USER |
| tenant_id | Foreign key referencing Tenant |

#### Task

| Field | Description |
|---|---|
| id | Task primary key |
| title | Task title |
| description | Task description |
| status | TODO, IN_PROGRESS, or COMPLETED |
| assigned_to | Foreign key referencing User |
| tenant_id | Foreign key referencing Tenant |

### Entity Relationships

- One tenant can have multiple users.
- One tenant can have multiple tasks.
- One user can be assigned multiple tasks.
- Each task belongs to one tenant.
- A task can optionally be assigned to a user.

The Tenant table uses its `id` as the tenant identifier. The User and Task tables store this identifier in their `tenant_id` columns.

---

## 5. Role-Based Access Control

The application supports three roles.

| Operation | ADMIN | MANAGER | USER |
|---|:---:|:---:|:---:|
| Create users | Yes | No | No |
| Create tasks | No | Yes | No |
| Assign tasks | No | Yes | No |
| View all tenant tasks | Yes | Yes | No |
| View assigned tasks | Yes | Yes | Yes |
| Update assigned task status | No | No | Yes |

### ADMIN

- Can create users within their own tenant.
- Can view all tasks belonging to their tenant.
- Cannot access another tenant's data.

### MANAGER

- Can create tasks within their tenant.
- Can assign tasks to users belonging to the same tenant.
- Can view all tasks belonging to their tenant.

### USER

- Can view only tasks assigned to them.
- Can update the status of their assigned tasks.
- Cannot access or modify tasks assigned to other users.

---

## 6. JWT Authentication and Authorization

JWT is used to authenticate users and identify their tenant and role.

### JWT Payload

After successful login, the application generates a signed JWT containing:

```json
{
  "sub": "admin@techpulse.com",
  "tenant_id": 1,
  "role": "ADMIN",
  "iat": 1793700000,
  "exp": 1793703600
}
```

The timestamps above are illustrative. Actual token timestamps are generated when the user logs in.

### Authentication Flow

1. A user submits their email and password through the login API.
2. The application retrieves the user from the database.
3. BCrypt verifies the submitted password against the stored password hash.
4. If the credentials are valid, the application generates a JWT.
5. The JWT contains the user's email, tenant ID, role, issued-at time, and expiration time.
6. The client sends the JWT in the Authorization header for secured requests.

Example:

```http
Authorization: Bearer <JWT_TOKEN>
```

### Custom JWT Authentication Filter

The application uses Spring Security's `OncePerRequestFilter`.

The filter performs the following operations:

1. Reads the Authorization header.
2. Extracts the JWT from the Bearer token.
3. Validates the JWT signature and expiration.
4. Extracts the email, tenant ID, and role from JWT claims.
5. Retrieves the corresponding user from the database.
6. Verifies that the tenant ID and role in the JWT match the current database user.
7. Creates an authenticated Spring Security principal.
8. Stores the tenant ID in `TenantContext`.
9. Continues the filter chain.

The tenant context is cleared in a `finally` block after request processing to prevent tenant information from leaking between reused application threads.

### Tenant Context

A `ThreadLocal` variable is used to store the current tenant ID during request processing.

This allows service methods to obtain the authenticated tenant ID without accepting it from the client.

Example:

```java
Long tenantId = TenantContext.getTenantId();
```

The tenant context is cleared after the request completes.

---

## 7. Tenant Data Isolation

Tenant isolation is the most important security feature of this application.

The application ensures that users can access only records belonging to their authenticated tenant.

### Implementation

**1. Tenant ID from JWT**

The tenant ID is extracted from the verified JWT, not from the request body or URL.

**2. Repository-Level Filtering**

Repository methods include tenant-specific conditions.

Examples:

```java
findByTenant_Id(tenantId);

findByIdAndTenant_Id(taskId, tenantId);

findByTenant_IdAndAssignedTo_Id(tenantId, userId);
```

**3. Task Assignment Validation**

Before assigning a task, the application verifies:

- The task belongs to the current tenant.
- The target user belongs to the same tenant.

**4. Task Status Validation**

Before updating task status, the application verifies:

- The task belongs to the current tenant.
- The task is assigned to the authenticated user.

**5. Cross-Tenant Access Prevention**

If a task or user does not belong to the current tenant, the application does not allow the operation.

This prevents unauthorized cross-tenant data access.

---

## 8. REST API Endpoints

Base URL:

```text
http://localhost:8080
```

### Authentication and Tenant Registration

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/tenant/register` | Register a new tenant and its first ADMIN | Public |
| POST | `/auth/login` | Authenticate user and generate JWT | Public |

#### Register Tenant

Request:

```json
{
  "tenantName": "TechPulse",
  "email": "admin@techpulse.com",
  "password": "Admin@123"
}
```

Response: `201 Created`

#### Login

Request:

```json
{
  "email": "admin@techpulse.com",
  "password": "Admin@123"
}
```

Response: `200 OK`

The response contains the JWT token, token type, tenant ID, email, role, and token expiration duration.

### User Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/users` | Create a user under the authenticated tenant | ADMIN |

Request:

```json
{
  "email": "manager@techpulse.com",
  "password": "Manager@123",
  "role": "MANAGER"
}
```

The tenant ID is automatically obtained from the authenticated user's JWT context.

### Task Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/tasks` | Create a task | MANAGER |
| PUT | `/tasks/{id}/assign` | Assign a task to a user | MANAGER |
| PUT | `/tasks/{id}/status` | Update status of an assigned task | USER |
| GET | `/tasks` | Retrieve tasks based on the user's role | ADMIN, MANAGER, USER |

#### Create Task

Request:

```json
{
  "title": "Develop Login API",
  "description": "Implement JWT authentication using Spring Security"
}
```

#### Assign Task

Request:

```http
PUT /tasks/1/assign
```

Body:

```json
{
  "userId": 3
}
```

The application checks that both the task and target user belong to the current tenant.

#### Update Task Status

Request:

```http
PUT /tasks/1/status
```

Body:

```json
{
  "status": "IN_PROGRESS"
}
```

Supported task statuses:

- `TODO`
- `IN_PROGRESS`
- `COMPLETED`

#### Get Tasks

```http
GET /tasks
```

The response depends on the authenticated role:

- ADMIN: All tasks in the current tenant.
- MANAGER: All tasks in the current tenant.
- USER: Only tasks assigned to the current user.

---

## 9. Project Package Structure

The project follows this package organization:

```text
com.techpulse.taskmanagement
│
├── config
│   ├── SecurityConfig.java
│   └── OpenApiConfig.java
│
├── controller
│   ├── AuthController.java
│   └── TaskController.java
│
├── dto
│   ├── request
│   │   ├── TenantRegistrationRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── CreateTaskRequest.java
│   │   ├── AssignTaskRequest.java
│   │   └── UpdateTaskStatusRequest.java
│   │
│   └── response
│       ├── RegistrationResponse.java
│       ├── LoginResponse.java
│       ├── UserResponse.java
│       └── TaskResponse.java
│
├── entity
│   ├── Tenant.java
│   ├── User.java
│   └── Task.java
│
├── enums
│   ├── Role.java
│   └── TaskStatus.java
│
├── exception
│   └── GlobalExceptionHandler.java
│
├── filter
│   └── JwtAuthenticationFilter.java
│
├── repository
│   ├── TenantRepository.java
│   ├── UserRepository.java
│   └── TaskRepository.java
│
├── security
│   └── JwtService.java
│
├── service
│   ├── AuthService.java
│   ├── UserService.java
│   └── TaskService.java
│
├── tenant
│   └── TenantContext.java
│
└── TaskmanagementApplication.java
```

---

## 10. Configuration and Database

The application uses an in-memory H2 database.

Database configuration is maintained in `application.yml`.

Important configuration:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: ""

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

H2 Console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:taskdb
```

Username: `sa`

Password: leave blank.

**Note:** This is an in-memory database. Data is not intended to persist after the application process stops.

---

## 11. How to Run the Project

### Prerequisites

- Java Development Kit 17
- Maven
- Spring Tool Suite (STS) or another Java IDE

### Step 1: Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

### Step 2: Navigate to the Project

```bash
cd taskmanagement
```

### Step 3: Build the Application

On systems with Maven installed:

```bash
mvn clean install
```

### Step 4: Run the Application

```bash
mvn spring-boot:run
```

Alternatively, run `TaskmanagementApplication.java` as a Spring Boot application from STS.

### Step 5: Open Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

Use Swagger to register tenants, log in, authorize with the JWT, and test the secured endpoints.

---

## 12. API Testing and Security Test Scenarios

The following scenarios should be verified before submission.

| Test Scenario | Expected Result |
|---|---|
| Register a tenant | Tenant and first ADMIN are created |
| Login with valid credentials | JWT is generated |
| Login with invalid credentials | Authentication is rejected |
| ADMIN creates a user | User is created in ADMIN's tenant |
| MANAGER creates a task | Task is created in MANAGER's tenant |
| MANAGER assigns a task to same-tenant user | Assignment succeeds |
| MANAGER assigns a task to another tenant's user | Request is rejected |
| USER views tasks | Only assigned tasks are returned |
| USER updates assigned task status | Update succeeds |
| USER updates another user's task | Request is rejected |
| Tenant A accesses Tenant B's task | Access is rejected |
| Request without a JWT accesses secured API | Authentication is required |

Automated test coverage and the final test results should be recorded after running the test suite.

---

## 13. Security Considerations

- Passwords are stored using BCrypt hashing.
- JWT tokens are digitally signed and validated.
- JWT expiration is enforced.
- Tenant ID and role are included in JWT claims.
- The authenticated tenant is resolved from the JWT.
- Client-provided tenant IDs are not trusted.
- Repository queries filter by tenant ID.
- Task assignment validates tenant ownership.
- Task status updates validate task ownership and assignment.
- Spring Security enforces role-based access control.
- Tenant context is cleared after request processing.

**Development configuration warning:** The fallback JWT secret in `application.yml` is for local development only. A production deployment must use a securely generated secret supplied through environment configuration. Never commit production secrets to GitHub.

---

## 14. Future Enhancements

Possible improvements include:

- PostgreSQL or MySQL database integration.
- Refresh token implementation.
- Pagination and sorting for task retrieval.
- Task due dates and priorities.
- Audit logging for task updates.
- More comprehensive automated integration tests.
- Docker-based deployment.
- Production-ready exception handling and monitoring.

---

## 15. Conclusion

The Multi-Tenant Task Management System demonstrates the implementation of a secure shared-database multi-tenant architecture using Spring Boot and Spring Security.

The application combines JWT-based authentication, role-based authorization, tenant context management, and repository-level tenant filtering to ensure that each organization can securely manage its own users and tasks without accessing another organization's data.

The project demonstrates practical backend development concepts including REST API design, Spring Security filters, JWT customization, JPA entity relationships, service-layer validation, and multi-tenant data isolation.
