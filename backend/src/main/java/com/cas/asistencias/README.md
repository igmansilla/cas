# Módulo de Asistencias

Este módulo permite llevar el control de asistencias a reuniones fecha a fecha, registrando quién vino y quién no asistió a cada reunión.

## Características Principales

### 🏢 Gestión de Reuniones
- **Crear reuniones**: Programa nuevas reuniones con fecha, hora, lugar y descripción
- **Estados de reunión**: PROGRAMADA, EN_CURSO, FINALIZADA, CANCELADA
- **Reuniones obligatorias**: Marca reuniones como obligatorias
- **Búsqueda y filtrado**: Busca reuniones por nombre, fecha, estado

### 👥 Control de Asistencias
- **Registro de asistencia**: Registra quien asiste a cada reunión
- **Estados de asistencia**: PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO
- **Hora de llegada/salida**: Registra horarios de entrada y salida
- **Observaciones**: Agrega notas sobre la asistencia
- **Prevención de duplicados**: No permite registrar la misma persona dos veces

### 📊 Reportes y Estadísticas
- **Reporte por reunión**: Lista completa de asistentes y ausentes
- **Historial por usuario**: Ver todas las asistencias de una persona
- **Porcentajes de asistencia**: Calcula automáticamente los porcentajes
- **Estadísticas**: Presentes, ausentes, tardanzas, justificados

## Estructura del Módulo

```
src/main/java/com/cas/asistencias/
├── controller/
│   ├── AsistenciaController.java          # API REST para asistencias
│   ├── ReunionController.java             # API REST para reuniones
│   └── AsistenciasExceptionHandler.java   # Manejo de errores
├── dto/
│   ├── AsistenciaDto.java                 # DTO para asistencias
│   ├── ReunionDto.java                    # DTO para reuniones
│   └── ReporteAsistenciaDto.java          # DTO para reportes
├── exception/
│   ├── AsistenciaNotFoundException.java   # Excepción asistencia no encontrada
│   ├── AsistenciaDuplicadaException.java  # Excepción asistencia duplicada
│   └── ReunionNotFoundException.java      # Excepción reunión no encontrada
├── model/
│   ├── Asistencia.java                    # Entidad de asistencia
│   └── Reunion.java                       # Entidad de reunión
├── repository/
│   ├── AsistenciaRepository.java          # Repositorio de asistencias
│   └── ReunionRepository.java             # Repositorio de reuniones
├── service/
│   ├── AsistenciaService.java             # Lógica de negocio asistencias
│   └── ReunionService.java                # Lógica de negocio reuniones
└── config/
    └── AsistenciasDataInitializer.java    # Datos de ejemplo
```

## API Endpoints

### Reuniones
- `GET /api/reuniones` - Obtener todas las reuniones (paginado)
- `GET /api/reuniones/todas` - Obtener todas las reuniones
- `GET /api/reuniones/{id}` - Obtener reunión por ID
- `POST /api/reuniones` - Crear nueva reunión
- `PUT /api/reuniones/{id}` - Actualizar reunión
- `DELETE /api/reuniones/{id}` - Eliminar reunión
- `GET /api/reuniones/estado/{estado}` - Reuniones por estado
- `GET /api/reuniones/proximas` - Próximas reuniones
- `GET /api/reuniones/mes/{year}/{month}` - Reuniones del mes
- `GET /api/reuniones/buscar?nombre=` - Buscar reuniones
- `PATCH /api/reuniones/{id}/estado?estado=` - Cambiar estado

### Asistencias
- `POST /api/asistencias` - Registrar asistencia
- `PUT /api/asistencias/{id}` - Actualizar asistencia
- `DELETE /api/asistencias/{id}` - Eliminar asistencia
- `GET /api/asistencias/reunion/{reunionId}` - Asistencias por reunión
- `GET /api/asistencias/usuario/{usuarioId}` - Asistencias por usuario
- `GET /api/asistencias/reunion/{reunionId}/usuario/{usuarioId}` - Asistencia específica
- `GET /api/asistencias/reporte/reunion/{reunionId}` - Reporte de asistencia
- `GET /api/asistencias/historial/usuario/{usuarioId}` - Historial de usuario
- `PATCH /api/asistencias/{id}/salida` - Marcar salida
- `POST /api/asistencias/multiple` - Registrar múltiples asistencias

## Modelos de Datos

