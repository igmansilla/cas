# Sistema de Gestión de Campamentos - Backend

Este es el backend del sistema de gestión de campamentos, construido con Java y Spring Boot. Proporciona una API REST para la gestión de acampantes, dirigentes y funcionalidades administrativas con un sistema de autenticación robusto y modular.

## 🏗️ Arquitectura del Sistema

### Tecnologías Principales
- **Java 17+** - Lenguaje de programación
- **Spring Boot 3.x** - Framework principal
- **Spring Security** - Autenticación y autorización
- **PostgreSQL** - Base de datos
- **Gradle** - Gestión de dependencias y build
- **Docker** - Containerización de la base de datos

### Estructura del Proyecto

```
src/main/java/com/cas/login/
├── config/              # Configuraciones de seguridad modularizadas
│   ├── SecurityConfig.java       # Configuración principal
│   ├── SecurityRoles.java        # Constantes de roles
│   ├── SecurityEndpoints.java    # Configuración de endpoints
│   ├── SecurityHandlers.java     # Manejadores de autenticación
│   └── CookieLoggingFilter.java  # Filtro de logging
├── controller/          # Controladores REST
├── model/              # Entidades JPA
├── repository/         # Repositorios de datos
├── service/           # Lógica de negocio
└── DataInitializer.java # Inicialización de datos
```

## 🔐 Sistema de Seguridad

### Roles y Permisos

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **ADMIN** | Administrador del sistema | Acceso completo, gestión de dirigentes |
| **DIRIGENTE** | Líder de campamento | Gestión de acampantes, funciones organizativas |
| **USER** | Usuario básico | Acceso limitado a funcionalidades específicas |

### Endpoints de API

#### Públicos (sin autenticación)
- `GET /api/status` - Estado del servicio
- `GET /api/health` - Verificación de salud

#### Autenticados
- `GET /api/user/me` - Información del usuario actual
- `POST /api/logout` - Cerrar sesión

#### Para DIRIGENTE y ADMIN
- `GET /api/acampantes` - Listar acampantes
- `POST /api/acampantes` - Crear acampante
- `PUT /api/acampantes/{id}` - Actualizar acampante
- `DELETE /api/acampantes/{id}` - Eliminar acampante

#### Solo ADMIN
- `GET /api/dirigentes` - Listar dirigentes
- `POST /api/dirigentes` - Crear dirigente
- `PUT /api/dirigentes/{id}` - Actualizar dirigente
- `DELETE /api/dirigentes/{id}` - Eliminar dirigente
- `GET /api/admin/**` - Funciones administrativas

## 🚀 Desarrollo y Ejecución

### Prerrequisitos
- Java JDK 17 o superior
- Docker y Docker Compose
- Git

### Configuración Inicial

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd listacas/backend
   ```

2. **Iniciar la base de datos**
   ```bash
   docker-compose up -d
   ```

3. **Ejecutar la aplicación**
   ```bash
   ./gradlew bootRun
   ```

La aplicación estará disponible en `http://localhost:8080`

### Comandos de Desarrollo

```bash
# Ejecutar en modo desarrollo con recarga automática
./gradlew bootRun

# Compilar la aplicación
./gradlew build

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean

# Ver dependencias
./gradlew dependencies
```

## 🗄️ Configuración de Base de Datos

### PostgreSQL con Docker

El proyecto utiliza PostgreSQL ejecutándose en un contenedor Docker. La configuración está definida en `docker-compose.yml`:

```yaml
# Configuración por defecto
Database: mydatabase
Username: myuser
Password: mypassword
Port: 5432
Host: localhost
```

### Inicialización de Datos

El sistema incluye un `DataInitializer` que crea usuarios y roles de ejemplo:

- **Admin**: `admin@cas.com` / `admin123`
- **Dirigente**: `dirigente@cas.com` / `dirigente123`
- **Usuario**: `user@cas.com` / `user123`

## 🔧 Configuración

### Variables de Entorno

Puedes personalizar la configuración mediante variables de entorno:

```bash
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/mydatabase
DB_USERNAME=myuser
DB_PASSWORD=mypassword

# Servidor
SERVER_PORT=8080

# Logging
LOGGING_LEVEL_ROOT=INFO
```

### Profiles de Spring

- `dev` - Desarrollo local
- `prod` - Producción
- `test` - Testing

```bash
# Ejecutar con profile específico
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📝 Autenticación

### Login via API

```bash
# Autenticación HTTP Basic
curl -X POST http://localhost:8080/perform_login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin@cas.com&password=admin123"
```

### Respuesta de Login Exitoso
```json
{
  "username": "admin@cas.com",
  "roles": ["ROLE_ADMIN"],
  "success": true,
  "message": "Authentication successful"
}
```

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
./gradlew test

# Tests específicos
./gradlew test --tests "UserControllerTests"

# Tests con reporte
./gradlew test jacocoTestReport
```

### Tests de Seguridad
Los tests incluyen verificación de:
- Autenticación correcta
- Autorización por roles
- Respuestas de error apropiadas
- Protección de endpoints

## 📚 Documentación Adicional

- [Configuración de Seguridad Detallada](./src/main/java/com/cas/login/config/README.md)
- [Módulo de Login](./src/main/java/com/cas/login/README.md)

## 🐛 Debugging

### Logs útiles
```bash
# Ver logs de la aplicación
./gradlew bootRun --debug

# Logs de seguridad
logging.level.org.springframework.security=DEBUG
```

### Herramientas de Desarrollo
- Activar DevTools para recarga automática
- Usar perfiles de desarrollo
- Configurar logging detallado

## 🚢 Despliegue

### Construcción para Producción
```bash
# Crear JAR ejecutable
./gradlew bootJar

# El archivo estará en build/libs/
java -jar build/libs/login-service-0.0.1-SNAPSHOT.jar
```

### Docker (Futuro)
```bash
# Construir imagen Docker
docker build -t campamentos-backend .

# Ejecutar contenedor
docker run -p 8080:8080 campamentos-backend
```

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.
