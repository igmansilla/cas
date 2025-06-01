import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import ProtectedRoute from './ProtectedRoute';
import { MemoryRouter, Routes, Route, NavigateProps } from 'react-router-dom';

// Mock react-router-dom's Navigate component
const mockNavigateComp = vi.fn(({ to }: NavigateProps) => <div>Navigating to {to}</div>);
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        Navigate: (props: NavigateProps) => mockNavigateComp(props),
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

const TestChildComponent: React.FC = () => <div>Protected Content</div>;

describe('ProtectedRoute', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockLocalStorage.clear();
    });

    test('renders children if user is authenticated', () => {
        mockLocalStorage.setItem('user', JSON.stringify({ username: 'testuser', roles: ['USER'] }));

        render(
            <MemoryRouter initialEntries={['/protected']}>
                <Routes>
                    <Route path="/protected" element={
                        <ProtectedRoute>
                            <TestChildComponent />
                        </ProtectedRoute>
                    } />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText('Protected Content')).toBeInTheDocument();
        expect(mockNavigateComp).not.toHaveBeenCalled();
    });

    test('redirects to /login if user is not authenticated', () => {
        // Ensure no user data in localStorage (cleared in beforeEach)

        render(
            <MemoryRouter initialEntries={['/protected']}>
                <Routes>
                    <Route path="/protected" element={
                        <ProtectedRoute>
                            <TestChildComponent />
                        </ProtectedRoute>
                    } />
                    <Route path="/login" element={<div>Login Page</div>} />
                </Routes>
            </MemoryRouter>
        );

        // The child component should not be rendered
        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();

        // Navigate component should have been called with to="/login"
        expect(mockNavigateComp).toHaveBeenCalledTimes(1);
        expect(mockNavigateComp).toHaveBeenCalledWith(expect.objectContaining({ to: '/login', replace: true }));

        // Check if the placeholder for Navigate (rendered by mockNavigateComp) is present
        expect(screen.getByText('Navigating to /login')).toBeInTheDocument();
    });
});
