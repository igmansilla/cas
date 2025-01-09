import React from "react";
import { Trash2, Plus } from "lucide-react";
import { DragDropContext, Droppable, DropResult } from "@hello-pangea/dnd";
import { CategoryList } from "./components/CategoryList";
import { usePackingLists } from "./hooks/usePackingLists";
import { useLocalStorage } from "./hooks/useLocalStorage";
import { CheckedItems } from "./types";
import { Logo } from "./components/Logo";
import { SyncStatus } from "./components/SyncStatus";

function App() {
  const [checkedItems, setCheckedItems, { lastSynced, error }] =
    useLocalStorage<CheckedItems>("packing-list-checked", {});
  const {
    categories,
    addCategory,
    editCategory,
    deleteCategory,
    addItem,
    editItem,
    deleteItem,
    reorderCategories,
    reorderItems,
    resetCategories
  } = usePackingLists();

  const handleToggleItem = (itemId: string) => {
    setCheckedItems((prev) => ({
      ...prev,
      [itemId]: !prev[itemId],
    }));
  };

  const handleReset = () => {
    if (window.confirm("¿Estás seguro que querés reiniciar la lista?")) {
      setCheckedItems({}); // Limpia los checkeds
      resetCategories(); // Restaura las categorías a las predeterminadas
    }
  };
  

  const handleAddCategory = () => {
    const title = 'Nueva Categoría';
    addCategory({
      id: `${Date.now()}`, // Genera un ID único
      title,
      items: [] as { id: string; text: string }[], // Especifica el tipo del array
    });
  };
  

  const handleDragEnd = (result: DropResult) => {
    if (!result.destination) return;

    const { source, destination, type } = result;

    if (type === "category") {
      reorderCategories(source.index, destination.index);
    } else {
      const categoryId = result.type;
      const category = categories.find(
        (cat) => `items-${cat.title}` === categoryId
      );

      if (category) {
        const categoryIndex = categories.findIndex(
          (cat) => cat.title === category.title
        );
        reorderItems(categoryIndex, source.index, destination.index);
      }
    }
  };

  const totalItems = categories.reduce((sum, category) => sum + category.items.length, 0);
  const checkedCount = Object.keys(checkedItems).filter((key) => checkedItems[key]).length;
  const totalProgress = totalItems > 0 ? (checkedCount / totalItems) * 100 : 0;
  

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <div className="min-h-screen bg-gray-50">
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
                <SyncStatus lastSynced={lastSynced} error={error} />
                <button
                  onClick={handleReset}
                  className="flex items-center gap-2 px-4 py-2 bg-primary-dark rounded-lg hover:bg-primary-dark/90 transition-colors whitespace-nowrap"
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
                {categories.map((category, index) => (
                  <CategoryList
                    key={category.id}
                    index={index}
                    category={category}
                    checkedItems={checkedItems}
                    onToggleItem={handleToggleItem}
                    onUpdateCategory={(newTitle) =>
                      editCategory(category.id, newTitle)
                    }
                    onDeleteCategory={() => deleteCategory(category.id)}
                    onAddItem={(item) => addItem(category.id, item)}
                    onEditItem={(itemId, newText) =>
                      editItem(category.id, itemId, newText)
                    }
                    onDeleteItem={(itemId) => deleteItem(category.id, itemId)}
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
}

export default App;
