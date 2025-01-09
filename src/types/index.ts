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