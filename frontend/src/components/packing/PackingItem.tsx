import React from 'react';
import { Check, Pencil, Trash2 } from 'lucide-react';
import { PackingListItemDto } from '../../types/api'; // Import the DTO

interface PackingItemProps {
  item: PackingListItemDto; // Use the DTO
  // isChecked prop is removed, it's part of item
  onToggle: (itemId: number | undefined) => void; // Pass item ID (number or undefined if new)
  onEdit: (itemId: number | undefined) => void;   // Pass item ID
  onDelete: (itemId: number | undefined) => void; // Pass item ID
}

export function PackingItem({ item, onToggle, onEdit, onDelete }: PackingItemProps) {
  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit(item.id);
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete(item.id);
  };

  const handleToggle = () => {
    onToggle(item.id);
  };

  return (
    <li
      className="group flex items-center justify-between p-3 bg-white rounded-lg hover:bg-gray-50 transition-all duration-200 cursor-pointer border border-gray-100"
      onClick={handleToggle} // Use the new handler
    >
      <div className="flex items-center flex-1">
        <div
          className={`
            flex-shrink-0 w-5 h-5 border-2 rounded mr-3 flex items-center justify-center
            transition-all duration-300 ease-in-out
            ${item.isChecked // Use item.isChecked
              ? 'border-secondary bg-secondary'
              : 'border-gray-300 group-hover:border-secondary'}
          `}
        >
          {item.isChecked && <Check className="w-3 h-3 text-white" />}
        </div>
        <span
          className={`
            text-sm sm:text-base text-gray-700 transition-all duration-300
            ${item.isChecked ? 'line-through text-gray-400' : ''}
          `}
        >
          {item.text}
        </span>
      </div>
      <div className="flex gap-2 opacity-100 sm:opacity-0 group-hover:opacity-100 transition-opacity ml-4">
        <button
          onClick={handleEdit}
          className="p-2 text-gray-600 hover:text-primary transition-colors rounded-lg hover:bg-gray-100"
          title="Editar ítem"
          disabled={item.id === undefined} // Optionally disable for not-yet-persisted items
        >
          <Pencil className="w-4 h-4" />
        </button>
        <button
          onClick={handleDelete}
          className="p-2 text-gray-600 hover:text-primary transition-colors rounded-lg hover:bg-gray-100"
          title="Eliminar ítem"
          disabled={item.id === undefined} // Optionally disable for not-yet-persisted items
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </li>
  );
}