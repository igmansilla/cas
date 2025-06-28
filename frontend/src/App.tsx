import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import {
  MainLayout,
  PackingListApp,
  GestionAcampantesPage,
  CronogramaEventosPage,
  LoginPage,
  ProtectedRoute
} from "./components";
import "./index.css";

function App() {
  return (
    <Routes>
      {/* Public route for login */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Navigate replace to="/equipo" />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/equipo"
        element={
          <ProtectedRoute>
            <MainLayout>
              <PackingListApp />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/acampantes"
        element={
          <ProtectedRoute>
            <MainLayout>
              <GestionAcampantesPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/eventos"
        element={
          <ProtectedRoute>
            <MainLayout>
              <CronogramaEventosPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Optional: Add a catch-all route for 404 Not Found, can also be protected or public */}
      {/* <Route path="*" element={<div>Page Not Found</div>} /> */}
    </Routes>
  );
}

export default App;
