import { render, screen } from '@testing-library/react';
import PackingListApp from './PackingListApp';
import { describe, it, expect, vi } from 'vitest';

// Mock @hello-pangea/dnd
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Droppable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), droppableProps: {}, placeholder: null }, {})}</div>,
  Draggable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), draggableProps: {}, dragHandleProps: {} }, {})}</div>,
}));

// Mock useLocalStorage
vi.mock('../hooks/useLocalStorage', () => ({
  useLocalStorage: vi.fn((key, initialValue) => [initialValue, vi.fn(), { lastSynced: null, error: null }]),
}));

// Mock usePackingLists (as it likely uses useLocalStorage or has its own state)
vi.mock('../hooks/usePackingLists', () => ({
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


describe('PackingListApp', () => {
  it('renders without crashing and shows header', () => {
    render(<PackingListApp />);
    // Check for a distinctive element, e.g., the main header text
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument();
  });
});
