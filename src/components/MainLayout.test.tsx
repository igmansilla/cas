import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import MainLayout from './MainLayout';
import { MemoryRouter, Routes, Route, NavigateProps }
    from 'react-router-dom';

// Mock react-router-dom's useNavigate and NavLink
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
        NavLink: ({ children, to }: { children: React.ReactNode, to: string }) => <a href={to}>{children}</a>,
    };
});

// Mock global fetch
global.fetch = vi.fn();

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

// Mock Lucide icons
vi.mock('lucide-react', () => ({
    MenuIcon: () => <div>Menu</div>,
    XIcon: () => <div>X</div>,
    LogOut: () => <div>LogoutIcon</div>,
}));


const TestPage: React.FC<{ title: string }> = ({ title }) => <div>{title}</div>;

const renderMainLayoutWithUser = (userData: any) => {
    mockLocalStorage.setItem('user', JSON.stringify(userData));
    return render(
        <MemoryRouter initialEntries={['/']}>
            <MainLayout>
                <Routes>
                    <Route path="/" element={<TestPage title="Home Page Content" />} />
                    <Route path="/equipo" element={<TestPage title="Equipo Page" />} />
                    <Route path="/acampantes" element={<TestPage title="Acampantes Page" />} />
                    <Route path="/eventos" element={<TestPage title="Eventos Page" />} />
                    <Route path="/chat" element={<TestPage title="Chat Page" />} />
                    <Route path="/mis-actividades" element={<TestPage title="Mis Actividades Page" />} />
                </Routes>
            </MainLayout>
        </MemoryRouter>
    );
};


describe('MainLayout', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockLocalStorage.clear();
        (fetch as ReturnType<typeof vi.fn>).mockResolvedValue({ ok: true, json: async () => ({}) }); // Default successful fetch for logout
    });

    test('renders common navigation links and user info', () => {
        const userData = { username: 'testuser', roles: ['ROLE_USER'] };
        renderMainLayoutWithUser(userData);

        expect(screen.getByText(`Usuario: ${userData.username}`)).toBeInTheDocument();
        expect(screen.getByText(`Roles: ${userData.roles.join(', ')}`)).toBeInTheDocument();
        expect(screen.getByText('Lista de Equipo')).toBeInTheDocument(); // NavLink renders as <a>
        expect(screen.getByText('Cronograma de Eventos')).toBeInTheDocument();
        expect(screen.getByText('Chat Grupal')).toBeInTheDocument();
        expect(screen.getByText('Cerrar Sesión')).toBeInTheDocument();
    });

    test('shows "Gestión de Acampantes" for ROLE_DIRIGENTE', () => {
        renderMainLayoutWithUser({ username: 'dirigente', roles: ['ROLE_DIRIGENTE'] });
        expect(screen.getByText('Gestión de Acampantes')).toBeInTheDocument();
    });

    test('shows "Gestión de Acampantes" for ROLE_ADMIN', () => {
        renderMainLayoutWithUser({ username: 'admin', roles: ['ROLE_ADMIN'] });
        expect(screen.getByText('Gestión de Acampantes')).toBeInTheDocument();
    });

    test('does NOT show "Gestión de Acampantes" for basic user', () => {
        renderMainLayoutWithUser({ username: 'user', roles: ['ROLE_CAMPISTA'] });
        expect(screen.queryByText('Gestión de Acampantes')).not.toBeInTheDocument();
    });

    test('shows "Mis Actividades (Campista)" for ROLE_CAMPISTA and not DIRIGENTE/ADMIN', () => {
        renderMainLayoutWithUser({ username: 'campista', roles: ['ROLE_CAMPISTA'] });
        expect(screen.getByText('Mis Actividades (Campista)')).toBeInTheDocument();
    });

    test('does NOT show "Mis Actividades (Campista)" for ROLE_DIRIGENTE', () => {
        renderMainLayoutWithUser({ username: 'dirigente', roles: ['ROLE_DIRIGENTE', 'ROLE_CAMPISTA'] });
        // Assuming DIRIGENTE role takes precedence or the condition is strictly !DIRIGENTE && !ADMIN
        expect(screen.queryByText('Mis Actividades (Campista)')).not.toBeInTheDocument();
    });


    test('handles logout correctly', async () => {
        renderMainLayoutWithUser({ username: 'testuser', roles: ['USER'] });

        const logoutButton = screen.getByText('Cerrar Sesión'); // This will actually be the button
        // Ensure the button is interactive before clicking
        expect(logoutButton.closest('button')).not.toBeDisabled();


        fireEvent.click(logoutButton.closest('button')!);


        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/logout', {
                method: 'POST',
                 headers: {},
            });
        });

        await waitFor(() => expect(mockLocalStorage.getItem('user')).toBeNull());
        await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/login'));
    });

    test('handles logout correctly even if backend call fails', async () => {
        (fetch as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Network error'));
        renderMainLayoutWithUser({ username: 'testuser', roles: ['USER'] });

        fireEvent.click(screen.getByText('Cerrar Sesión').closest('button')!);

        await waitFor(() => expect(fetch).toHaveBeenCalledTimes(1));
        // Client-side logout should still occur
        await waitFor(() => expect(mockLocalStorage.getItem('user')).toBeNull());
        await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/login'));
    });

    test('redirects to login if user data is not in localStorage initially and ProtectedRoute is not used', () => {
        // This test specifically checks MainLayout's own safeguard.
        mockLocalStorage.clear();
        render(
            <MemoryRouter initialEntries={['/']}>
                <MainLayout>
                    <div>Child Content Should Not Render</div>
                </MainLayout>
            </MemoryRouter>
        );
        // Check that navigate to /login was called by MainLayout's useEffect
        // It might be called multiple times if there are re-renders, so check if called at all.
        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
});
