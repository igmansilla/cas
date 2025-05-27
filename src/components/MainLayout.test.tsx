import { render, screen, fireEvent } from '@testing-library/react';
import MainLayout from './MainLayout';
import { describe, it, expect, vi } from 'vitest';

// Mock lucide-react icons
vi.mock('lucide-react', async (importOriginal) => {
  const originalModule = await importOriginal(); // Correctly await the dynamic import
  return {
    ...(originalModule as object), // Spread the original module properties
    Menu: () => <div data-testid="menu-icon">Menu</div>,
    X: () => <div data-testid="x-icon">X</div>,
  };
});

describe('MainLayout Original Tests', () => {
  it('renders children correctly', () => {
    render(
      <MainLayout>
        <div>Test Child Content</div>
      </MainLayout>
    );
    expect(screen.getByText('Test Child Content')).toBeInTheDocument();
  });

  // This test is now more nuanced due to mobile-first design.
  // On mobile, these are initially hidden. On desktop, they are visible.
  // The new "desktop view" test covers the desktop scenario.
  // We can adjust this to reflect mobile initial state or remove it if redundant.
  it('sidebar navigation elements are present in the DOM (though may be hidden on mobile initially)', () => {
    render(
      <MainLayout>
        <div>Test Content</div>
      </MainLayout>
    );
    expect(screen.getByText('Packing List')).toBeInTheDocument();
    expect(screen.getByText('Future Features')).toBeInTheDocument();
    expect(screen.getByText('Campers Management')).toBeInTheDocument();
    expect(screen.getByText('Event Schedule')).toBeInTheDocument();
  });
});

describe('MainLayout Responsive Behavior', () => {
  // Helper to check for class substrings, useful for transform: translateX
  const hasClass = (element: HTMLElement, classNameSubstring: string) => {
    return Array.from(element.classList).some(cls => cls.includes(classNameSubstring));
  };

  it('renders toggle button with mobile-only visibility', () => {
    render(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    // The button is inside a div with md:hidden.
    expect(toggleButton.parentElement?.classList.contains('md:hidden')).toBe(true);
  });

  it('sidebar is initially hidden on mobile (by checking transform class)', () => {
    render(<MainLayout><div>Test</div></MainLayout>);
    const sidebar = screen.getByTestId('sidebar'); 
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
  });

  it('clicking toggle button opens and closes sidebar on mobile, and changes icon', () => {
    render(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    const sidebar = screen.getByTestId('sidebar');

    // Check initial state (closed)
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument();

    // Click to open
    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, 'translate-x-0')).toBe(true);
    expect(screen.getByTestId('x-icon')).toBeInTheDocument(); 

    // Click to close
    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument();
  });
  
  it('shows overlay when mobile sidebar is open and hides it when closed', () => {
    render(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    
    // Initially, no overlay
    expect(screen.queryByTestId('mobile-overlay')).toBeNull();

    // Click to open sidebar
    fireEvent.click(toggleButton);
    expect(screen.getByTestId('mobile-overlay')).toBeInTheDocument();

    // Click to close sidebar using the toggle button
    fireEvent.click(toggleButton);
    expect(screen.queryByTestId('mobile-overlay')).toBeNull();
  });

  it('clicking overlay closes mobile sidebar', () => {
    render(<MainLayout><div>Test</div></MainLayout>);
    const toggleButton = screen.getByLabelText('Toggle sidebar');
    const sidebar = screen.getByTestId('sidebar');

    // Open sidebar
    fireEvent.click(toggleButton);
    expect(hasClass(sidebar, 'translate-x-0')).toBe(true);
    const overlay = screen.getByTestId('mobile-overlay');
    expect(overlay).toBeInTheDocument();

    // Click overlay
    fireEvent.click(overlay);
    expect(hasClass(sidebar, '-translate-x-full')).toBe(true);
    expect(screen.queryByTestId('mobile-overlay')).toBeNull();
    expect(screen.getByTestId('menu-icon')).toBeInTheDocument(); // Icon should revert
  });

  it('renders sidebar navigation elements and sidebar is static on desktop', () => {
    render(<MainLayout><div>Test Content</div></MainLayout>);
    const sidebar = screen.getByTestId('sidebar');
    // Check for desktop class that makes it static and visible
    expect(hasClass(sidebar, 'md:static')).toBe(true); 
    expect(hasClass(sidebar, 'md:translate-x-0')).toBe(true); // Should not be translated off-screen

    // Check content is visible
    expect(screen.getByText('Packing List')).toBeInTheDocument();
    expect(screen.getByText('Future Features')).toBeInTheDocument();
    expect(screen.getByText('Campers Management')).toBeInTheDocument();
  });
});
