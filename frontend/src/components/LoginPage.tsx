import React, { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Logo } from './Logo'; // Import the Logo component

const LoginPage: React.FC = () => {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const navigate = useNavigate();

    const getCsrfToken = (): string | null => {
        console.log("Attempting to find XSRF-TOKEN in cookies:", document.cookie);
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'XSRF-TOKEN') {
                console.log("XSRF-TOKEN found:", value);
                return value;
            }
        }
        console.log("XSRF-TOKEN not found in cookies.");
        return null;
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setLoading(true);
        setError(null);

        console.log("All cookies:", document.cookie);
        const csrfToken = getCsrfToken();
        if (!csrfToken) {
            setError('CSRF token not found. Please refresh the page.');
            setLoading(false);
            return;
        }

        try {
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-XSRF-TOKEN': csrfToken,
            };
            console.log("Request headers to be sent:", headers);

            const response = await fetch('/perform_login', { // Updated to custom login processing URL
                method: 'POST',
                headers: headers,
                body: new URLSearchParams({ username, password }),
            });

            setLoading(false);

            if (response.ok) {
                const userData = await response.json();
                localStorage.setItem('user', JSON.stringify(userData));
                navigate('/equipo'); // Redirect to a protected page
            } else {
                if (response.status === 401) {
                    const errorData = await response.json();
                    setError(errorData.message || 'Invalid username or password.');
                } else {
                    setError(`Login failed: ${response.statusText} (Status: ${response.status})`);
                }
            }
        } catch (err) {
            setLoading(false);
            setError('An unexpected error occurred. Please try again.');
            console.error('Login error:', err);
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col justify-center items-center p-4 sm:p-0">
            <div className="bg-white shadow-lg rounded-lg p-6 sm:p-8 max-w-md w-full">
                <div className="flex justify-center mb-6">
                    <Logo />
                </div>
                <h2 className="text-3xl font-bold mb-6 text-center text-cas-black">Login</h2>
                <form onSubmit={handleSubmit}>
                    <div className="mb-5">
                        <label htmlFor="username" className="block mb-2 text-sm font-medium text-cas-black">Username:</label>
                        <input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-cas-orange focus:border-cas-orange text-cas-black"
                        />
                    </div>
                    <div className="mb-6">
                        <label htmlFor="password" className="block mb-2 text-sm font-medium text-cas-black">Password:</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:ring-cas-orange focus:border-cas-orange text-cas-black"
                        />
                    </div>
                    {error && <p className="text-cas-red text-sm mb-4 text-center">{error}</p>}
                    <button type="submit" disabled={loading} className="w-full py-3 px-4 bg-cas-orange text-cas-white font-semibold rounded-md shadow-md cursor-pointer hover:bg-cas-orange/90 focus:outline-none focus:ring-2 focus:ring-cas-orange focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed">
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;
