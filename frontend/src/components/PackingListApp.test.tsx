import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import PackingListApp from './PackingListApp';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { PackingListDto, PackingListCategoryDto, PackingListItemDto } from '../types/api';
import { ApiError } from '../services/api'; // Assuming ApiError is exported

// Mock @hello-pangea/dnd
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: { children: React.ReactNode }) => <div data-testid="dnd-context">{children}</div>,
  Droppable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div data-testid="dnd-droppable">{children({ innerRef: vi.fn(), droppableProps: {}, placeholder: null }, {})}</div>,
  Draggable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div data-testid="dnd-draggable">{children({ innerRef: vi.fn(), draggableProps: {}, dragHandleProps: {} }, {})}</div>,
}));

// Mock the API service
const mockGetPackingList = vi.fn();
const mockSavePackingList = vi.fn();

vi.mock('../services/api', () => ({
  default: { // if api.ts exports 'api' as default
    packingList: {
      get: mockGetPackingList,
      save: mockSavePackingList,
    },
  },
  api: { // if api.ts exports 'api' as a named export
    packingList: {
      get: mockGetPackingList,
      save: mockSavePackingList,
    },
  },
  ApiError: class extends Error { // Mock ApiError if it's a class used for errors
    status: number;
    constructor(status: number, message: string) {
      super(message);
      this.name = 'ApiError';
      this.status = status;
    }
  }
}));


// Mock child components to simplify tests and focus on PackingListApp logic
vi.mock('./Logo', () => ({ Logo: () => <div data-testid="logo">Logo</div> }));
vi.mock('./SyncStatus', () => ({ SyncStatus: ({ isLoading, error, lastSynced }: any) => (
    <div data-testid="sync-status">
        {isLoading && <span>Loading...</span>}
        {error && <span>Error: {error}</span>}
        {lastSynced && <span>Synced: {new Date(lastSynced).toLocaleTimeString()}</span>}
    </div>
)}));

// Mock CategoryList and PackingItem to avoid deep rendering issues and focus on PackingListApp
vi.mock('./CategoryList', () => ({
    CategoryList: ({ category, onAddItem, onToggleItem, onUpdateCategoryTitle, onDeleteCategory, onEditItem, onDeleteItem }: any) => (
        <div data-testid={`category-${category.id || category.title}`}>
            <h2 data-testid={`category-title-${category.id || category.title}`}>{category.title}</h2>
            <button onClick={() => onAddItem('New Mock Item Text')}>Add Item to {category.title}</button>
            <button onClick={() => onUpdateCategoryTitle('Updated Title for ' + category.title)}>Update Title</button>
            <button onClick={() => onDeleteCategory()}>Delete Category</button>
            {category.items.map((item: PackingListItemDto) => (
                <div key={item.id || item.text} data-testid={`item-${item.id || item.text}`}>
                    <span>{item.text}</span>
                    <input type="checkbox" checked={item.isChecked} onChange={() => onToggleItem(item.id)} />
                    <button onClick={() => onEditItem(item.id, 'Updated item text')}>Edit</button>
                    <button onClick={() => onDeleteItem(item.id)}>Delete</button>
                </div>
            ))}
        </div>
    )
}));


const createMockItem = (id: number, text: string, isChecked: boolean, displayOrder: number): PackingListItemDto => ({
  id, text, isChecked, displayOrder
});

const createMockCategory = (id: number, title: string, items: PackingListItemDto[], displayOrder: number): PackingListCategoryDto => ({
  id, title, items, displayOrder
});

