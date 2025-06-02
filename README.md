# listacas

Un proyecto con un frontend en React y un backend en Java Spring Boot.

## Frontend

El frontend está construido usando:
- React
- TypeScript
- Vite
- Tailwind CSS

### Desarrollo

Para ejecutar el servidor de desarrollo del frontend:
```bash
cd frontend
npm install # Si aún no lo has hecho
npm run dev
```

### Compilación (Build)

Para compilar el frontend para producción:
```bash
cd frontend
npm install # Si aún no lo has hecho
npm run build
```

## Backend

El backend consiste en una aplicación principal Spring Boot y un servicio de login separado.

### Backend Principal

El backend principal está construido usando:
- Java
- Spring Boot
- Gradle

#### Desarrollo

Para ejecutar el servidor de desarrollo del backend principal:
```bash
cd backend
./gradlew bootRun
```

#### Compilación (Build)

Para compilar el backend principal:
```bash
cd backend
./gradlew build
```

### Servicio de Login

El servicio de login se encuentra en `backend/login-service` y está construido usando:
- Java
- Spring Boot
- Maven

#### Desarrollo

Para ejecutar el servicio de login:
```bash
cd backend/login-service
./mvnw spring-boot:run
```

#### Compilación (Build)

Para compilar el servicio de login:
```bash
cd backend/login-service
./mvnw package
```