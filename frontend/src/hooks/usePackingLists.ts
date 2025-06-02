import { useState, useEffect, useCallback } from 'react';
import {
  PackingListDto,
  PackingListCategoryDto,
  PackingListItemDto,
} from '../types/api';
import api from '../services/api'; // Ensure this path is correct
import { packingCategories as defaultCategoriesData } from '../data/packingList'; // For initial structure if needed

// Helper to convert default data to DTO structure (if used for new users)
// This needs careful handling of IDs (backend assigns them) and displayOrder
const mapDefaultCategoryToDto = (
  category: typeof defaultCategoriesData[0],
  categoryIndex: number
): PackingListCategoryDto => ({
  id: undefined, // Backend will assign ID
  title: category.title,
  displayOrder: categoryIndex,
  items: category.items.map((item, itemIndex) => ({
    id: undefined, // Backend will assign ID
    text: item.text,
    isChecked: false, // Default to not checked
    displayOrder: itemIndex,
  })),
});

export function usePackingLists() {
  const [packingList, setPackingList] = useState<PackingListDto | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [lastSynced, setLastSynced] = useState<Date | null>(null);

  const getCategories = (): PackingListCategoryDto[] => {
    return packingList?.categories || [];
  };

  // Private persist function
  const persistList = useCallback(async (listToSave: PackingListDto) => {
    setIsLoading(true);
    setError(null);
    try {
      // Assign displayOrder before sending if not already meticulously managed
      const listWithOrder = {
        ...listToSave,
        categories: listToSave.categories.map((cat, catIndex) => ({
          ...cat,
          displayOrder: cat.displayOrder ?? catIndex,
          items: cat.items.map((item, itemIndex) => ({
            ...item,
            displayOrder: item.displayOrder ?? itemIndex,
          })),
        })),
      };

      const response = await api.packingList.save(listWithOrder);
      if (response.success && response.data) {
        setPackingList(response.data);
        setLastSynced(new Date());
      } else {
        setError(response.message || 'Failed to save packing list.');
        // Potentially revert optimistic update here or offer a retry mechanism
      }
    } catch (e: any) {
      setError(e.message || 'An unexpected error occurred while saving.');
      // Potentially revert
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const fetchList = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await api.packingList.get();
        if (response.success && response.data) {
          if (response.data.id) { // Check if it's a persisted list or a new transient one
            setPackingList(response.data);
          } else {
            // Backend returned a new, empty DTO.
            // Initialize with an empty structure or default local data.
            // For now, let's use an empty structure, assuming backend is source of truth.
            setPackingList({
              id: undefined,
              categories: [], // Or mapDefaultCategoryToDto if we want defaults for truly new users
              createdAt: undefined,
              updatedAt: undefined,
            });
          }
        } else {
          setError(response.message || 'Failed to fetch packing list.');
          // Initialize with a basic empty structure on failure to fetch
          setPackingList({ id: undefined, categories: [], createdAt: undefined, updatedAt: undefined });
        }
      } catch (e: any) {
        setError(e.message || 'An unexpected error occurred while fetching.');
        setPackingList({ id: undefined, categories: [], createdAt: undefined, updatedAt: undefined });
      } finally {
        setIsLoading(false);
      }
    };
    fetchList();
  }, []);


  const addCategory = useCallback(async (title: string) => {
    if (!packingList) return;
    const newCategory: PackingListCategoryDto = {
      // id is undefined for new categories; backend will assign
      title,
      items: [],
      displayOrder: packingList.categories.length, // Simple order at the end
    };
    const updatedList = {
      ...packingList,
      categories: [...packingList.categories, newCategory],
    };
    setPackingList(updatedList); // Optimistic update
    await persistList(updatedList);
  }, [packingList, persistList]);

  const editCategoryTitle = useCallback(async (categoryId: number, newTitle: string) => {
    if (!packingList) return;
    let categoryExists = false;
    const updatedCategories = packingList.categories.map((cat) => {
      if (cat.id === categoryId) {
        categoryExists = true;
        return { ...cat, title: newTitle };
      }
      return cat;
    });

    if (!categoryExists) { // Handle case where ID might be temp client-side if not yet persisted
        console.warn("Attempted to edit category with non-matching ID or not yet persisted ID:", categoryId);
        // Potentially find by a temporary client-side ID if that's a pattern used
        return; 
    }

    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);


  const deleteCategory = useCallback(async (categoryId: number) => {
    if (!packingList) return;
    const updatedCategories = packingList.categories.filter(cat => cat.id !== categoryId);
    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  const addItemToCategory = useCallback(async (categoryId: number, itemText: string) => {
    if (!packingList) return;
    let categoryFound = false;
    const updatedCategories = packingList.categories.map(cat => {
      if (cat.id === categoryId) {
        categoryFound = true;
        const newItem: PackingListItemDto = {
          // id is undefined for new items
          text: itemText,
          isChecked: false,
          displayOrder: cat.items.length, // Simple order at the end
        };
        return { ...cat, items: [...cat.items, newItem] };
      }
      return cat;
    });

    if (!categoryFound) {
        console.warn("Attempted to add item to category with non-matching ID or not yet persisted ID:", categoryId);
        return;
    }

    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  const editItemText = useCallback(async (categoryId: number, itemId: number, newText: string) => {
    if (!packingList) return;
    const updatedCategories = packingList.categories.map(cat => {
      if (cat.id === categoryId) {
        return {
          ...cat,
          items: cat.items.map(item =>
            item.id === itemId ? { ...item, text: newText } : item
          ),
        };
      }
      return cat;
    });
    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  const deleteItemInCategory = useCallback(async (categoryId: number, itemId: number) => {
    if (!packingList) return;
    const updatedCategories = packingList.categories.map(cat => {
      if (cat.id === categoryId) {
        return { ...cat, items: cat.items.filter(item => item.id !== itemId) };
      }
      return cat;
    });
    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  const handleToggleItem = useCallback(async (categoryId: number, itemId: number) => {
    if (!packingList) return;
    const updatedCategories = packingList.categories.map(cat => {
      if (cat.id === categoryId) {
        return {
          ...cat,
          items: cat.items.map(item =>
            item.id === itemId ? { ...item, isChecked: !item.isChecked } : item
          ),
        };
      }
      return cat;
    });
    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);
  
  const reorderCategories = useCallback(async (startIndex: number, endIndex: number) => {
    if (!packingList) return;
    const currentCategories = packingList.categories;
    const result = Array.from(currentCategories);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);

    const orderedResult = result.map((cat, index) => ({ ...cat, displayOrder: index }));

    const updatedList = { ...packingList, categories: orderedResult };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  const reorderItems = useCallback(async (categoryId: number, startIndex: number, endIndex: number) => {
    if (!packingList) return;
      
    const updatedCategories = packingList.categories.map(cat => {
      if (cat.id === categoryId) {
        const newItems = Array.from(cat.items);
        const [removed] = newItems.splice(startIndex, 1);
        newItems.splice(endIndex, 0, removed);
        const orderedItems = newItems.map((item, index) => ({ ...item, displayOrder: index }));
        return { ...cat, items: orderedItems };
      }
      return cat;
    });
      
    const updatedList = { ...packingList, categories: updatedCategories };
    setPackingList(updatedList);
    await persistList(updatedList);
  }, [packingList, persistList]);

  // Reset to default (fetched or empty) - or could re-fetch.
  // For now, this is a client-side reset to initial fetched state, or empty if fetch failed.
  // A true "reset to defaults" might involve calling persist with defaultCategoriesData mapped to DTOs.
  const resetPackingList = useCallback(() => {
    // This effectively re-runs the initial fetch logic
    // Or, if we want to reset to local defaults:
    // const defaultDtoCategories = defaultCategoriesData.map(mapDefaultCategoryToDto);
    // const listToPersist = { id: packingList?.id, categories: defaultDtoCategories };
    // setPackingList(listToPersist);
    // persistList(listToPersist);
    // For now, just re-trigger initial fetch:
    const fetchList = async () => {
        setIsLoading(true);
        setError(null);
        try {
          const response = await api.packingList.get();
          if (response.success && response.data) {
            setPackingList(response.data.id ? response.data : { id: undefined, categories: [] });
          } else {
            setError(response.message || 'Failed to fetch packing list.');
            setPackingList({ id: undefined, categories: [] });
          }
        } catch (e: any) {
          setError(e.message || 'An unexpected error occurred while fetching.');
          setPackingList({ id: undefined, categories: [] });
        } finally {
          setIsLoading(false);
        }
      };
      fetchList();
  }, [/* packingList?.id, persistList */]); // Dependencies might change based on strategy

  return {
    categories: getCategories(),
    packingListId: packingList?.id,
    addCategory,
    editCategoryTitle,
    deleteCategory,
    addItemToCategory,
    editItemText,
    deleteItemInCategory,
    handleToggleItem,
    reorderCategories,
    reorderItems,
    resetPackingList, // Renamed from resetCategories
    isLoading,
    error,
    lastSynced,
  };
}
