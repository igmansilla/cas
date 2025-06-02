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

## Configuración de la Base de Datos

Este proyecto usa una base de datos **PostgreSQL** que corre en un contenedor Docker.

Para iniciar la base de datos:
1.  Asegúrate de tener **Docker** y **Docker Compose** instalados.
2.  Navega al directorio `backend` en tu terminal.
3.  Ejecuta el comando: `docker-compose up -d`

Esto iniciará el contenedor de PostgreSQL en modo *detached*. La base de datos será accesible en `localhost:5432` con el usuario `myuser`, la contraseña `mypassword` y el nombre de la base de datos `mydatabase`, tal como está configurado en `docker-compose.yml` y `application.properties`.
