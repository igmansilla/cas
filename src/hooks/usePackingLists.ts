import { useLocalStorage } from "./useLocalStorage";
import { PackingCategory } from "../types";
import { packingCategories as defaultCategories } from "../data/packingList";

export function usePackingLists() {
  const [categories, setCategories] = useLocalStorage<PackingCategory[]>(
    "packing-list-categories",
    defaultCategories
  );

  const addCategory = (newCategory: PackingCategory) => {
    setCategories((prev) => [newCategory, ...prev]);
  };

  const editCategory = (id: string, newTitle: string) => {
    setCategories((prev) =>
      prev.map((cat) => (cat.id === id ? { ...cat, title: newTitle } : cat))
    );
  };

  const deleteCategory = (id: string) => {
    setCategories((prev) => prev.filter((cat) => cat.id !== id));
  };

  const addItem = (categoryId: string, itemText: string) => {
    setCategories((prev) =>
      prev.map((cat) =>
        cat.id === categoryId
          ? {
              ...cat,
              items: [{ id: `${Date.now()}`, text: itemText }, ...cat.items],
            }
          : cat
      )
    );
  };

  const editItem = (categoryId: string, itemId: string, newText: string) => {
    setCategories((prev) =>
      prev.map((cat) =>
        cat.id === categoryId
          ? {
              ...cat,
              items: cat.items.map((item) =>
                item.id === itemId ? { ...item, text: newText } : item
              ),
            }
          : cat
      )
    );
  };

  const deleteItem = (categoryId: string, itemId: string) => {
    setCategories((prev) =>
      prev.map((cat) =>
        cat.id === categoryId
          ? { ...cat, items: cat.items.filter((item) => item.id !== itemId) }
          : cat
      )
    );
  };

  const reorderCategories = (startIndex: number, endIndex: number) => {
    setCategories((prev) => {
      const result = Array.from(prev);
      const [removed] = result.splice(startIndex, 1);
      result.splice(endIndex, 0, removed);
      return result;
    });
  };

  const reorderItems = (
    categoryIndex: number,
    startIndex: number,
    endIndex: number
  ) => {
    setCategories((prev) =>
      prev.map((cat, i) => {
        if (i !== categoryIndex) return cat;

        const newItems = Array.from(cat.items);
        const [removed] = newItems.splice(startIndex, 1);
        newItems.splice(endIndex, 0, removed);

        return { ...cat, items: newItems };
      })
    );
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
