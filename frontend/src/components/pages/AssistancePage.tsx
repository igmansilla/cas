// frontend/src/components/pages/AssistancePage.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../../services/api';
import type { AssistanceRecord, UserData } from '../../types'; // Asegúrate de que UserData incluya id y username
import { useLocalStorage } from '../../hooks/useLocalStorage'; // Para obtener el usuario actual

// Interfaz para el estado de cada campista en la UI
interface CamperAssistance extends UserData {
  assistanceId?: number; // ID del registro de asistencia, si existe
  hasAttended: boolean | null; // null si no hay registro, boolean si sí
  name: string; // Nombre del campista para mostrar
}

const AssistancePage: React.FC = () => {
  const [campers, setCampers] = useState<CamperAssistance[]>([]);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { storedValue: currentUser } = useLocalStorage<UserData | null>('user', null);

  // Función para obtener la lista de todos los usuarios (campistas)
  // Esto es un placeholder, idealmente tendrías un endpoint específico para "campers" o usuarios con rol "CAMPER"
  const fetchAllUsers = useCallback(async () => {
    try {
      // Este endpoint es un ejemplo. Deberías tener uno que devuelva todos los usuarios
      // o al menos aquellos que son relevantes para la asistencia (ej. rol CAMPER).
      // Por ahora, asumiremos que /api/users devuelve UserData[] o algo similar.
      // Si getCurrentUser devuelve solo el logueado, necesitarás otro endpoint.
      // const response = await api.call<UserData[]>('/api/users'); // Endpoint hipotético

      // *** Placeholder si no tienes un endpoint de todos los usuarios ***
      // Si solo puedes obtener el usuario actual, esta página solo funcionará para ese usuario.
      // Para una gestión real, necesitas una lista de TODOS los campistas.
      // Aquí usaremos un mock o una lista limitada si no hay endpoint.
      if (currentUser && currentUser.username) { // Asegurarse que currentUser y username no son null
        // Mock de otros usuarios para demostración si no hay endpoint /api/users
        const mockUsers: UserData[] = [
          currentUser, // Incluir al usuario actual
          // Añade otros usuarios mock si es necesario para probar
          // { id: 2, username: 'Maria Lopez', roles: ['CAMPER'] },
          // { id: 3, username: 'Carlos Sanchez', roles: ['CAMPER'] },
        ];
        // Filtrar para asegurar que todos los usuarios tienen un id y username.
        // En un caso real, el backend debería garantizar esto.
        return mockUsers.filter(u => u.username) as (UserData & { id: number; username: string })[];
      }
      return []; // Devuelve un array vacío si no hay usuario actual o no hay endpoint
      // return response.data || [];
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Error al cargar la lista de campistas.');
      return [];
    }
  }, [currentUser]);


  // Función para cargar la asistencia de los campistas para la fecha seleccionada
  const fetchAssistanceData = useCallback(async (date: string, users: (UserData & { id: number; username: string })[]) => {
    if (users.length === 0) {
      setCampers([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const userIds = users.map(u => u.id);
      // El backend debería devolver UserData con el id
      // const response = await api.assistance.getForUsersOnDate({ userIds, date }); // Este endpoint no existe en el controller
      // Usaremos getByDate y luego mapearemos. O mejor, que el controller de Assistance devuelva el nombre del usuario.
      const assistanceResponse = await api.assistance.getByDate(date);

      const assistanceRecords = assistanceResponse.data || [];

      const camperAssistanceStatus: CamperAssistance[] = users.map(user => {
        const record = assistanceRecords.find(ar => ar.userId === user.id);
        return {
          ...user,
          name: user.username, // Asumiendo que username es el nombre a mostrar
          assistanceId: record?.id,
          hasAttended: record ? record.hasAttended : null, // null si no hay registro
        };
      });
      setCampers(camperAssistanceStatus);
    } catch (err: any) {
      setError(err.message || 'Error al cargar los datos de asistencia.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  // Efecto para cargar usuarios y luego su asistencia
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      const users = await fetchAllUsers();
       // Asegurarse de que users es del tipo correcto antes de pasarlo
      const validUsers = users.filter(u => typeof u.id === 'number' && typeof u.username === 'string') as (UserData & { id: number; username: string })[];
      if (validUsers.length > 0) {
        await fetchAssistanceData(selectedDate, validUsers);
      } else {
         setCampers([]); // No hay usuarios para mostrar
         setLoading(false);
      }
    };
    loadData();
  }, [selectedDate, fetchAllUsers, fetchAssistanceData]);


  const handleToggleAssistance = async (userId: number) => {
    const camper = campers.find(c => c.id === userId);
    if (!camper) return;

    const newHasAttended = camper.hasAttended === null ? true : !camper.hasAttended;

    try {
      // Optimistic update
      setCampers(prevCampers =>
        prevCampers.map(c =>
          c.id === userId ? { ...c, hasAttended: newHasAttended, name: c.username } : c
        )
      );

      const response = await api.assistance.record({
        userId,
        date: selectedDate,
        hasAttended: newHasAttended,
      });

      // Update with server response (e.g., if ID is generated)
      setCampers(prevCampers =>
        prevCampers.map(c =>
          c.id === userId ? { ...c, assistanceId: response.data?.id, hasAttended: newHasAttended, name: c.username } : c
        )
      );
    } catch (err: any) {
      setError(err.message || 'Error al actualizar la asistencia.');
      console.error(err);
      // Revert optimistic update
      setCampers(prevCampers =>
        prevCampers.map(c =>
          c.id === userId ? { ...c, hasAttended: camper.hasAttended, name: camper.username } : c
        )
      );
    }
  };

  const handleDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedDate(event.target.value);
  };

  if (!currentUser) {
    return <div className="p-4">Por favor, inicia sesión para gestionar la asistencia.</div>;
  }


  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Gestión de Asistencia</h1>

      <div className="mb-4">
        <label htmlFor="assistance-date" className="block text-sm font-medium text-gray-700 mr-2">
          Seleccionar Fecha:
        </label>
        <input
          type="date"
          id="assistance-date"
          value={selectedDate}
          onChange={handleDateChange}
          className="mt-1 block w-full md:w-auto px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
        />
      </div>

      {loading && <div className="p-4">Cargando asistencia...</div>}
      {error && <div className="p-4 text-red-500">{error}</div>}

      {!loading && !error && (
        <>
          {campers.length === 0 ? (
            <p>No hay campistas para mostrar o no se encontraron registros de asistencia para esta fecha.</p>
          ) : (
            <ul className="space-y-2">
              {campers.map(camper => (
                <li
                  key={camper.id}
                  className={`p-3 rounded-md shadow-sm flex justify-between items-center transition-colors ${
                    camper.hasAttended === true ? 'bg-green-100' :
                    camper.hasAttended === false ? 'bg-red-100' : 'bg-gray-100'
                  }`}
                >
                  <span className="text-lg">{camper.name}</span> {/* Usar camper.name que viene de UserData.username */}
                  <button
                    onClick={() => camper.id && handleToggleAssistance(camper.id)}
                    disabled={!camper.id} // Deshabilitar si no hay ID (no debería pasar con UserData)
                    className={`px-4 py-2 rounded-md text-white font-semibold transition-colors ${
                      camper.hasAttended === true
                        ? 'bg-red-500 hover:bg-red-600' // Presente -> Marcar Ausente
                        : 'bg-green-500 hover:bg-green-600' // Ausente o sin marcar -> Marcar Presente
                    }`}
                  >
                    {camper.hasAttended === true ? 'Marcar Ausente' : 'Marcar Presente'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  );
};

export default AssistancePage;
