export interface PackingCategory {
  id: string; // Identificador único de la categoría
  title: string; // Título de la categoría
  items: { id: string; text: string }[]; // Ítems con IDs únicos
}

export interface CheckedItems {
  [key: string]: boolean;
}

export interface PackingItem {
  id: string;
  text: string;
  isChecked: boolean;
}

// Tipos que ya deberían estar en api.ts pero por si se usan directamente en componentes
export type {
  User,
  Acampante,
  AcampanteRequest,
  Dirigente,
  DirigenteRequest,
  Evento,
  EventoRequest,
  Mensaje,
  MensajeRequest,
  PackingListDto,
  PackingListCategoryDto,
  PackingListItemDto,
  AssistanceRecord,
  AssistanceRecordRequest,
  UserAssistanceOnDateRequest,
  UserData, // Asegurarse que UserData se exporta desde api.ts o se define aquí
} from './api';