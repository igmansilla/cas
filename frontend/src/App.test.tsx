import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';
import { describe, it, expect, vi } from 'vitest';

// Mock @hello-pangea/dnd (needed by PackingListApp)
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Droppable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), droppableProps: {}, placeholder: null }, {})}</div>,
  Draggable: ({ children }: { children: (provided: any, snapshot: any) => React.ReactNode }) => <div>{children({ innerRef: vi.fn(), draggableProps: {}, dragHandleProps: {} }, {})}</div>,
}));

// Mock useLocalStorage (needed by PackingListApp)
vi.mock('./hooks/useLocalStorage', () => ({
  useLocalStorage: vi.fn((_key, initialValue) => [initialValue, vi.fn(), { lastSynced: null, error: null }]),
}));

// Mock usePackingLists (needed by PackingListApp)
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

// Mock lucide-react icons (needed by MainLayout)
vi.mock('lucide-react', async (importOriginal) => {
  const originalModule = await importOriginal();
  return {
    ...(originalModule as object),
    Menu: () => <div data-testid="menu-icon">Menu</div>,
    X: () => <div data-testid="x-icon">X</div>,
  };
});

// Mock localStorage
const mockLocalStorage = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();
Object.defineProperty(window, 'localStorage', { value: mockLocalStorage });

describe('App Routing and Integration', () => {
  beforeEach(() => {
    // Set up a fake user in localStorage for authenticated tests
    mockLocalStorage.setItem('user', JSON.stringify({ username: 'testuser', roles: ['ROLE_USER'] }));
  });

  afterEach(() => {
    mockLocalStorage.clear();
  });

  const renderAppWithRouter = (initialEntries = ['/']) => {
    return render(
      <MemoryRouter initialEntries={initialEntries}>
        <App />
      </MemoryRouter>
    );
  };

  it('redirects from / to /equipo and renders PackingListApp with MainLayout', () => {
    renderAppWithRouter(['/']);
    // Check for content from PackingListApp
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument(); // Header from PackingListApp
    // Check for content from MainLayout (Spanish localized)
    expect(screen.getByRole('link', { name: /Lista de Equipo/i })).toBeInTheDocument(); // NavLink from MainLayout
    expect(screen.getByText('CASAPP')).toBeInTheDocument(); // Mobile header title from MainLayout
  });

  it('renders PackingListApp for /equipo route', () => {
    renderAppWithRouter(['/equipo']);
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Lista de Equipo/i })).toBeInTheDocument();
  });

  it('renders GestionAcampantesPage for /acampantes route', () => {
    renderAppWithRouter(['/acampantes']);
    expect(screen.getByText('Página de Gestión de Acampantes')).toBeInTheDocument();
    // MainLayout should still be there
    expect(screen.getByRole('link', { name: /Gestión de Acampantes/i })).toBeInTheDocument();
  });
  it('renders CronogramaEventosPage for /eventos route', () => {
    renderAppWithRouter(['/eventos']);
    expect(screen.getByText('Página de Cronograma de Eventos')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Cronograma de Eventos/i })).toBeInTheDocument();
  });

  it('navigates to different pages when links in MainLayout are clicked', () => {
    renderAppWithRouter(['/equipo']); // Start at /equipo
    
    // Check initial page
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument();

    // Click on "Gestión de Acampantes" link
    const acampantesLink = screen.getByRole('link', { name: /Gestión de Acampantes/i });
    fireEvent.click(acampantesLink);
    
    // Check if GestionAcampantesPage content is rendered
    expect(screen.getByText('Página de Gestión de Acampantes')).toBeInTheDocument();
    // Check that PackingListApp content is gone
    expect(screen.queryByText('Lista de Equipamiento')).not.toBeInTheDocument();     // Click on "Cronograma de Eventos" link
    const eventosLink = screen.getByRole('link', { name: /Cronograma de Eventos/i });
    fireEvent.click(eventosLink);
    expect(screen.getByText('Página de Cronograma de Eventos')).toBeInTheDocument();
    expect(screen.queryByText('Página de Gestión de Acampantes')).not.toBeInTheDocument();

    // Navigate back to "Lista de Equipo"
    const equipoLink = screen.getByRole('link', { name: /Lista de Equipo/i });
    fireEvent.click(equipoLink);
    expect(screen.getByText('Lista de Equipamiento')).toBeInTheDocument();
    expect(screen.queryByText('Página de Cronograma de Eventos')).not.toBeInTheDocument();
  });
});
