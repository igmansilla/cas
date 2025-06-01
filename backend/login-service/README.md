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
