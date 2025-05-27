import React, { useState } from 'react';
import { NavLink } from 'react-router-dom'; // Import NavLink
import { Menu as MenuIcon, X as XIcon } from 'lucide-react'; // For hamburger and close icons

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      {/* Mobile: fixed, overlay, hidden by default, toggled by state */}
      {/* Desktop: static, part of flex layout */}
      <div
        data-testid="sidebar" // Added data-testid for easier selection
        className={`
          fixed inset-y-0 left-0 z-30 w-64 bg-primary text-white p-5 space-y-4
          transform transition-transform ease-in-out duration-300
          ${isMobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          md:static md:translate-x-0 md:flex md:flex-col md:flex-shrink-0
        `}
      >
        {/* Sidebar content from previous version */}
        <h2 className="text-2xl font-semibold mb-6">Navigation</h2>
        <nav className="space-y-2">
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
          {/* Placeholder for Future Features */}
          <div className="px-4 py-2 mt-6">
            <span className="text-xs font-semibold text-gray-300 uppercase">Future Features</span>
          </div>
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
          <NavLink
            to="/chat"
            className={({ isActive }) =>
              `block px-4 py-2.5 rounded-lg shadow-sm transition-colors duration-150 ease-in-out ${
                isActive ? 'bg-primary-dark text-white font-medium' : 'text-gray-300 hover:bg-primary-dark hover:text-white'
              }`
            }
          >
            Chat Grupal
          </NavLink>
        </nav>
      </div>

      {/* Content Area & Mobile Toggle Button */}
      <div className="flex-1 flex flex-col overflow-hidden"> {/* Added flex-col for potential header/button bar */}
        {/* Mobile Header/Toggle Area */}
        <div className="md:hidden p-4 bg-primary text-white flex justify-between items-center shadow-md"> {/* Example mobile header bar */}
          <button
            onClick={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
            className="p-2 rounded-md hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            aria-label="Toggle sidebar"
          >
            {isMobileSidebarOpen ? <XIcon size={24} /> : <MenuIcon size={24} />}
          </button>
          <span className="text-xl font-bold">CASAPP</span> {/* Optional: Title in mobile header */}
        </div>
        
        {/* Main Content */}
        <main className="flex-1 p-6 overflow-y-auto">
          {children}
        </main>
      </div>
      
      {/* Optional: Overlay for mobile when sidebar is open */}
      {isMobileSidebarOpen && (
        <div
          data-testid="mobile-overlay" // Added data-testid for easier selection
          className="fixed inset-0 z-20 bg-black opacity-50 md:hidden"
          onClick={() => setIsMobileSidebarOpen(false)}
        ></div>
      )}
    </div>
  );
};

export default MainLayout;