const mockInitialList: PackingListDto = {
  id: 1,
  categories: [
    createMockCategory(10, "Ropa", [createMockItem(101, "Mochila", false, 0)], 0),
    createMockCategory(20, "Documentos", [createMockItem(201, "DNI", true, 0)], 1),
  ],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

const mockEmptyList: PackingListDto = {
    id: undefined, // Or null, depending on backend for new user
    categories: [],
    createdAt: undefined,
    updatedAt: undefined,
};


describe('PackingListApp', ()_ => {
  beforeEach(() => {
    mockGetPackingList.mockClear();
    mockSavePackingList.mockClear();
  });

  it('renders loading state initially, then fetches and displays data', async ()_ => {
    mockGetPackingList.mockResolvedValueOnce({ success: true, data: mockInitialList });
    render(<PackingListApp />);

    expect(screen.getByText(/Cargando tu lista.../i)).toBeInTheDocument();
    
    await waitFor(() => expect(mockGetPackingList).toHaveBeenCalledTimes(1));
    
    await waitFor(() => {
      expect(screen.queryByText(/Cargando tu lista.../i)).not.toBeInTheDocument();
      expect(screen.getByText('Ropa')).toBeInTheDocument();
      expect(screen.getByText('Mochila')).toBeInTheDocument();
      expect(screen.getByText('Documentos')).toBeInTheDocument();
      expect(screen.getByText('DNI')).toBeInTheDocument();
    });
    expect(screen.getByTestId("sync-status")).toHaveTextContent(/Synced/);
  });

  it('handles empty list for a new user', async ()_ => {
    mockGetPackingList.mockResolvedValueOnce({ success: true, data: mockEmptyList });
    render(<PackingListApp />);
    
    expect(screen.getByText(/Cargando tu lista.../i)).toBeInTheDocument();
    await waitFor(() => expect(mockGetPackingList).toHaveBeenCalledTimes(1));
    
    await waitFor(() => {
      expect(screen.queryByText(/Cargando tu lista.../i)).not.toBeInTheDocument();
      // Check that no categories are rendered, or an empty state message if one exists
      expect(screen.queryByText('Ropa')).not.toBeInTheDocument();
    });
     expect(screen.getByTestId("sync-status")).not.toHaveTextContent(/Error/);
  });

  it('displays an error message if initial fetch fails', async ()_ => {
    mockGetPackingList.mockRejectedValueOnce(new ApiError(500, "Server Fetch Error"));
    render(<PackingListApp />);

    await waitFor(() => expect(mockGetPackingList).toHaveBeenCalledTimes(1));
    
    await waitFor(()_ => {
      expect(screen.getByText(/Error/i)).toBeInTheDocument(); // From SyncStatus mock
      expect(screen.getByTestId("sync-status")).toHaveTextContent("Server Fetch Error");
    });
  });

  it('allows adding an item and calls save API', async ()_ => {
    mockGetPackingList.mockResolvedValueOnce({ success: true, data: JSON.parse(JSON.stringify(mockInitialList)) }); // Deep copy
    
    const listAfterAddItem: PackingListDto = {
        ...mockInitialList,
        categories: mockInitialList.categories.map(c => 
            c.id === 10 ? { ...c, items: [...c.items, createMockItem(undefined as any, "New Mock Item Text", false, c.items.length)]} : c
        ),
        updatedAt: new Date().toISOString() 
    };
    // The save mock should return the list with the new item having an ID
    const listAfterSaveWithNewId: PackingListDto = {
        ...listAfterAddItem,
        categories: listAfterAddItem.categories.map(c => 
            c.id === 10 ? { ...c, items: c.items.map(i => i.text === "New Mock Item Text" ? {...i, id: 102 } : i) } : c
        )
    };
    mockSavePackingList.mockResolvedValueOnce({ success: true, data: listAfterSaveWithNewId });

    render(<PackingListApp />);
    await waitFor(() => expect(screen.getByText('Ropa')).toBeInTheDocument());

    // Simulate adding an item to the "Ropa" category (ID 10)
    // Using the mocked CategoryList's "Add Item" button
    fireEvent.click(screen.getByText('Add Item to Ropa'));
    
    await waitFor(() => expect(mockSavePackingList).toHaveBeenCalledTimes(1));
    
    // Check payload of save (optimistic update means item might not have ID yet)
    const savedCallArg = mockSavePackingList.mock.calls[0][0] as PackingListDto;
    expect(savedCallArg.categories.find(c=>c.id===10)?.items.some(i=>i.text === "New Mock Item Text" && i.id === undefined)).toBe(true);

    // Check if the new item (now with ID 102 from mock response) is rendered
    await waitFor(() => {
        expect(screen.getByTestId('item-102')).toBeInTheDocument();
        expect(screen.getByTestId('item-102')).toHaveTextContent("New Mock Item Text");
    });
    expect(screen.getByTestId("sync-status")).toHaveTextContent(/Synced/);
  });

  it('allows toggling an item and calls save API', async ()_ => {
    mockGetPackingList.mockResolvedValueOnce({ success: true, data: JSON.parse(JSON.stringify(mockInitialList)) });
    
    const listAfterToggle: PackingListDto = {
        ...mockInitialList,
        categories: mockInitialList.categories.map(c => 
            c.id === 10 ? { ...c, items: c.items.map(i => i.id === 101 ? {...i, isChecked: !i.isChecked } : i) } : c
        ),
         updatedAt: new Date().toISOString() 
    };
    mockSavePackingList.mockResolvedValueOnce({ success: true, data: listAfterToggle });

    render(<PackingListApp />);
    await waitFor(() => expect(screen.getByText('Mochila')).toBeInTheDocument());

    const mochilaCheckbox = screen.getByTestId('item-101').querySelector('input[type="checkbox"]')!;
    expect(mochilaCheckbox).not.toBeChecked();

    fireEvent.click(mochilaCheckbox); // Toggle "Mochila" (ID 101)

    await waitFor(() => expect(mockSavePackingList).toHaveBeenCalledTimes(1));
    
    const savedCallArg = mockSavePackingList.mock.calls[0][0] as PackingListDto;
    expect(savedCallArg.categories.find(c=>c.id===10)?.items.find(i=>i.id===101)?.isChecked).toBe(true);
    
    await waitFor(() => expect(mochilaCheckbox).toBeChecked());
    expect(screen.getByTestId("sync-status")).toHaveTextContent(/Synced/);
  });

  it('displays an error if save API fails', async ()_ => {
    mockGetPackingList.mockResolvedValueOnce({ success: true, data: JSON.parse(JSON.stringify(mockInitialList)) });
    mockSavePackingList.mockRejectedValueOnce(new ApiError(500, "Server Save Error"));

    render(<PackingListApp />);
    await waitFor(() => expect(screen.getByText('Ropa')).toBeInTheDocument());

    // Simulate adding an item to trigger save
    fireEvent.click(screen.getByText('Add Item to Ropa'));

    await waitFor(() => expect(mockSavePackingList).toHaveBeenCalledTimes(1));
    
    await waitFor(() => {
      expect(screen.getByTestId("sync-status")).toHaveTextContent("Server Save Error");
    });
  });
});
