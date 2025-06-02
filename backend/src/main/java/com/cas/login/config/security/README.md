# Configuración de Seguridad - Sistema de Autenticación

## Descripción General

Este módulo implementa la configuración de seguridad para el sistema de gestión de campamentos, utilizando Spring Security con una arquitectura modularizada que separa responsabilidades y facilita el mantenimiento.

## Arquitectura de Seguridad

### Componentes Principales

1. **SecurityConfig**: Configuración principal que define las cadenas de filtros
2. **SecurityRoles**: Constantes centralizadas para roles del sistema
3. **SecurityEndpoints**: Configuración de permisos por endpoint
4. **SecurityHandlers**: Manejadores de eventos de autenticación

### Cadenas de Filtros

El sistema utiliza dos cadenas de filtros separadas:

#### 1. API Security Filter Chain (Orden 1)
- **Rutas**: `/api/**`
- **Método de autenticación**: HTTP Basic
- **CSRF**: Deshabilitado (para APIs stateless)
- **Respuesta no autorizada**: HTTP 401

#### 2. Form Login Filter Chain (Orden 2)
- **Rutas**: Todas las demás
- **Método de autenticación**: Form login
- **CSRF**: Deshabilitado
- **Respuestas**: JSON personalizadas

## Roles del Sistema

### Jerarquía de Roles

1. **ADMIN** - Administrador del sistema
   - Acceso completo a todas las funcionalidades
   - Gestión de dirigentes
   - Acceso a endpoints administrativos

2. **DIRIGENTE** - Líder de campamento
   - Gestión de acampantes
   - Acceso a funcionalidades de organización

3. **USER** - Usuario básico
   - Acceso limitado a funcionalidades específicas

### Constantes de Roles

```java
// Roles base (sin prefijo)
SecurityRoles.ADMIN = "ADMIN"
SecurityRoles.DIRIGENTE = "DIRIGENTE"
SecurityRoles.USER = "USER"

// Roles con prefijo ROLE_ para Spring Security
SecurityRoles.ROLE_ADMIN = "ROLE_ADMIN"
SecurityRoles.ROLE_DIRIGENTE = "ROLE_DIRIGENTE"
SecurityRoles.ROLE_USER = "ROLE_USER"
```

## Endpoints y Permisos

### Endpoints Públicos
- `/api/status` - Estado del servicio
- `/api/health` - Verificación de salud
- `/perform_login` - Procesamiento de login
- `/error` - Manejo de errores
- `/` - Raíz del sitio

### Endpoints con Autenticación

| Endpoint | Roles Requeridos | Descripción |
|----------|------------------|-------------|
| `/api/acampantes/**` | DIRIGENTE, ADMIN | Gestión de acampantes |
| `/api/dirigentes/**` | ADMIN | Gestión de dirigentes |
| `/api/admin/**` | ADMIN | Funciones administrativas |
| `/api/user/me` | Autenticado | Información del usuario actual |
| `/api/logout` | Autenticado | Cerrar sesión |

## Respuestas de Autenticación

### Login Exitoso
```json
{
  "username": "usuario@ejemplo.com",
  "roles": ["ROLE_ADMIN"],
  "success": true,
  "message": "Authentication successful"
}
```

### Login Fallido
```json
{
  "success": false,
  "error": "Authentication failed",
  "message": "Bad credentials"
}
```

### Logout Exitoso
```json
{
  "success": true,
  "message": "Logout successful"
}
```

## Configuración de Filtros

### CookieLoggingFilter
- **Posición**: Después de `UsernamePasswordAuthenticationFilter`
- **Propósito**: Registro y monitoreo de cookies para debugging

### Autenticación HTTP Basic
- **Aplicación**: Solo endpoints `/api/**`
- **Uso**: Principalmente para integraciones y testing

## Seguridad Adicional

### Protección CSRF
- **Estado**: Deshabilitado
- **Razón**: API stateless, uso de tokens para autenticación

### Manejo de Excepciones
- **API**: Retorna HTTP 401 para no autenticados
- **Forms**: Redirección a manejadores personalizados

## Configuración de Desarrollo

Para desarrollo local, asegúrate de:

1. Tener usuarios con roles apropiados en la base de datos
2. Configurar las propiedades de la aplicación correctamente
3. Usar HTTPS en producción para proteger credenciales

## Extensibilidad

### Agregar Nuevos Roles
1. Añadir constante en `SecurityRoles`
2. Actualizar `SecurityEndpoints` con los permisos correspondientes
3. Modificar la lógica de negocio según sea necesario

### Agregar Nuevos Endpoints
1. Definir permisos en `SecurityEndpoints`
2. Documentar en esta guía
3. Actualizar tests de seguridad

### Personalizar Respuestas
1. Modificar `SecurityHandlers` según necesidades
2. Mantener formato JSON consistente
3. Actualizar documentación de API

## Debugging y Monitoreo

### Logs de Seguridad
El sistema incluye filtros de logging para:
- Cookies de sesión
- Eventos de autenticación
- Accesos denegados

### Testing de Seguridad
Verificar:
- Acceso correcto según roles
- Respuestas de error apropiadas
- Funcionamiento de logout
- Protección de endpoints sensibles
