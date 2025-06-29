// api.ts - Servicio centralizado de API con estándares de la industria

import type {
  ApiResponse,
  LoginResponse,
  PackingListDto,
  AssistanceRecord,
  AssistanceRecordRequest,
  UserAssistanceOnDateRequest,
  NewAcampanteCreateRequest, // Added
  NewAcampanteResponse,   // Added
  UserDto,
} from '../types/api';

// Re-export para compatibilidad con código existente
export type {
  ApiResponse,
  LoginResponse,
  PackingListDto,
  AssistanceRecord,
  AssistanceRecordRequest,
  UserAssistanceOnDateRequest,
  NewAcampanteCreateRequest, // Added
  NewAcampanteResponse,   // Added
  UserDto,
};
export interface UserData {
  username: string;
  roles: string[];
}

// Interfaces para Acampantes
export interface Acampante {
  id: number;
  nombreCompleto: string;
  edad: number;
  contactoEmergenciaNombre: string;
  contactoEmergenciaTelefono: string;
}

export interface AcampanteForm {
  nombreCompleto: string;
  edad: number;
  contactoEmergenciaNombre: string;
  contactoEmergenciaTelefono: string;
}

// Configuración base de la API
const API_BASE_URL = import.meta.env.DEV ? '' : 'http://localhost:8082';

// Clase para errores personalizados de API
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public response?: unknown
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// Helper para obtener el token CSRF
const getCsrfToken = (): string | null => {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN') {
      return value;
    }
  }
  return null;
};

// Helper para obtener credenciales de usuario
const getUserCredentials = (): { username: string; password: string } | null => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;
  
  try {
    const user = JSON.parse(userStr);
    // Asumimos que las credenciales se guardan temporalmente después del login
    const credentials = localStorage.getItem('userCredentials');
    if (!credentials) return null;
    
    const { username, password } = JSON.parse(credentials);
    return { username, password };
  } catch {
    return null;
  }
};

// Función base para hacer requests con manejo estándar de errores
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  // Agregar autenticación HTTP Basic para endpoints de API
  if (endpoint.startsWith('/api/')) {
    const credentials = getUserCredentials();
    if (credentials) {
      const basicAuth = btoa(`${credentials.username}:${credentials.password}`);
      defaultHeaders['Authorization'] = `Basic ${basicAuth}`;
    }
  }

  // Agregar token CSRF para requests que lo necesiten
  if (options.method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(options.method)) {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      defaultHeaders['X-XSRF-TOKEN'] = csrfToken;
    }
  }

  const config: RequestInit = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    
    // Intentar parsear la respuesta como JSON
    let responseData;
    try {
      responseData = await response.json();
    } catch {
      responseData = {};
    }

    if (!response.ok) {
      throw new ApiError(response.status, responseData.message || response.statusText, responseData);
    }

    // Normalizar la respuesta a formato estándar
    return {
      success: true,
      data: responseData,
      message: responseData.message,
    };
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    
    // Error de red u otro error
    throw new ApiError(0, 'Network error or unexpected error occurred', error);
  }
}

// Función específica para login con formato especial
export async function login(username: string, password: string): Promise<ApiResponse<LoginResponse>> {
  const csrfToken = getCsrfToken();
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/x-www-form-urlencoded',
  };
  
  // Solo agregar CSRF token si existe
  if (csrfToken) {
    headers['X-XSRF-TOKEN'] = csrfToken;
  }
  try {
    const response = await fetch(`${API_BASE_URL}/perform_login`, {
      method: 'POST',
      headers,
      body: new URLSearchParams({ username, password }),
    });

    const responseData = await response.json();

    if (!response.ok) {
      throw new ApiError(response.status, responseData.message || 'Login failed', responseData);
    }

    // Guardar credenciales para autenticación HTTP Basic en endpoints API
    localStorage.setItem('userCredentials', JSON.stringify({ username, password }));

    return {
      success: true,
      data: responseData,
      message: 'Login successful',
    };
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(0, 'Network error during login', error);
  }
}

// Función para logout
export async function logout(): Promise<ApiResponse<void>> {
  try {
    const response = await fetch(`${API_BASE_URL}/logout`, {
      method: 'POST',
      headers: {},
    });

    if (!response.ok) {
      console.warn('Backend logout failed, but proceeding with client-side logout');
    }

    // Limpiar credenciales almacenadas
    localStorage.removeItem('userCredentials');

    return {
      success: true,
      message: 'Logout successful',
    };
  } catch (error) {
    console.warn('Logout error:', error);
    // Limpiar credenciales incluso si hay error
    localStorage.removeItem('userCredentials');
    return {
      success: true,
      message: 'Logout completed (with backend error)',
    };
  }
}

