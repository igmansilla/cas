import React, { useState } from "react";
import {
  Plus,
  Pencil,
  Trash2,
  Check,
  X,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import { Draggable, Droppable } from "@hello-pangea/dnd";
import { PackingCategory } from "../types";

interface EditableListProps {
  category: PackingCategory;
  index: number;
  onUpdateCategory: (newTitle: string) => void;
  onDeleteCategory: () => void;
  onAddItem: (item: string) => void;
  onEditItem: (id: string, newText: string) => void;
  onDeleteItem: (id: string) => void;
}

export function EditableList({
  category,
  index,
  onUpdateCategory,
  onDeleteCategory,
  onAddItem,
  onEditItem,
  onDeleteItem,
}: EditableListProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [newTitle, setNewTitle] = useState(category.title);
  const [newItem, setNewItem] = useState("");
  const [editingItemIndex, setEditingItemIndex] = useState<string | null>(null);
  const [editingItemText, setEditingItemText] = useState("");
  const [isExpanded, setIsExpanded] = useState(true);

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

  const handleEditItem = (id: string) => {
    const itemToEdit = category.items.find((item) => item.id === id);
    if (itemToEdit) {
      setEditingItemIndex(id); // Guarda el ID del ítem que se está editando
      setEditingItemText(itemToEdit.text); // Establece el texto actual del ítem
    }
  };
  const handleSaveItem = (id: string) => {
    if (editingItemText.trim()) {
      onEditItem(id, editingItemText.trim());
      setEditingItemIndex(null);
      setEditingItemText("");
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
            <div className="flex-1" {...provided.dragHandleProps}>
              {isEditing ? (
                <form onSubmit={handleSubmitTitle} className="flex gap-2">
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
                <div className="flex items-center gap-4">
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
                  <h2 className="text-lg sm:text-xl font-semibold text-gray-800">
                    {category.title}
                  </h2>
                </div>
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
                    {category.items.map((item) => (
                      <Draggable
                        key={item.id}
                        draggableId={item.id}
                        index={category.items.findIndex(
                          (i) => i.id === item.id
                        )} // Si necesitas el índice para Draggable
                      >
                        {(provided) => (
                          <li
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            {...provided.dragHandleProps}
                            className="flex items-center justify-between p-3 bg-gray-50 rounded-lg group hover:bg-gray-100 transition-all duration-200"
                          >
                            {editingItemIndex === item.id ? (
                              <div className="flex-1 flex flex-col sm:flex-row gap-2">
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
                                    onClick={() => handleSaveItem(item.id)}
                                    className="flex-1 sm:flex-none p-2 text-white bg-secondary rounded-lg hover:bg-secondary-dark transition-colors"
                                  >
                                    <Check className="w-5 h-5" />
                                  </button>
                                  <button
                                    onClick={() => setEditingItemIndex(null)}
                                    className="flex-1 sm:flex-none p-2 text-gray-600 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
                                  >
                                    <X className="w-5 h-5" />
                                  </button>
                                </div>
                              </div>
                            ) : (
                              <>
                                <span className="text-sm sm:text-base text-gray-700">
                                  {item.text}
                                </span>
                                <div className="flex gap-2 opacity-100 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                                  <button
                                    onClick={() => handleEditItem(item.id)}
                                    className="p-2 text-gray-600 hover:text-primary transition-colors"
                                  >
                                    <Pencil className="w-4 h-4" />
                                  </button>
                                  <button
                                    onClick={() => onDeleteItem(item.id)}
                                    className="p-2 text-gray-600 hover:text-primary transition-colors"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </div>
                              </>
                            )}
                          </li>
                        )}
                      </Draggable>
                    ))}

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
