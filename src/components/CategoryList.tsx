import React from 'react';
import { ListChecks } from 'lucide-react';
import { PackingCategory } from '../types';
import { PackingItem } from './PackingItem';

interface CategoryListProps {
  category: PackingCategory;
  checkedItems: { [key: string]: boolean };
  onToggleItem: (itemId: string) => void;
}

export function CategoryList({ category, checkedItems, onToggleItem }: CategoryListProps) {
  const getItemId = (categoryTitle: string, itemText: string) => 
    `${categoryTitle}-${itemText}`;

  const progress = category.items.reduce((count, item) => {
    const itemId = getItemId(category.title, item);
    return checkedItems[itemId] ? count + 1 : count;
  }, 0);

  const progressPercentage = (progress / category.items.length) * 100;

  return (
    <div className="mb-8 bg-white rounded-xl p-6 shadow-md border border-gray-100">
      <div className="flex items-center gap-2 mb-4">
        <ListChecks className="w-6 h-6 text-primary" />
        <h2 className="text-xl font-semibold text-gray-800">{category.title}</h2>
      </div>

      <div className="mb-4">
        <div className="flex justify-between text-sm text-gray-600 mb-1">
          <span>{progress} de {category.items.length} items</span>
          <span>{Math.round(progressPercentage)}%</span>
        </div>
        <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
          <div 
            className="h-full bg-secondary transition-all duration-500 ease-out"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      <ul className="space-y-2">
        {category.items.map((item) => {
          const itemId = getItemId(category.title, item);
          return (
            <PackingItem
              key={itemId}
              text={item}
              isChecked={checkedItems[itemId] || false}
              onToggle={() => onToggleItem(itemId)}
            />
          );
        })}
      </ul>
    </div>
  );
}