// API endpoints para recursos
export const api = {
  // Auth
  auth: {
    login,
    logout,
    getCurrentUser: () => apiRequest<UserData>('/api/user/me'),
  },  // Acampantes
  acampantes: {
    getAll: () => apiRequest<Acampante[]>('/api/acampantes'),
    getById: (id: number) => apiRequest<Acampante>(`/api/acampantes/${id}`),
    create: (data: AcampanteForm) => apiRequest<Acampante>('/api/acampantes', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    update: (id: number, data: AcampanteForm) => apiRequest<Acampante>(`/api/acampantes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
    delete: (id: number) => apiRequest<void>(`/api/acampantes/${id}`, {
      method: 'DELETE',
    }),
  },

  // Dirigentes
  dirigentes: {
    getAll: () => apiRequest<unknown>('/api/dirigentes'),
    getById: (id: number) => apiRequest<unknown>(`/api/dirigentes/${id}`),
    getByUsername: (username: string) => apiRequest<unknown>(`/api/dirigentes/byUsername/${username}`),
    create: (data: unknown) => apiRequest<unknown>('/api/dirigentes', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    update: (id: number, data: unknown) => apiRequest<unknown>(`/api/dirigentes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
    delete: (id: number) => apiRequest<unknown>(`/api/dirigentes/${id}`, {
      method: 'DELETE',
    }),
    // New method for a Dirigente to create an Acampante
    createAcampanteByDirigente: (data: NewAcampanteCreateRequest) =>
      apiRequest<NewAcampanteResponse>('/api/dirigentes/me/acampantes', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
  },

  // Packing List
  packingList: {
    get: () => apiRequest<PackingListDto>('/api/packing-list'),
    save: (data: PackingListDto) => apiRequest<PackingListDto>('/api/packing-list', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  },

  // Assistance
  assistance: {
    record: (data: AssistanceRecordRequest) =>
      apiRequest<AssistanceRecord>('/api/assistance', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    getForUserAndDate: (userId: number, date: string) =>
      apiRequest<AssistanceRecord>(`/api/assistance/user/${userId}/date/${date}`),
    getForUser: (userId: number) =>
      apiRequest<AssistanceRecord[]>(`/api/assistance/user/${userId}`),
    getByDate: (date: string) =>
      apiRequest<AssistanceRecord[]>(`/api/assistance/date/${date}`),
    getAll: () => apiRequest<AssistanceRecord[]>('/api/assistance'),
    getForUsersOnDate: (data: UserAssistanceOnDateRequest) =>
      apiRequest<AssistanceRecord[]>('/api/assistance/users-on-date', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    delete: (assistanceId: number) =>
      apiRequest<void>(`/api/assistance/${assistanceId}`, {
        method: 'DELETE',
      }),
    getForSupervisedCampers: (dirigenteId: number, date: string) =>
      apiRequest<AssistanceRecord[]>(`/api/assistance/dirigente/${dirigenteId}/supervised/date/${date}`),
  },

  supervision: {
    assignAcampanteToDirigente: (dirigenteId: number, acampanteId: number) =>
      apiRequest<ApiResponse<unknown>>(`/api/supervision/dirigente/${dirigenteId}/assign/${acampanteId}`, { // ApiResponse<unknown> o un tipo más específico para el mensaje
        method: 'POST',
      }),
    removeAcampanteFromDirigente: (dirigenteId: number, acampanteId: number) =>
      apiRequest<ApiResponse<unknown>>(`/api/supervision/dirigente/${dirigenteId}/remove/${acampanteId}`, {
        method: 'DELETE',
      }),
    getSupervisedCampers: (dirigenteId: number) =>
      apiRequest<UserDto[]>(`/api/supervision/dirigente/${dirigenteId}/campers`),
    getSupervisorsForAcampante: (acampanteId: number) =>
      apiRequest<UserDto[]>(`/api/supervision/acampante/${acampanteId}/supervisors`),
    getAllDirigentes: () => apiRequest<UserDto[]>('/api/supervision/dirigentes'),
    getAllAcampantes: () => apiRequest<UserDto[]>('/api/supervision/acampantes'),
  },

  // Generic API call para endpoints personalizados
  call: apiRequest,
};

export default api;
