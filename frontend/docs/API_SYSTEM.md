# Documentación del Sistema de API

## Resumen

Este proyecto utiliza un enfoque estandarizado de la industria para el manejo de llamadas HTTP y el estado de la API en una aplicación React con TypeScript. El sistema está diseñado para ser escalable, mantenible y siguiendo las mejores prácticas modernas.

## Configuración del Proxy

### Vite Proxy Configuration
En `vite.config.ts`, todas las llamadas a la API son proxy-ficadas hacia `localhost:8082`:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8082',
      changeOrigin: true,
      secure: false,
    },
    '/login': {
      target: 'http://localhost:8082',
      changeOrigin: true,
      secure: false,
    },
    '/perform_login': {
      target: 'http://localhost:8082',
      changeOrigin: true,
      secure: false,
    },
    '/logout': {
      target: 'http://localhost:8082',
      changeOrigin: true,
      secure: false,
    },
  },
}
```

## Arquitectura del Sistema

### 1. Tipos TypeScript (`src/types/api.ts`)

Define todas las interfaces TypeScript para:
- Respuestas de API estandarizadas
- Entidades del dominio (Acampante, Dirigente, Evento, etc.)
- Estados de la API para los hooks

### 2. Servicio de API (`src/services/api.ts`)

#### Características:
- **Manejo centralizado de errores**: Clase `ApiError` personalizada
- **Gestión automática de CSRF**: Obtención y envío automático de tokens CSRF
- **Formato de respuesta estandarizado**: Todas las respuestas siguen el formato `ApiResponse<T>`
- **Configuración base**: URLs y headers configurables
- **Endpoints organizados**: Agrupados por recurso (auth, acampantes, dirigentes)

#### Ejemplo de uso:
```typescript
import { api } from '../services/api';

// Login
const response = await api.auth.login(username, password);

// Crear acampante
const newAcampante = await api.acampantes.create(formData);

// Obtener lista de acampantes
const acampantes = await api.acampantes.getAll();
```

### 3. Hooks Personalizados (`src/hooks/useApi.ts`)

#### `useApi<T>()` - Para llamadas individuales
```typescript
const createAcampante = useApi(api.acampantes.create);

// Uso
const handleSubmit = async () => {
  const result = await createAcampante.execute(formData);
  if (result?.success) {
    // Manejar éxito
  }
};

// Estado disponible
console.log(createAcampante.loading); // boolean
console.log(createAcampante.error);   // string | null
console.log(createAcampante.data);    // T | null
```

#### `useApiList<T>()` - Para listas paginadas
```typescript
const acampantesList = useApiList(api.acampantes.getAll);

useEffect(() => {
  acampantesList.execute();
}, []);

