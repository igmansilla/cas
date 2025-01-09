import { useLocalStorage } from './useLocalStorage';
import { PackingCategory } from '../types';
import { packingCategories as defaultCategories } from '../data/packingList';

export function usePackingLists() {
  const [categories, setCategories] = useLocalStorage<PackingCategory[]>(
    'packing-list-categories',
    defaultCategories
  );

  const addCategory = (title: string) => {
    setCategories(prev => [{ title, items: [] }, ...prev]);
  };

  const editCategory = (index: number, newTitle: string) => {
    setCategories(prev => prev.map((cat, i) => 
      i === index ? { ...cat, title: newTitle } : cat
    ));
  };

  const deleteCategory = (index: number) => {
    setCategories(prev => prev.filter((_, i) => i !== index));
  };

  const addItem = (categoryIndex: number, item: string) => {
    setCategories(prev => prev.map((cat, i) => 
      i === categoryIndex 
        ? { ...cat, items: [item, ...cat.items] }
        : cat
    ));
  };

  const editItem = (categoryIndex: number, itemIndex: number, newText: string) => {
    setCategories(prev => prev.map((cat, i) => 
      i === categoryIndex 
        ? {
            ...cat,
            items: cat.items.map((item, j) => 
              j === itemIndex ? newText : item
            )
          }
        : cat
    ));
  };

  const deleteItem = (categoryIndex: number, itemIndex: number) => {
    setCategories(prev => prev.map((cat, i) => 
      i === categoryIndex 
        ? {
            ...cat,
            items: cat.items.filter((_, j) => j !== itemIndex)
          }
        : cat
    ));
  };

  const reorderCategories = (startIndex: number, endIndex: number) => {
    setCategories(prev => {
      const result = Array.from(prev);
      const [removed] = result.splice(startIndex, 1);
      result.splice(endIndex, 0, removed);
      return result;
    });
  };

  const reorderItems = (categoryIndex: number, startIndex: number, endIndex: number) => {
    setCategories(prev => prev.map((cat, i) => {
      if (i !== categoryIndex) return cat;
      
      const newItems = Array.from(cat.items);
      const [removed] = newItems.splice(startIndex, 1);
      newItems.splice(endIndex, 0, removed);
      
      return { ...cat, items: newItems };
    }));
  };

  return {
    categories,
    addCategory,
    editCategory,
    deleteCategory,
    addItem,
    editItem,
    deleteItem,
    reorderCategories,
    reorderItems,
  };
}