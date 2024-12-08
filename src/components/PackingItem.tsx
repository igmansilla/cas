import React from 'react';
import { Check } from 'lucide-react';

interface PackingItemProps {
  text: string;
  isChecked: boolean;
  onToggle: () => void;
}

export function PackingItem({ text, isChecked, onToggle }: PackingItemProps) {
  return (
    <li
      className="group flex items-center p-3 bg-white rounded-lg hover:bg-gray-50 transition-all duration-200 cursor-pointer border border-gray-100"
      onClick={onToggle}
    >
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
          text-gray-700 transition-all duration-300
          ${isChecked ? 'line-through text-gray-400' : ''}
        `}
      >
        {text}
      </span>
    </li>
  );
}