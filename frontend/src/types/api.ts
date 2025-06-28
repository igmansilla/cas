// types/api.ts - Interfaces TypeScript para todas las respuestas de la API

// Respuesta base de la API
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  pagination?: PaginationInfo;
}

// Información de paginación
export interface PaginationInfo {
  page: number;
  pageSize: number;
  size: number; // Alias para compatibilidad
  total: number;
  totalPages: number;
}

// Usuario y autenticación
export interface User {
  username: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  username: string;
  roles: string[];
}

// Acampantes
export interface Acampante {
  id: number;
  nombreCompleto: string;
  edad: number;
  contactoEmergenciaNombre: string;
  contactoEmergenciaTelefono: string;
}

export interface AcampanteRequest {
  nombreCompleto: string;
  edad: number;
  contactoEmergenciaNombre: string;
  contactoEmergenciaTelefono: string;
}

// Dirigentes (estructura similar para futuras implementaciones)
export interface Dirigente {
  id: number;
  nombreCompleto: string;
  edad: number;
  especialidad: string;
  telefono: string;
  email: string;
}

export interface DirigenteRequest {
  nombreCompleto: string;
  edad: number;
  especialidad: string;
  telefono: string;
  email: string;
}

// Eventos (para futuras implementaciones)
export interface Evento {
  id: number;
  titulo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  ubicacion: string;
  responsable: string;
}

export interface EventoRequest {
  titulo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  ubicacion: string;
  responsable: string;
}

// Mensajes de chat (para futuras implementaciones)
export interface Mensaje {
  id: number;
  usuario: string;
  contenido: string;
  timestamp: string;
  tipo: 'texto' | 'imagen' | 'archivo';
}

export interface MensajeRequest {
  contenido: string;
  tipo: 'texto' | 'imagen' | 'archivo';
}

// Estados de API para hooks
export interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export interface ApiListState<T> {
  items: T[];
  loading: boolean;
  error: string | null;
  pagination: PaginationInfo;
}

// Packing List
export interface PackingListItemDto {
  id?: number; // Optional for new items not yet saved
  text: string;
  isChecked: boolean;
  displayOrder: number;
}

export interface PackingListCategoryDto {
  id?: number; // Optional for new categories not yet saved
  title: string;
  displayOrder: number;
  items: PackingListItemDto[];
}

export interface PackingListDto {
  id?: number; // Optional if the list itself is new
  categories: PackingListCategoryDto[];
  createdAt?: string; // ISO date string
  updatedAt?: string; // ISO date string
}
