import React from 'react';
import { Check, Pencil, Trash2 } from 'lucide-react';

interface PackingItemProps {
  text: string;
  isChecked: boolean;
  onToggle: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

export function PackingItem({ text, isChecked, onToggle, onEdit, onDelete }: PackingItemProps) {
  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit();
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete();
  };

  return (
    <li
      className="group flex items-center justify-between p-3 bg-white rounded-lg hover:bg-gray-50 transition-all duration-200 cursor-pointer border border-gray-100"
      onClick={onToggle}
    >
      <div className="flex items-center flex-1">
        <div
          className={`
            flex-shrink-0 w-5 h-5 border-2 rounded mr-3 flex items-center justify-center
            transition-all duration-300 ease-in-out
            ${isChecked 
              ? 'border-secondary bg-secondary' 
              : 'border-gray-300 group-hover:border-secondary'}
          `}
        >
          {isChecked && <Check className="w-3 h-3 text-white" />}
        </div>
        <span
          className={`
            text-sm sm:text-base text-gray-700 transition-all duration-300
            ${isChecked ? 'line-through text-gray-400' : ''}
          `}
        >
          {text}
        </span>
      </div>
      <div className="flex gap-2 opacity-100 sm:opacity-0 group-hover:opacity-100 transition-opacity ml-4">
        <button
          onClick={handleEdit}
          className="p-2 text-gray-600 hover:text-primary transition-colors rounded-lg hover:bg-gray-100"
        >
          <Pencil className="w-4 h-4" />
        </button>
        <button
          onClick={handleDelete}
          className="p-2 text-gray-600 hover:text-primary transition-colors rounded-lg hover:bg-gray-100"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </li>
  );
}