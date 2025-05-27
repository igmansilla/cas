import React from "react";
import { Routes, Route, Navigate } from "react-router-dom"; // Import Routes, Route, Navigate
import MainLayout from "./components/MainLayout";
import PackingListApp from "./components/PackingListApp";
import GestionAcampantesPage from "./components/GestionAcampantesPage"; // Import new components
import CronogramaEventosPage from "./components/CronogramaEventosPage";
import ChatGrupalPage from "./components/ChatGrupalPage";
import './index.css';

function App() {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<Navigate replace to="/equipo" />} /> {/* Redirect root to /equipo */}
        <Route path="/equipo" element={<PackingListApp />} />
        <Route path="/acampantes" element={<GestionAcampantesPage />} />
        <Route path="/eventos" element={<CronogramaEventosPage />} />
        <Route path="/chat" element={<ChatGrupalPage />} />
        {/* Optional: Add a catch-all route for 404 Not Found */}
        {/* <Route path="*" element={<div>Page Not Found</div>} /> */}
      </Routes>
    </MainLayout>
  );
}

export default App;
