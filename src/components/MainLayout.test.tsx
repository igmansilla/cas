import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom'; // Import MemoryRouter
import MainLayout from './MainLayout';
import { describe, it, expect, vi } from 'vitest';

// Mock lucide-react icons
vi.mock('lucide-react', async (importOriginal) => {
  const originalModule = await importOriginal();
  return {
    ...(originalModule as object),
    Menu: () => <div data-testid="menu-icon">Menu</div>,
    X: () => <div data-testid="x-icon">X</div>,
  };
});

const renderWithRouter = (ui: React.ReactElement, options = {}) => {
  return render(ui, { wrapper: MemoryRouter, ...options });
};

describe('MainLayout Tests with Routing and Localization', () => {
  it('renders children correctly', () => {
    renderWithRouter(
      <MainLayout>
        <div>Test Child Content</div>
      </MainLayout>
    );
    expect(screen.getByText('Test Child Content')).toBeInTheDocument();
  });

  it('renders "CASAPP" as mobile header title', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    // The title "CASAPP" is inside a span in the mobile header.
    // The mobile header is initially visible in the DOM but hidden by md:hidden on larger screens.
    // We need to ensure we're selecting the correct element if there are multiple "CASAPP" texts.
    // For this component, it's specific enough.
    expect(screen.getByText('CASAPP')).toBeInTheDocument();
  });

  it('renders sidebar navigation links with correct Spanish text and hrefs', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);

    const listaEquipoLink = screen.getByRole('link', { name: /Lista de Equipo/i });
    expect(listaEquipoLink).toBeInTheDocument();
    expect(listaEquipoLink).toHaveAttribute('href', '/equipo');

    const gestionAcampantesLink = screen.getByRole('link', { name: /Gestión de Acampantes/i });
    expect(gestionAcampantesLink).toBeInTheDocument();
    expect(gestionAcampantesLink).toHaveAttribute('href', '/acampantes');

    const cronogramaEventosLink = screen.getByRole('link', { name: /Cronograma de Eventos/i });
    expect(cronogramaEventosLink).toBeInTheDocument();
    expect(cronogramaEventosLink).toHaveAttribute('href', '/eventos');

    const chatGrupalLink = screen.getByRole('link', { name: /Chat Grupal/i });
    expect(chatGrupalLink).toBeInTheDocument();
    expect(chatGrupalLink).toHaveAttribute('href', '/chat');

    // Check for "Future Features" as it's still part of the layout
    expect(screen.getByText('Future Features')).toBeInTheDocument();
  });
});

describe('MainLayout Responsive Behavior (Unchanged tests)', () => {
  const hasClass = (element: HTMLElement, classNameSubstring: string) => {
    return Array.from(element.classList).some(cls => cls.includes(classNameSubstring));
  };

  it('renders toggle button with mobile-only visibility', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    expect(toggleButton.parentElement?.classList.contains('md:hidden')).toBe(true);
  });

  it('sidebar is initially hidden on mobile (by checking transform class)', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    const sidebar = screen.getByTestId('sidebar');
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
  });

  it('clicking toggle button opens and closes sidebar on mobile, and changes icon', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    const sidebar = screen.getByTestId('sidebar');

    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument();

    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, 'translate-x-0')).toBe(true);
    expect(screen.getByTestId('x-icon')).toBeInTheDocument();

    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument();
  });

  it('shows overlay when mobile sidebar is open and hides it when closed', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');

    expect(screen.queryByTestId('mobile-overlay')).toBeNull();

    fireEvent.click(toggleButton);
    expect(screen.getByTestId('mobile-overlay')).toBeInTheDocument();

    fireEvent.click(toggleButton);
    expect(screen.queryByTestId('mobile-overlay')).toBeNull();
  });

  it('clicking overlay closes mobile sidebar', () => {
    renderWithRouter(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    const sidebar = screen.getByTestId('sidebar');

    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, 'translate-x-0')).toBe(true);
    const overlay = screen.getByTestId('mobile-overlay');
    expect(overlay).toBeInTheDocument();

    fireEvent.click(overlay);
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.queryByTestId('mobile-overlay')).toBeNull();
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument();
  });

  it('renders sidebar navigation elements and sidebar is static on desktop', () => {
    renderWithRouter(<MainLayout><div>Test Content</div></MainLayout>);
    const sidebar = screen.getByTestId('sidebar');
    expect(hasClass(sidebar, 'md:static')).toBe(true);
    expect(hasClass(sidebar, 'md:translate-x-0')).toBe(true);

    // Check content is visible (using Spanish text)
    expect(screen.getByRole('link', { name: /Lista de Equipo/i })).toBeInTheDocument();
    expect(screen.getByText('Future Features')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Gestión de Acampantes/i })).toBeInTheDocument();
  });
});
