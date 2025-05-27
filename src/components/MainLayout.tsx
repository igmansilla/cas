import React from 'react';

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="flex h-screen bg-gray-100"> {/* Added bg-gray-100 for overall page background */}
      {/* Sidebar */}
      <div className="w-64 bg-gray-800 text-white p-5 space-y-4"> {/* Styled sidebar: darker, white text, padding, spacing */}
        <h2 className="text-2xl font-semibold mb-6">Navigation</h2> {/* Changed title */}
        
        <nav className="space-y-2"> {/* Added spacing for nav items */}
          {/* Active Link for Packing List */}
          <div className="block px-4 py-2.5 bg-gray-700 rounded-lg shadow"> {/* Styling for active link */}
            <span className="font-medium">Packing List</span>
          </div>

          {/* Placeholder for Future Features */}
          <div className="px-4 py-2 mt-6"> {/* Margin top for separation */}
            <span className="text-xs font-semibold text-gray-400 uppercase">Future Features</span>
          </div>
          
          <div className="block px-4 py-2.5 text-gray-400 cursor-not-allowed"> {/* Styling for disabled/placeholder link */}
            <span>Campers Management</span>
          </div>
          <div className="block px-4 py-2.5 text-gray-400 cursor-not-allowed">
            <span>Event Schedule</span>
          </div>
          <div className="block px-4 py-2.5 text-gray-400 cursor-not-allowed">
            <span>Group Chat</span>
          </div>
        </nav>
      </div>

      {/* Content Area */}
      <div className="flex-1 p-6 overflow-y-auto"> {/* Added more padding to content area */}
        {children}
      </div>
    </div>
  );
};

export default MainLayout;
