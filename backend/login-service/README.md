# Login Service

This is a Spring Boot application providing login functionality.

## Running with PostgreSQL (Docker)

This service is configured to connect to a PostgreSQL database. You can run a PostgreSQL instance using Docker Compose.

**Prerequisites:**
- Docker and Docker Compose installed.

**Steps:**

1.  **Start PostgreSQL Container:**
    Navigate to the `backend/login-service` directory in your terminal and run:
    ```bash
    docker-compose up -d
    ```
    This will start a PostgreSQL container in detached mode. The database will be named `mylogindb` with user `dbuser` and password `dbpassword`, accessible on `localhost:5432`. Data is persisted in a Docker volume.

2.  **Run the Spring Boot Application:**
    Once the PostgreSQL container is running, you can start the Spring Boot application using your IDE or by running the following Maven command from the `backend/login-service` directory:
    ```bash
    ./mvnw spring-boot:run
    ```
    (or `mvn spring-boot:run` if you don't have the wrapper or it's not executable)

    The application will connect to the PostgreSQL database specified in `application.properties`. The `DataInitializer` will create test users ('user'/'password' and 'admin'/'adminpass') if they don't already exist.

3.  **Access the Application:**
    Open your web browser and go to `http://localhost:8080/login`.

**Stopping PostgreSQL Container:**
To stop the PostgreSQL container, run the following command from the `backend/login-service` directory:
```bash
docker-compose down
```
To stop and remove the data volume (useful for a clean restart):
```bash
docker-compose down -v
```

## User Roles and Management

The application now distinguishes between different types of users with specific roles:

*   **ROLE_ADMIN:** Administrators with full access to the system, including managing Dirigentes and potentially other administrative tasks.
*   **ROLE_DIRIGENTE:** Leaders or organizers who can manage Acampantes and related activities. They have user accounts to log in.
*   **ROLE_ACAMPANTE:** Represents campers. Currently, Acampantes do not have login accounts and are managed by Dirigentes. This role is defined for potential future use or for categorizing data.

**Initial Sample Users (created by `DataInitializer`):**

*   **Admin:**
    *   Username: `admin`
    *   Password: `adminpass`
    *   Role: `ROLE_ADMIN`
*   **Dirigente (Standard):**
    *   Username: `dirigente1`
    *   Password: `dirigentepass`
    *   Role: `ROLE_DIRIGENTE`
*   **Dirigente (Admin Privileges):**
    *   Username: `superdirigente`
    *   Password: `superpass`
    *   Roles: `ROLE_DIRIGENTE`, `ROLE_ADMIN`

## API Endpoints

The following API endpoints are available for managing Dirigentes and Acampantes. Access is restricted by roles.

### Dirigentes API (`/api/dirigentes`)

*   `POST /api/dirigentes`: Create a new Dirigente (Admin only).
    *   Request Body: `{ "nombreCompleto": "...", "responsabilidades": "...", "username": "...", "password": "...", "roles": ["ROLE_DIRIGENTE"] }`
*   `GET /api/dirigentes`: Get all Dirigentes (Admin or Dirigente).
*   `GET /api/dirigentes/{id}`: Get Dirigente by ID (Admin or Dirigente).
*   `GET /api/dirigentes/byUsername/{username}`: Get Dirigente by username (Admin or Dirigente).
*   `PUT /api/dirigentes/{id}`: Update Dirigente details (Admin only).
    *   Request Body: `{ "nombreCompleto": "...", "responsabilidades": "..." }`
*   `DELETE /api/dirigentes/{id}`: Delete Dirigente (Admin only).

### Acampantes API (`/api/acampantes`)

*   `POST /api/acampantes`: Create a new Acampante (Dirigente or Admin).
    *   Request Body: `{ "nombreCompleto": "...", "edad": ..., "contactoEmergenciaNombre": "...", "contactoEmergenciaTelefono": "..." }`
*   `GET /api/acampantes`: Get all Acampantes (Dirigente or Admin).
*   `GET /api/acampantes/{id}`: Get Acampante by ID (Dirigente or Admin).
*   `PUT /api/acampantes/{id}`: Update Acampante details (Dirigente or Admin).
*   `DELETE /api/acampantes/{id}`: Delete Acampante (Dirigente or Admin).

Refer to the controller classes (`DirigenteController.java`, `AcampanteController.java`) and service classes for more details on request/response structures and business logic.
