# listacas

A project with a React frontend and a Java Spring Boot backend.

## Frontend

The frontend is built using:
- React
- TypeScript
- Vite
- Tailwind CSS

### Development

To run the frontend development server:
```bash
cd frontend
npm install # If you haven't already
npm run dev
```

### Build

To build the frontend for production:
```bash
cd frontend
npm install # If you haven't already
npm run build
```

## Backend

The backend consists of a main Spring Boot application and a separate login service.

### Main Backend

The main backend is built using:
- Java
- Spring Boot
- Gradle

#### Development

To run the main backend development server:
```bash
cd backend
./gradlew bootRun
```

#### Build

To build the main backend:
```bash
cd backend
./gradlew build
```

### Login Service

The login service is located in `backend/login-service` and is built using:
- Java
- Spring Boot
- Maven

#### Development

To run the login service:
```bash
cd backend/login-service
./mvnw spring-boot:run
```

#### Build

To build the login service:
```bash
cd backend/login-service
./mvnw package
```