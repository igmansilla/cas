import { render, screen } from '@testing-library/react';
import App from './App'; // Correct path for App.test.tsx in src/
import { describe, it, expect, vi } from 'vitest';

// Mock @hello-pangea/dnd (needed by PackingListApp)
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Droppable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), droppableProps: {}, placeholder: null }, {})}</div>,
  Draggable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), draggableProps: {}, dragHandleProps: {} }, {})}</div>,
}));

// Mock useLocalStorage (needed by PackingListApp)
// Path is relative to where the hook is imported, typically PackingListApp.tsx
vi.mock('./hooks/useLocalStorage', () => ({
  useLocalStorage: vi.fn((key, initialValue) => [initialValue, vi.fn(), { lastSynced: null, error: null }]),
}));

// Mock usePackingLists (needed by PackingListApp)
// Path is relative to where the hook is imported, typically PackingListApp.tsx
vi.mock('./hooks/usePackingLists', () => ({
  usePackingLists: vi.fn(() => ({
    categories: [],
    addCategory: vi.fn(),
    editCategory: vi.fn(),
    deleteCategory: vi.fn(),
    addItem: vi.fn(),
    editItem: vi.fn(),
    deleteItem: vi.fn(),
    reorderCategories: vi.fn(),
    reorderItems: vi.fn(),
  })),
}));

describe('App', () => {
  it('renders MainLayout and PackingListApp content', () => {
    render(<App />);
    
    // Check for an element from MainLayout (e.g., a sidebar item)
    expect(screen.getByText('Packing List')).toBeInTheDocument(); // From MainLayout's sidebar
    
    // Check for an element from PackingListApp (e.g., its main header)
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument(); // From PackingListApp
  });
});
