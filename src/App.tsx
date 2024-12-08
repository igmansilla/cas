import React from "react";
import { Trash2 } from "lucide-react";
import { CategoryList } from "./components/CategoryList";
import { packingCategories } from "./data/packingList";
import { useLocalStorage } from "./hooks/useLocalStorage";
import { CheckedItems } from "./types";
import { Logo } from "./components/Logo";
import { SyncStatus } from "./components/SyncStatus";

function App() {
  const [checkedItems, setCheckedItems, { lastSynced, error }] =
    useLocalStorage<CheckedItems>("packing-list-checked", {});

  const handleToggleItem = (itemId: string) => {
    setCheckedItems((prev) => ({
      ...prev,
      [itemId]: !prev[itemId],
    }));
  };

  const handleReset = () => {
    if (window.confirm("¿Estás seguro que querés reiniciar la lista?")) {
      setCheckedItems({});
    }
  };

  const totalItems = packingCategories.reduce(
    (sum, category) => sum + category.items.length,
    0
  );
  const checkedCount = Object.values(checkedItems).filter(Boolean).length;
  const totalProgress = (checkedCount / totalItems) * 100;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-primary text-white py-6 shadow-lg sticky top-0 z-10">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Logo />
              <h1 className="text-2xl font-bold">Lista de Equipamiento</h1>
            </div>
            <div className="flex items-center gap-4">
              <SyncStatus lastSynced={lastSynced} error={error} />
              <button
                onClick={handleReset}
                className="flex items-center gap-2 px-4 py-2 bg-primary-dark rounded-lg hover:bg-primary-dark/90 transition-colors text-sm sm:px-3 sm:py-1"
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

      <main className="container mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-md p-6 mb-6 border-l-4 border-primary">
          <p className="text-primary font-medium flex items-center gap-2">
            <span>⚠️</span>
            Importante: No llevar elementos de vidrio o plástico frágiles
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {packingCategories.map((category, index) => (
            <CategoryList
              key={index}
              category={category}
              checkedItems={checkedItems}
              onToggleItem={handleToggleItem}
            />
          ))}
        </div>
      </main>

      <footer className="bg-primary-dark text-white py-4 mt-8">
        <div className="container mx-auto px-4 text-center">
          <p>Lista de equipamiento recomendado para campamento</p>
        </div>
      </footer>
    </div>
  );
}

export default App;
