export interface PackingCategory {
  title: string;
  items: string[];
}

export interface CheckedItems {
  [key: string]: boolean;
}

export interface PackingItem {
  id: string;
  text: string;
  isChecked: boolean;
}