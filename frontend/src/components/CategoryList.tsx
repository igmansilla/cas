import React, { useState } from "react";
import {
  ListChecks,
  Pencil,
  Plus,
  Trash2,
  Check,
  X,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import { Draggable, Droppable } from "@hello-pangea/dnd";
import { PackingCategory } from "../types";
import { PackingItem } from "./PackingItem";

interface CategoryListProps {
  category: PackingCategory;
  checkedItems: { [key: string]: boolean };
  onToggleItem: (itemId: string) => void;
  index: number;
  onUpdateCategory: (newTitle: string) => void;
  onDeleteCategory: () => void;
  onAddItem: (item: string) => void;
  onEditItem: (itemId: string, newText: string) => void; // Cambiado a itemId
  onDeleteItem: (itemId: string) => void; // Cambiado a itemId
}

export function CategoryList({
  category,
  checkedItems,
  onToggleItem,
  index,
  onUpdateCategory,
  onDeleteCategory,
  onAddItem,
  onEditItem,
  onDeleteItem,
}: CategoryListProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [newTitle, setNewTitle] = useState(category.title);
  const [newItem, setNewItem] = useState("");
  const [editingItemIndex, setEditingItemIndex] = useState<string | null>(null);
  const [editingItemText, setEditingItemText] = useState("");
  const [isExpanded, setIsExpanded] = useState(true);

  const getItemId = (
    categoryTitle: string,
    item: { id: string; text: string }
  ) => `${categoryTitle}-${item.id}`;

  const progress = category.items.reduce((count, item) => {
    const itemId = getItemId(category.title, item); // Usa el id directamente
    return checkedItems[itemId] ? count + 1 : count;
  }, 0);

  const progressPercentage = (progress / category.items.length) * 100;

  const handleSubmitTitle = (e: React.FormEvent) => {
    e.preventDefault();
    if (newTitle.trim()) {
      onUpdateCategory(newTitle.trim());
      setIsEditing(false);
    }
  };

  const handleAddItem = (e: React.FormEvent) => {
    e.preventDefault();
    if (newItem.trim()) {
      onAddItem(newItem.trim());
      setNewItem("");
    }
  };

  const handleEditItem = (itemId: string) => {
    const itemToEdit = category.items.find((item) => item.id === itemId);
    if (itemToEdit) {
      setEditingItemIndex(itemId); // Cambia para almacenar el ID en lugar del índice
      setEditingItemText(itemToEdit.text);
    }
  };

  const handleSaveItem = (itemId: string) => {
    if (editingItemText.trim()) {
      onEditItem(itemId, editingItemText.trim());
      setEditingItemIndex(null); // Restablece el estado después de guardar
      setEditingItemText(""); // Limpia el texto
    }
  };

  return (
    <Draggable draggableId={category.title} index={index}>
      {(provided) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          className="bg-white rounded-xl p-4 sm:p-6 shadow-md border border-gray-100"
        >
          <div className="flex items-center justify-between mb-4">
            <div
              className="flex-1 flex items-center gap-2"
              {...provided.dragHandleProps}
            >
              {isEditing ? (
                <form
                  onSubmit={handleSubmitTitle}
                  className="flex-1 flex gap-2"
                >
                  <input
                    type="text"
                    value={newTitle}
                    onChange={(e) => setNewTitle(e.target.value)}
                    className="flex-1 px-3 py-2 text-sm sm:text-base border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50"
                    placeholder="Nombre de la categoría"
                    autoFocus
                  />
                  <button
                    type="submit"
                    className="p-2 text-white bg-secondary rounded-lg hover:bg-secondary-dark transition-colors"
                  >
                    <Check className="w-5 h-5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsEditing(false);
                      setNewTitle(category.title);
                    }}
                    className="p-2 text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </form>
              ) : (
                <>
                  <button
                    onClick={() => setIsExpanded(!isExpanded)}
                    className="text-gray-600 hover:text-primary transition-colors"
                  >
                    {isExpanded ? (
                      <ChevronDown className="w-5 h-5" />
                    ) : (
                      <ChevronUp className="w-5 h-5" />
                    )}
                  </button>
                  <ListChecks className="w-5 h-5 sm:w-6 sm:h-6 text-primary" />
                  <h2 className="text-lg sm:text-xl font-semibold text-gray-800">
                    {category.title}
                  </h2>
                </>
              )}
            </div>
            {!isEditing && (
              <div className="flex gap-2">
                <button
                  onClick={() => setIsEditing(true)}
                  className="p-2 text-gray-600 hover:text-primary transition-colors"
                  title="Editar categoría"
                >
                  <Pencil className="w-5 h-5" />
                </button>
                <button
                  onClick={onDeleteCategory}
                  className="p-2 text-gray-600 hover:text-primary transition-colors"
                  title="Eliminar categoría"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            )}
          </div>

          {isExpanded && (
            <>
              <div className="mb-4">
                <div className="flex justify-between text-sm text-gray-600 mb-1">
                  <span>
                    {progress} de {category.items.length} items
                  </span>
                  <span>{Math.round(progressPercentage)}%</span>
                </div>
                <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-secondary transition-all duration-500 ease-out"
                    style={{ width: `${progressPercentage}%` }}
                  />
                </div>
              </div>

              <form
                onSubmit={handleAddItem}
                className="mb-4 flex flex-col sm:flex-row gap-2"
              >
                <input
                  type="text"
                  value={newItem}
                  onChange={(e) => setNewItem(e.target.value)}
                  className="flex-1 px-4 py-2 text-sm sm:text-base border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50"
                  placeholder="Agregar nuevo ítem..."
                />
                <button
                  type="submit"
                  className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors flex items-center justify-center gap-2 whitespace-nowrap"
                >
                  <Plus className="w-5 h-5" />
                  <span>Agregar</span>
                </button>
              </form>

              <Droppable
                droppableId={`items-${category.title}`}
                type={`items-${category.title}`}
              >
                {(provided) => (
                  <ul
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className="space-y-2"
                  >
                    {category.items.map((item) => {
                      const itemId = getItemId(category.title, item);
                      return (
                        <Draggable
                          key={itemId}
                          draggableId={itemId}
                          index={category.items.findIndex(
                            (i) => i.id === item.id
                          )}
                        >
                          {(provided) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                            >
                              {editingItemIndex === item.id ? ( // Compara el id, no el índice
                                <div className="flex flex-col sm:flex-row gap-2 p-3 bg-gray-50 rounded-lg">
                                  <input
                                    type="text"
                                    value={editingItemText}
                                    onChange={(e) =>
                                      setEditingItemText(e.target.value)
                                    }
                                    className="flex-1 px-3 py-2 text-sm sm:text-base border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50"
                                    autoFocus
                                  />
                                  <div className="flex gap-2">
                                    <button
                                      onClick={() => handleSaveItem(item.id)} // Usa el id aquí
                                      className="flex-1 sm:flex-none p-2 text-white bg-secondary rounded-lg hover:bg-secondary-dark transition-colors"
                                    >
                                      <Check className="w-5 h-5" />
                                    </button>
                                    <button
                                      onClick={() => setEditingItemIndex(null)} // Cancela la edición
                                      className="flex-1 sm:flex-none p-2 text-gray-600 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
                                    >
                                      <X className="w-5 h-5" />
                                    </button>
                                  </div>
                                </div>
                              ) : (
                                <div className="group">
                                  <PackingItem
                                    text={item.text}
                                    isChecked={checkedItems[itemId] || false}
                                    onToggle={() => onToggleItem(itemId)}
                                    onEdit={() => handleEditItem(item.id)} // Usa el id aquí
                                    onDelete={() => onDeleteItem(item.id)} // Usa el id aquí
                                  />
                                </div>
                              )}
                            </div>
                          )}
                        </Draggable>
                      );
                    })}

                    {provided.placeholder}
                  </ul>
                )}
              </Droppable>
            </>
          )}
        </div>
      )}
    </Draggable>
  );
}
