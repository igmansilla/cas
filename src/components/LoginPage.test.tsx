import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import LoginPage from './LoginPage';

// Mock react-router-dom's useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
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


describe('LoginPage', () => {
    beforeEach(() => {
        // Clear mocks before each test
        vi.clearAllMocks();
        mockLocalStorage.clear();
        // Reset document.cookie if getCsrfToken relies on it directly
        Object.defineProperty(document, 'cookie', {
            writable: true,
            value: 'XSRF-TOKEN=test-csrf-token',
        });
    });

    test('renders initial form elements', () => {
        render(<LoginPage />);
        expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    });

    test('allows typing into username and password fields', () => {
        render(<LoginPage />);
        const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
        const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;

        fireEvent.change(usernameInput, { target: { value: 'testuser' } });
        expect(usernameInput.value).toBe('testuser');

        fireEvent.change(passwordInput, { target: { value: 'password123' } });
        expect(passwordInput.value).toBe('password123');
    });

    test('handles successful login', async () => {
        const mockUserData = { username: 'testuser', roles: ['USER'] };
        (fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
            ok: true,
            json: async () => mockUserData,
        });

        render(<LoginPage />);
        fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testuser' } });
        fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
        fireEvent.click(screen.getByRole('button', { name: /login/i }));

        expect(screen.getByRole('button', { name: /logging in.../i})).toBeInTheDocument();

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-XSRF-TOKEN': 'test-csrf-token',
                },
                body: new URLSearchParams({ username: 'testuser', password: 'password123' }),
            });
        });

        await waitFor(() => expect(mockLocalStorage.getItem('user')).toBe(JSON.stringify(mockUserData)));
        await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/equipo'));
    });

    test('handles failed login with error message', async () => {
        const mockErrorData = { message: 'Invalid credentials' };
        (fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
            ok: false,
            status: 401,
            json: async () => mockErrorData,
        });

        render(<LoginPage />);
        fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'wronguser' } });
        fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrongpass' } });
        fireEvent.click(screen.getByRole('button', { name: /login/i }));

        expect(screen.getByRole('button', { name: /logging in.../i})).toBeInTheDocument();

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/login', expect.anything());
        });

        await waitFor(() => expect(screen.getByText(mockErrorData.message)).toBeInTheDocument());

        expect(mockLocalStorage.getItem('user')).toBeNull();
        expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('handles CSRF token not found', async () => {
        Object.defineProperty(document, 'cookie', {
            writable: true,
            value: '', // No CSRF token
        });

        render(<LoginPage />);
        fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testuser' } });
        fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
        fireEvent.click(screen.getByRole('button', { name: /login/i }));

        await waitFor(() => expect(screen.getByText('CSRF token not found. Please refresh the page.')).toBeInTheDocument());
        expect(fetch).not.toHaveBeenCalled();
        expect(mockNavigate).not.toHaveBeenCalled();
    });
});