### Reunión
```json
{
  "id": 1,
  "nombre": "Reunión Mensual",
  "descripcion": "Reunión de planificación mensual",
  "fechaReunion": "2024-01-15T14:00:00",
  "lugar": "Sala de Reuniones",
  "esObligatoria": true,
  "estado": "PROGRAMADA",
  "fechaCreacion": "2024-01-01T10:00:00",
  "fechaActualizacion": "2024-01-01T10:00:00",
  "totalAsistentes": 10,
  "presentes": 8,
  "ausentes": 2
}
```

### Asistencia
```json
{
  "id": 1,
  "reunionId": 1,
  "nombreReunion": "Reunión Mensual",
  "fechaReunion": "2024-01-15T14:00:00",
  "usuarioId": 1,
  "nombreUsuario": "Juan Pérez",
  "usernameUsuario": "jperez",
  "fechaRegistro": "2024-01-15T13:55:00",
  "estadoAsistencia": "PRESENTE",
  "horaLlegada": "2024-01-15T14:05:00",
  "horaSalida": "2024-01-15T16:00:00",
  "observaciones": "Llegó 5 minutos tarde",
  "registradoPor": "admin"
}
```

### Reporte de Asistencia
```json
{
  "reunionId": 1,
  "nombreReunion": "Reunión Mensual",
  "fechaReunion": "2024-01-15T14:00:00",
  "lugar": "Sala de Reuniones",
  "esObligatoria": true,
  "totalRegistrados": 10,
  "presentes": 7,
  "ausentes": 2,
  "tardanzas": 1,
  "justificados": 0,
  "porcentajeAsistencia": 70.0,
  "detalleAsistencias": [...]
}
```

## Estados

### Estados de Reunión
- **PROGRAMADA**: Reunión planificada pero no iniciada
- **EN_CURSO**: Reunión en progreso
- **FINALIZADA**: Reunión terminada
- **CANCELADA**: Reunión cancelada

### Estados de Asistencia
- **PRESENTE**: Persona asistió a la reunión
- **AUSENTE**: Persona no asistió
- **TARDANZA**: Persona llegó tarde
- **JUSTIFICADO**: Ausencia justificada

## Base de Datos

El módulo crea las siguientes tablas:

### Tabla `reuniones`
- `id`: ID único de la reunión
- `nombre`: Nombre de la reunión
- `descripcion`: Descripción detallada
- `fecha_reunion`: Fecha y hora programada
- `lugar`: Lugar de la reunión
- `es_obligatoria`: Si es obligatoria o no
- `estado`: Estado actual de la reunión
- `fecha_creacion`: Cuándo se creó
- `fecha_actualizacion`: Última actualización

### Tabla `asistencias`
- `id`: ID único de la asistencia
- `reunion_id`: ID de la reunión
- `user_id`: ID del usuario
- `fecha_registro`: Cuándo se registró
- `estado_asistencia`: Estado de la asistencia
- `hora_llegada`: Hora de llegada
- `hora_salida`: Hora de salida
- `observaciones`: Comentarios
- `registrado_por`: Quién lo registró

## Uso Típico

1. **Crear una reunión**:
   ```bash
   POST /api/reuniones
   {
     "nombre": "Reunión Semanal",
     "descripcion": "Revisión semanal de proyectos",
     "fechaReunion": "2024-01-20T10:00:00",
     "lugar": "Sala A",
     "esObligatoria": true
   }
   ```

2. **Registrar asistencia**:
   ```bash
   POST /api/asistencias
   {
     "reunionId": 1,
     "usuarioId": 1,
     "estadoAsistencia": "PRESENTE",
     "observaciones": "Llegó puntual"
   }
   ```

3. **Generar reporte**:
   ```bash
   GET /api/asistencias/reporte/reunion/1
   ```

## Datos de Ejemplo

El sistema incluye datos de ejemplo que se cargan automáticamente:
- 3 reuniones de ejemplo con diferentes estados
- Asistencias de ejemplo con diferentes estados
- Usuarios de prueba para demostrar funcionalidad

## Documentación API

La documentación completa de la API está disponible en Swagger UI:
`http://localhost:8082/swagger-ui.html`

## Características Técnicas

- **Framework**: Spring Boot 3.5.0
- **Base de datos**: PostgreSQL
- **ORM**: JPA/Hibernate
- **Migraciones**: Flyway
- **Documentación**: OpenAPI/Swagger
- **Logging**: SLF4J/Logback
- **Validación**: Spring Validation
- **Transacciones**: Spring Transaction Management