// Estado disponible
console.log(acampantesList.items);      // T[]
console.log(acampantesList.loading);    // boolean
console.log(acampantesList.error);      // string | null
console.log(acampantesList.pagination); // PaginationInfo
```

## Implementaciones Existentes

### 1. Sistema de Autenticación
- **LoginPage**: Utiliza `api.auth.login()` con manejo de errores y redirección automática
- **MainLayout**: Logout con `api.auth.logout()` y limpieza de estado local

### 2. Gestión de Acampantes
- **GestionAcampantesPage**: CRUD completo utilizando:
  - `useApiList()` para la lista de acampantes
  - `useApi()` para create, update, delete
  - Manejo de estados de carga y errores
  - Formularios modales con validación

## Estándares de Respuesta de la API

### Formato Exitoso
```typescript
{
  success: true,
  data: T,                    // Los datos solicitados
  message?: string,           // Mensaje opcional
  pagination?: {              // Para listas paginadas
    page: number,
    pageSize: number,
    total: number,
    totalPages: number
  }
}
```

### Formato de Error
```typescript
{
  success: false,
  error: string,              // Mensaje de error principal
  message?: string,           // Detalles adicionales
  errors?: Record<string, string[]> // Errores de validación
}
```

## Manejo de Errores

### Tipos de Error
1. **Errores de Red**: Conexión perdida, servidor no disponible
2. **Errores HTTP**: 400, 401, 403, 404, 500, etc.
3. **Errores de Validación**: Datos incorrectos en formularios
4. **Errores de Parsing**: JSON malformado

### Estrategia de Manejo
```typescript
try {
  const response = await api.acampantes.create(data);
  // Manejar éxito
} catch (error) {
  if (error instanceof ApiError) {
    // Error específico de la API
    console.error('API Error:', error.message, error.status);
  } else {
    // Error de red u otro tipo
    console.error('Unexpected error:', error);
  }
}
```

## Seguridad

### CSRF Protection
- Obtención automática de tokens CSRF desde cookies
- Envío automático en headers `X-XSRF-TOKEN`
- Validación en el backend Spring Boot

### Autenticación
- Sesiones basadas en cookies
- Validación de roles en el frontend
- Redirección automática a login si no autenticado

## Patrones de Uso Recomendados

### 1. Componentes de Lista
```typescript
const MyListComponent = () => {
  const dataList = useApiList(api.resource.getAll);
  
  useEffect(() => {
    dataList.execute();
  }, []);

  if (dataList.loading) return <LoadingSpinner />;
  if (dataList.error) return <ErrorMessage error={dataList.error} />;

  return (
    <div>
      {dataList.items.map(item => (
        <ItemCard key={item.id} item={item} />
      ))}
    </div>
  );
};
```

### 2. Formularios
```typescript
const MyFormComponent = () => {
  const createItem = useApi(api.resource.create);
  const [formData, setFormData] = useState(initialData);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await createItem.execute(formData);
    if (result?.success) {
      // Resetear formulario, cerrar modal, etc.
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* campos del formulario */}
      <button 
        type="submit" 
        disabled={createItem.loading}
      >
        {createItem.loading ? 'Guardando...' : 'Guardar'}
      </button>
      {createItem.error && <ErrorMessage error={createItem.error} />}
    </form>
  );
};
```

### 3. Estados de Carga
```typescript
// Deshabilitar botones durante la carga
<button disabled={api.loading}>
  {api.loading ? 'Procesando...' : 'Ejecutar'}
</button>

// Mostrar spinners
{api.loading && <LoadingSpinner />}

