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
import { PackingListCategoryDto, PackingListItemDto } from "../types/api"; // Import DTOs
import { PackingItem } from "./PackingItem";

interface CategoryListProps {
  category: PackingListCategoryDto;
  // checkedItems prop removed
  onToggleItem: (itemId: number | undefined) => void; // itemId is number or undefined
  index: number; // For category drag and drop
  onUpdateCategoryTitle: (newTitle: string) => void; // Renamed for clarity from PackingListApp
  onDeleteCategory: () => void;
  onAddItem: (itemText: string) => void; // itemText is string
  onEditItem: (itemId: number | undefined, newText: string) => void;
  onDeleteItem: (itemId: number | undefined) => void;
}

export function CategoryList({
  category,
  onToggleItem,
  index,
  onUpdateCategoryTitle,
  onDeleteCategory,
  onAddItem,
  onEditItem,
  onDeleteItem,
}: CategoryListProps) {
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [newTitle, setNewTitle] = useState(category.title);
  const [newItemText, setNewItemText] = useState("");
  const [editingItemId, setEditingItemId] = useState<number | undefined | null>(null); // Store item ID (number or undefined)
  const [editingItemCurrentText, setEditingItemCurrentText] = useState("");
  const [isExpanded, setIsExpanded] = useState(true);

  // Progress calculation based on item.isChecked
  const checkedItemCount = category.items.filter(item => item.isChecked).length;
  const progressPercentage = category.items.length > 0
    ? (checkedItemCount / category.items.length) * 100
    : 0;

  const handleSubmitTitle = (e: React.FormEvent) => {
    e.preventDefault();
    if (newTitle.trim()) {
      onUpdateCategoryTitle(newTitle.trim());
      setIsEditingTitle(false);
    }
  };

  const handleAddItem = (e: React.FormEvent) => {
    e.preventDefault();
    if (newItemText.trim()) {
      onAddItem(newItemText.trim());
      setNewItemText("");
    }
  };

  const handleStartEditItem = (item: PackingListItemDto) => {
    setEditingItemId(item.id);
    setEditingItemCurrentText(item.text);
  };

  const handleSaveItem = (itemId: number | undefined) => {
    if (editingItemCurrentText.trim()) {
      onEditItem(itemId, editingItemCurrentText.trim());
      setEditingItemId(null);
      setEditingItemCurrentText("");
    }
  };
  
  // DND IDs: Use category.id if available, otherwise a temporary ID for new categories.
  // For items, use item.id if available. New items might not be draggable until persisted,
  // or require a temporary client-side unique ID if dragging new items is needed.
  // For simplicity, if category.id is undefined, dragging might be problematic or disabled by parent.
  // Here, we'll assume category.id is defined for persisted categories to be draggable.
  // If category.id is undefined, we use `new-category-${index}` as a fallback draggableId.
  const categoryDraggableId = category.id !== undefined ? `category-${category.id}` : `new-category-${index}`;
  const itemsDroppableId = category.id !== undefined ? `items-${category.id}` : `items-new-category-${index}`;


  return (
    <Draggable draggableId={categoryDraggableId} index={index}>
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
              {isEditingTitle ? (
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
                      setIsEditingTitle(false);
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
            {!isEditingTitle && (
              <div className="flex gap-2">
                <button
                  onClick={() => setIsEditingTitle(true)}
                  className="p-2 text-gray-600 hover:text-primary transition-colors"
                  title="Editar categoría"
                  disabled={category.id === undefined} // Disable if category not persisted
                >
                  <Pencil className="w-5 h-5" />
                </button>
                <button
                  onClick={onDeleteCategory}
                  className="p-2 text-gray-600 hover:text-primary transition-colors"
                  title="Eliminar categoría"
                  disabled={category.id === undefined} // Disable if category not persisted
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
                    {checkedItemCount} de {category.items.length} items
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
                  value={newItemText}
                  onChange={(e) => setNewItemText(e.target.value)}
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

              <Droppable droppableId={itemsDroppableId} type="item"> {/* Consistent type="item" */}
                {(providedDroppable) => (
                  <ul
                    ref={providedDroppable.innerRef}
                    {...providedDroppable.droppableProps}
                    className="space-y-2"
                  >
                    {category.items.map((item, itemIndex) => {
                      // For items that are new (item.id is undefined), use a temporary draggableId.
                      // Persisted items use `item-${item.id}`.
                      const itemDraggableId = item.id !== undefined ? `item-${category.id}-${item.id}` : `new-item-${category.id}-${itemIndex}`;
                      return (
                        <Draggable
                          key={itemDraggableId} // key must be unique
                          draggableId={itemDraggableId}
                          index={itemIndex}
                          isDragDisabled={item.id === undefined} // Optionally disable dragging for new, unpersisted items
                        >
                          {(providedDraggable) => (
                            <div
                              ref={providedDraggable.innerRef}
                              {...providedDraggable.draggableProps}
                              {...providedDraggable.dragHandleProps}
                            >
                              {editingItemId === item.id ? (
                                <div className="flex flex-col sm:flex-row gap-2 p-3 bg-gray-50 rounded-lg">
                                  <input
                                    type="text"
                                    value={editingItemCurrentText}
                                    onChange={(e) =>
                                      setEditingItemCurrentText(e.target.value)
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
                                      onClick={() => setEditingItemId(null)}
                                      className="flex-1 sm:flex-none p-2 text-gray-600 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
                                    >
                                      <X className="w-5 h-5" />
                                    </button>
                                  </div>
                                </div>
                              ) : (
                                <PackingItem
                                  item={item} // Pass the whole item DTO
                                  onToggle={onToggleItem}
                                  onEdit={() => handleStartEditItem(item)}
                                  onDelete={onDeleteItem}
                                />
                              )}
                            </div>
                          )}
                        </Draggable>
                      );
                    })}
                    {providedDroppable.placeholder}
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
