import React from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
    children: JSX.Element;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const user = localStorage.getItem('user');

    if (!user) {
        // User not authenticated, redirect to login page
        return <Navigate to="/login" replace />;
    }

    // User authenticated, render the requested component
    return children;
};

export default ProtectedRoute;
