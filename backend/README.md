# Backend

Este proyecto está construido con Java y Spring Boot.

## Estructura del Proyecto

- `src/main/java`: Contiene el código fuente principal de la aplicación.
- `src/main/resources`: Contiene recursos estáticos, plantillas y archivos de configuración.
- `src/test/java`: Contiene el código fuente de las pruebas.
- `build.gradle`: El archivo de configuración de compilación para Gradle.

## Desarrollo y Compilación del Backend Principal

### Cómo Ejecutar (Desarrollo)

1. Asegúrate de tener Java JDK 17 o superior instalado.
2. Navega a la carpeta `backend` en tu terminal.
3. Ejecuta el comando `./gradlew bootRun`.
4. El servidor backend se iniciará en el puerto predeterminado (generalmente 8080).

### Compilación (Build)

Para compilar el backend principal:
```bash
./gradlew build
```

## Servicio de Login

El servicio de login se encuentra en `backend/login-service`. Está construido usando:
- Java
- Spring Boot
- Maven

### Desarrollo

Para ejecutar el servicio de login:
```bash
cd login-service
./mvnw spring-boot:run
```

### Compilación (Build)

Para compilar el servicio de login:
```bash
cd login-service
./mvnw package
```

## Database Setup

This project uses a PostgreSQL database running in a Docker container.

To start the database:
1. Make sure you have Docker and Docker Compose installed.
2. Navigate to the `backend` directory in your terminal.
3. Run the command: `docker-compose up -d`

This will start the PostgreSQL container in detached mode. The database will be accessible on `localhost:5432` with the username `myuser`, password `mypassword`, and database name `mydatabase`, as configured in `docker-compose.yml` and `application.properties`.
