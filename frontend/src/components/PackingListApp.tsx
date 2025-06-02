import React from "react";
import { Trash2, Plus, Loader2 } from "lucide-react"; // Added Loader2
import { DragDropContext, Droppable, DropResult } from "@hello-pangea/dnd";
import { CategoryList } from "./CategoryList";
import { usePackingLists } from "../hooks/usePackingLists";
// Removed: import { useLocalStorage } from "../hooks/useLocalStorage";
// Removed: import { CheckedItems } from "../types"; // isChecked is now part of PackingListItemDto
import { Logo } from "./Logo";
import { SyncStatus } from "./SyncStatus";
import { PackingListCategoryDto } from "../types/api"; // Using DTO types

const PackingListApp: React.FC = () => {
  const {
    categories, // This is PackingListCategoryDto[]
    // packingListId, // Available if needed
    addCategory,
    editCategoryTitle, // Renamed in hook for clarity
    deleteCategory,
    addItemToCategory, // Renamed in hook
    editItemText,      // Renamed in hook
    deleteItemInCategory, // Renamed in hook
    handleToggleItem,     // New from hook
    reorderCategories,
    reorderItems,
    resetPackingList,     // Renamed in hook
    isLoading,
    error,
    lastSynced,
  } = usePackingLists();

  const handleReset = () => {
    if (window.confirm("¿Estás seguro que querés reiniciar la lista a los valores del servidor?")) {
      resetPackingList();
    }
  };

  const handleAddCategory = () => {
    const title = prompt("Nombre de la nueva categoría:", "Nueva Categoría");
    if (title && title.trim() !== "") {
      addCategory(title.trim());
    }
  };

  const handleDragEnd = (result: DropResult) => {
    if (!result.destination) return;

    const { source, destination, type, draggableId } = result;
    
    //draggableId is string "category-ID" or "item-CAT_ID-ITEM_ID"
    //source.droppableId is string "categories" or "items-CAT_ID"

    if (type === "category") {
      reorderCategories(source.index, destination.index);
    } else if (type === "item") { 
      // Type "item" indicates an item was dragged.
      // The droppableId of the source and destination lists will tell us the category.
      // e.g. source.droppableId = "items-123" where 123 is categoryId
      const sourceCategoryId = parseInt(source.droppableId.replace("items-", ""), 10);
      const destCategoryId = parseInt(destination.droppableId.replace("items-", ""), 10);

      if (isNaN(sourceCategoryId) || isNaN(destCategoryId)) {
        console.error("Could not parse category IDs from droppable IDs", source.droppableId, destination.droppableId);
        return;
      }
      
      // For simplicity, this example assumes reorderItems can handle cross-category or same-category
      // based on whether sourceCategoryId and destCategoryId are the same.
      // The current hook's reorderItems is for within a single category.
      // A true cross-category drag & drop would be more complex, involving removing from one list and adding to another.
      // Let's assume for now reorderItems is for within the same category.
      if (sourceCategoryId === destCategoryId) {
        reorderItems(sourceCategoryId, source.index, destination.index);
      } else {
        // Implement cross-category drag if necessary.
        // This would involve:
        // 1. Finding the item being dragged.
        // 2. Removing it from the source category's items list.
        // 3. Adding it to the destination category's items list at the destination index.
        // 4. Calling persistList with the updated packingList.
        console.warn("Cross-category item drag and drop not fully implemented in this simplified version. Item dropped back or handled as same category.");
        // For now, we can just ignore cross-category drops or attempt a simpler reorder within source
        // reorderItems(sourceCategoryId, source.index, destination.index); // This would be wrong if destination is different.
      }
    }
  };
  
  const totalItems = categories.reduce(
    (sum, category) => sum + category.items.length,
    0
  );
  const checkedCount = categories.reduce(
    (sum, category) =>
      sum + category.items.filter((item) => item.isChecked).length,
    0
  );
  const totalProgress = totalItems > 0 ? (checkedCount / totalItems) * 100 : 0;

  if (isLoading && !categories.length) { // Show loader only on initial load
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <Loader2 className="w-12 h-12 animate-spin text-primary" />
        <p className="ml-4 text-lg">Cargando tu lista...</p>
      </div>
    );
  }

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <div>
        <header className="bg-primary text-white py-4 shadow-lg sticky top-0 z-10">
          <div className="container mx-auto px-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Logo />
                <h1 className="text-lg sm:text-2xl font-bold">
                  Lista de Equipamiento
                </h1>
              </div>
              <div className="flex items-center gap-4">
                <SyncStatus lastSynced={lastSynced} error={error} isLoading={isLoading} />
                <button
                  onClick={handleReset}
                  className="flex items-center gap-2 px-4 py-2 bg-primary-dark rounded-lg hover:bg-primary-dark/90 transition-colors whitespace-nowrap"
                  disabled={isLoading}
                >
                  <Trash2 className="w-4 h-4" />
                  <span className="hidden sm:inline">Reiniciar</span>
                </button>
              </div>
            </div>

            <div className="mt-4">
              <div className="flex justify-between text-sm mb-1">
                <span>
                  {checkedCount} de {totalItems} items completados
                </span>
                <span>{Math.round(totalProgress)}%</span>
              </div>
              <div className="w-full h-2 bg-primary-dark rounded-full overflow-hidden">
                <div
                  className="h-full bg-secondary transition-all duration-500 ease-out"
                  style={{ width: `${totalProgress}%` }}
                />
              </div>
            </div>
          </div>
        </header>

        <main className="container mx-auto px-4 py-6 sm:py-8">
          {error && (
            <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6" role="alert">
              <p className="font-bold">Error</p>
              <p>{error}</p>
            </div>
          )}
          <div className="bg-white rounded-xl shadow-md p-4 sm:p-6 mb-6 border-l-4 border-primary">
            <p className="text-primary font-medium flex items-center gap-2 text-sm sm:text-base">
              <span>⚠️</span>
              Importante: No usar elementos de vidrio o plástico frágiles
            </p>
          </div>

          <Droppable droppableId="categories" type="category">
            {(provided) => (
              <div
                ref={provided.innerRef}
                {...provided.droppableProps}
                className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-8"
              >
                {categories.map((category: PackingListCategoryDto, index) => (
                  <CategoryList
                    key={category.id || `new-cat-${index}`} // Use index for new categories without ID yet
                    index={index}
                    category={category} // category is PackingListCategoryDto, items have isChecked
                    // checkedItems prop removed
                    onToggleItem={(itemId: number) => { // Assuming itemId is now number
                        if (category.id === undefined) {
                             console.error("Cannot toggle item in a category that has no ID yet."); return;
                        }
                        handleToggleItem(category.id, itemId);
                    }}
                    onUpdateCategoryTitle={(newTitle) => { // Renamed prop for clarity
                        if (category.id === undefined) {
                            console.error("Cannot update title of category that has no ID yet."); return;
                        }
                        editCategoryTitle(category.id, newTitle);
                    }}
                    onDeleteCategory={() => {
                        if (category.id === undefined) {
                            console.error("Cannot delete category that has no ID yet."); return;
                        }
                        deleteCategory(category.id);
                    }}
                    onAddItem={(itemText) => { // itemText is string
                        if (category.id === undefined) {
                            console.error("Cannot add item to category that has no ID yet."); return;
                        }
                        addItemToCategory(category.id, itemText);
                    }}
                    onEditItem={(itemId: number, newText: string) => { // itemId is number
                        if (category.id === undefined) {
                             console.error("Cannot edit item in a category that has no ID yet."); return;
                        }
                        editItemText(category.id, itemId, newText);
                    }}
                    onDeleteItem={(itemId: number) => { // itemId is number
                        if (category.id === undefined) {
                            console.error("Cannot delete item in a category that has no ID yet."); return;
                        }
                        deleteItemInCategory(category.id, itemId);
                    }}
                  />
                ))}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </main>

        <button
          onClick={handleAddCategory}
          className="fixed bottom-6 right-6 w-14 h-14 bg-primary text-white rounded-full shadow-lg hover:bg-primary-dark transition-colors flex items-center justify-center z-20"
          disabled={isLoading}
        >
          <Plus className="w-6 h-6" />
        </button>

        <footer className="bg-primary-dark text-white py-4 mt-8">
          <div className="container mx-auto px-4 text-center text-sm sm:text-base">
            <p>Lista de equipamiento recomendado para campamento</p>
          </div>
        </footer>
      </div>
    </DragDropContext>
  );
};

export default PackingListApp;
