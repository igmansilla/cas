import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom'; // Import NavLink and useNavigate
import { Menu as MenuIcon, X as XIcon, LogOut } from 'lucide-react'; // For hamburger, close, and logout icons
import { api } from '../services/api';

interface MainLayoutProps {
  children: React.ReactNode;
}

interface UserData {
  username: string;
  roles: string[];
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const [user, setUser] = useState<UserData | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const parsedUser: UserData = JSON.parse(storedUser);
        setUser(parsedUser);
      } catch (error) {
        console.error("Failed to parse user data from localStorage", error);
        localStorage.removeItem('user'); // Clear corrupted data
        setUser(null); // Ensure user state is null if parsing fails
        navigate('/login'); // Redirect to login if data is corrupted
      }
    } else {
      // This case should ideally be handled by ProtectedRoute, but as a safeguard:
      navigate('/login');
    }
  }, [navigate]);
  const handleLogout = async () => {
    try {
      await api.auth.logout();
    } catch (error) {
      console.error('Error during logout:', error);
    } finally {
      // Always perform client-side logout
      localStorage.removeItem('user');
      setUser(null);
      navigate('/login');
    }
  };

  // Helper to check for roles
  const hasRole = (roleName: string): boolean => {
    return user?.roles?.includes(roleName) ?? false;
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div
        data-testid="sidebar"
        className={`
          fixed inset-y-0 left-0 z-30 w-64 bg-primary text-white p-5 space-y-4
          transform transition-transform ease-in-out duration-300
          ${isMobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          md:static md:translate-x-0 md:flex md:flex-col md:flex-shrink-0 justify-between
        `}
      >
        <div> {/* Navigation Links Group */}
          <h2 className="text-2xl font-semibold mb-2">CASAPP</h2>
          {user && (
            <div className="mb-4 text-sm">
              <p>Usuario: {user.username}</p>
              <p>Roles: {user.roles.join(', ')}</p>
            </div>
          )}
          <nav className="space-y-2">
            {/* Common Link for all authenticated users */}
            <NavLink
              to="/equipo"
              className={({ isActive }) =>
                `block px-4 py-2.5 rounded-lg shadow-sm transition-colors duration-150 ease-in-out ${
                  isActive ? 'bg-primary-dark text-white font-medium' : 'text-gray-300 hover:bg-primary-dark hover:text-white'
                }`
              }
            >
              Lista de Equipo
            </NavLink>

            {/* Conditional Link for ROLE_DIRIGENTE or ROLE_ADMIN */}
            {(hasRole('ROLE_DIRIGENTE') || hasRole('ROLE_ADMIN')) && (
              <NavLink
                to="/acampantes"
                className={({ isActive }) =>
                  `block px-4 py-2.5 rounded-lg shadow-sm transition-colors duration-150 ease-in-out ${
                    isActive ? 'bg-primary-dark text-white font-medium' : 'text-gray-300 hover:bg-primary-dark hover:text-white'
                  }`
                }
              >
                Gestión de Acampantes
              </NavLink>
            )}

            {/* Example for a hypothetical ROLE_CAMPISTA or any authenticated user not DIRIGENTE/ADMIN */}
            {/* This is just an example, adjust logic as needed */}
            {hasRole('ROLE_CAMPISTA') && !(hasRole('ROLE_DIRIGENTE') || hasRole('ROLE_ADMIN')) && (
               <NavLink
                to="/mis-actividades" // Hypothetical route
                className={({ isActive }) =>
                  `block px-4 py-2.5 rounded-lg shadow-sm transition-colors duration-150 ease-in-out ${
                    isActive ? 'bg-primary-dark text-white font-medium' : 'text-gray-300 hover:bg-primary-dark hover:text-white'
                  }`
                }
              >
                Mis Actividades (Campista)
              </NavLink>
            )}            {/* Common Links */}
            <NavLink
              to="/eventos"
              className={({ isActive }) =>
                `block px-4 py-2.5 rounded-lg shadow-sm transition-colors duration-150 ease-in-out ${
                  isActive ? 'bg-primary-dark text-white font-medium' : 'text-gray-300 hover:bg-primary-dark hover:text-white'
                }`
              }
            >
              Cronograma de Eventos
            </NavLink>
          </nav>
        </div>

        {/* Logout Button at the bottom of sidebar */}
        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-start px-4 py-2.5 mt-6 rounded-lg shadow-sm text-gray-300 hover:bg-red-700 hover:text-white transition-colors duration-150 ease-in-out"
          aria-label="Logout"
        >
          <LogOut size={20} className="mr-2" />
          Cerrar Sesión
        </button>
      </div>

      {/* Content Area & Mobile Toggle Button */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="md:hidden p-4 bg-primary text-white flex justify-between items-center shadow-md">
          <button
            onClick={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
            className="p-2 rounded-md hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            aria-label="Toggle sidebar"
          >
            {isMobileSidebarOpen ? <XIcon size={24} /> : <MenuIcon size={24} />}
          </button>
          <span className="text-xl font-bold">CASAPP</span>
        </div>
        
        <main className="flex-1 p-6 overflow-y-auto">
          {user ? children : <div>Loading user data or redirecting...</div>}
        </main>
      </div>
      
      {isMobileSidebarOpen && (
        <div
          data-testid="mobile-overlay"
          className="fixed inset-0 z-20 bg-black opacity-50 md:hidden"
          onClick={() => setIsMobileSidebarOpen(false)}
        ></div>
      )}
    </div>
  );
};

export default MainLayout;