// Mostrar errores
{api.error && <div className="error">{api.error}</div>}
```

## Expansión del Sistema

### Agregar Nuevo Endpoint
1. Definir interfaces en `src/types/api.ts`
2. Agregar endpoint en `src/services/api.ts`
3. Crear componente usando los hooks apropiados

### Ejemplo - Agregar Eventos:
```typescript
// En api.ts
eventos: {
  getAll: () => apiRequest<Evento[]>('/api/eventos'),
  create: (data: EventoRequest) => apiRequest<Evento>('/api/eventos', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
}

// En componente
const EventosPage = () => {
  const eventosList = useApiList(api.eventos.getAll);
  const createEvento = useApi(api.eventos.create);
  
  // ... implementación similar a GestionAcampantesPage
};
```

## Testing

### Mockear API Calls
```typescript
// En tests
vi.mock('../services/api', () => ({
  api: {
    resource: {
      getAll: vi.fn().mockResolvedValue({
        success: true,
        data: mockData
      })
    }
  }
}));
```

## Monitoreo y Debugging

### Logs
- Todos los errores se registran en console.error
- Respuestas exitosas se pueden loggear en development

### Estado de la Aplicación
- Usar React DevTools para inspeccionar estado de los hooks
- Usar Network tab para verificar requests HTTP

## Mejores Prácticas

1. **Siempre usar los hooks personalizados** en lugar de fetch directo
2. **Manejar estados de carga** para mejor UX
3. **Mostrar errores de manera clara** al usuario
4. **Validar datos** antes de enviar al servidor
5. **Resetear formularios** después de operaciones exitosas
6. **Recargar listas** después de crear/actualizar/eliminar items
7. **Usar TypeScript** para todas las interfaces y tipos

Este sistema proporciona una base sólida y escalable para el manejo de APIs en aplicaciones React modernas.

## Backend API Endpoint Definitions

Esta sección detalla los endpoints específicos del backend disponibles para la aplicación.

### Packing List API

#### 1. Get Packing List

*   **Method:** `GET`
*   **URL:** `/api/packing-list`
*   **Description:** Retrieves the authenticated user's packing list. If no list exists for the user, a default/empty list structure may be returned by the backend (e.g., an object with a `null` or `undefined` `id` and empty `categories`).
*   **Authentication:** Required (e.g., session cookie, JWT token via Authorization header). The specific mechanism depends on the backend Spring Security configuration.
*   **Request Body:** None.
*   **Response Body (Success - 200 OK):** `PackingListDto`
    ```json
    {
      "id": 123, // Can be null if a new, never-saved list structure is returned
      "categories": [
        {
          "id": 1,
          "title": "Ropa",
          "displayOrder": 0,
          "items": [
            {
              "id": 101,
              "text": "Mochila anatómica",
              "isChecked": false,
              "displayOrder": 0
            }
            // ... more items
          ]
        }
        // ... more categories
      ],
      "createdAt": "2023-10-27T10:00:00Z", // ISO 8601 timestamp
      "updatedAt": "2023-10-27T10:00:00Z"  // ISO 8601 timestamp
    }
    ```
*   **Potential Error Status Codes:**
    *   `401 Unauthorized`: If the user is not authenticated or the session/token is invalid.
    *   `500 Internal Server Error`: If an unexpected server error occurs.

#### 2. Save Packing List

*   **Method:** `POST`
*   **URL:** `/api/packing-list`
*   **Description:** Saves or updates the authenticated user's packing list. The request should send the complete, current state of the packing list. If the list `id` in the payload is `null` or missing, a new list is created. If an `id` is provided, the existing list with that `id` (belonging to the user) is updated.
*   **Authentication:** Required.
*   **Request Body:** `PackingListDto`
    ```json
    {
      "id": 123, // Null/undefined for a new list, existing ID for an update
      "categories": [ // Send all categories and their items
        {
          "id": 1, // Null/undefined for a new category
          "title": "Ropa (Actualizada)",
          "displayOrder": 0,
          "items": [
            {
              "id": 101, // Null/undefined for a new item
              "text": "Mochila anatómica (Actualizada)",
              "isChecked": true,
              "displayOrder": 0
            },
            {
              // For a completely new item, 'id' would be omitted or null
              "text": "Nuevo Guante",
              "isChecked": false,
              "displayOrder": 1
            }
          ]
        }
      ],
      "createdAt": null, // Typically ignored by the server on save, managed by backend
      "updatedAt": null  // Typically ignored by the server on save, managed by backend
    }
    ```
*   **Response Body (Success - 200 OK):** The saved `PackingListDto`, including any newly assigned `id`s (for the list, categories, or items) and updated `createdAt` / `updatedAt` timestamps.
    ```json
    {
      "id": 123,
      "categories": [
        {
          "id": 1,
          "title": "Ropa (Actualizada)",
          "displayOrder": 0,
          "items": [
            {
              "id": 101,
              "text": "Mochila anatómica (Actualizada)",
              "isChecked": true,
              "displayOrder": 0
            },
            {
              "id": 102, // New ID assigned by backend
              "text": "Nuevo Guante",
              "isChecked": false,
              "displayOrder": 1
            }
          ]
        }
      ],
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:05:00Z" // Updated timestamp
    }
    ```
*   **Potential Error Status Codes:**
    *   `400 Bad Request`: If the request body is malformed or fails validation (e.g., missing required fields like `text` for an item, though the DTO structure is quite flexible).
    *   `401 Unauthorized`: If the user is not authenticated.
    *   `404 Not Found`: If the user associated with the authentication context is not found in the database (this should be rare if authentication is successful). This could also apply if trying to update a list with an ID that doesn't belong to the user, though the current implementation might just create a new list for the user or ignore the ID.
    *   `500 Internal Server Error`: If an unexpected server error occurs during processing or saving.
