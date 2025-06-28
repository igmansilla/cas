// frontend/src/components/pages/AssistancePage.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../../services/api';
import type { AssistanceRecord, UserData, UserDto } from '../../types';
import { useLocalStorage } from '../../hooks/useLocalStorage';

// Interfaz para el estado de cada campista en la UI
// Usamos UserDto como base ya que es lo que devolverán los nuevos endpoints
interface CamperDisplayInfo extends UserDto {
  assistanceId?: number;
  hasAttended: boolean | null;
  // 'name' ya no es necesario porque UserDto tiene 'username' que usaremos como nombre
}

const AssistancePage: React.FC = () => {
  const [displayList, setDisplayList] = useState<CamperDisplayInfo[]>([]);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { storedValue: currentUser } = useLocalStorage<UserData | null>('user', null); // UserData debe tener id y roles

  const isDirigente = currentUser?.roles?.includes('ROLE_DIRIGENTE');
  const isAdmin = currentUser?.roles?.includes('ROLE_ADMIN');
  const isStaff = currentUser?.roles?.includes('ROLE_STAFF');

  // Función para obtener la lista de campistas a mostrar
  const fetchCampersToDisplay = useCallback(async () => {
    if (!currentUser?.id) return [];
    setLoading(true);
    setError(null);
    try {
      if (isDirigente) {
        // Dirigente: obtener sus acampantes supervisados
        const response = await api.supervision.getSupervisedCampers(currentUser.id);
        return response.data || [];
      } else if (isAdmin || isStaff) {
        // Admin/Staff: obtener todos los acampantes
        const response = await api.supervision.getAllAcampantes();
        return response.data || [];
      }
      return []; // Otros roles no ven lista de campistas
    } catch (err: any) {
      setError(err.message || 'Error al cargar la lista de campistas.');
      console.error('Error fetching campers to display:', err);
      return [];
    } finally {
      setLoading(false);
    }
  }, [currentUser, isAdmin, isDirigente, isStaff]);

  // Función para cargar la asistencia de los campistas para la fecha seleccionada
  const fetchAssistanceForDisplayList = useCallback(async (date: string, campersSource: UserDto[]) => {
    if (campersSource.length === 0) {
      setDisplayList([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      let assistanceRecords: AssistanceRecord[] = [];
      if (isDirigente && currentUser?.id) {
        // Dirigente: usar el endpoint optimizado para sus supervisados
        const response = await api.assistance.getForSupervisedCampers(currentUser.id, date);
        assistanceRecords = response.data || [];
      } else if (isAdmin || isStaff) {
        // Admin/Staff: obtener asistencia para todos los campistas listados
        // (asumiendo que campersSource ya son solo acampantes)
        // Podríamos llamar a api.assistance.getByDate(date) y luego filtrar/mapear
        // o si la lista de campersSource es muy grande y getByDate devuelve demasiados datos,
        // se podría pensar en un endpoint getForUsersOnDate si fuera necesario,
        // pero getByDate y mapeo local es más simple si la cantidad de usuarios no es masiva.
        const response = await api.assistance.getByDate(date); // Obtiene toda la asistencia para la fecha
        const allAssistanceForDate = response.data || [];
        // Filtrar para solo los campistas que estamos mostrando
        const camperIdsToList = campersSource.map(c => c.id);
        assistanceRecords = allAssistanceForDate.filter(ar => camperIdsToList.includes(ar.userId));
      }

      const combinedList: CamperDisplayInfo[] = campersSource.map(user => {
        const record = assistanceRecords.find(ar => ar.userId === user.id);
        return {
          ...user, // id, username, roles from UserDto
          assistanceId: record?.id,
          hasAttended: record ? record.hasAttended : null,
        };
      });
      setDisplayList(combinedList);
    } catch (err: any) {
      setError(err.message || 'Error al cargar los datos de asistencia.');
      console.error('Error fetching assistance data:', err);
    } finally {
      setLoading(false);
    }
  }, [currentUser, isAdmin, isDirigente, isStaff]);

  // Efecto para cargar la lista de campistas y luego su asistencia
  useEffect(() => {
    const loadInitialData = async () => {
      if (!currentUser || (!isDirigente && !isAdmin && !isStaff)) {
        setLoading(false);
        setDisplayList([]);
        return;
      }
      setLoading(true);
      const campersSource = await fetchCampersToDisplay();
      if (campersSource && campersSource.length > 0) {
        await fetchAssistanceForDisplayList(selectedDate, campersSource);
      } else {
        setDisplayList([]);
        setLoading(false); // Si no hay campistas, terminar carga
      }
    };
    loadInitialData();
  }, [selectedDate, currentUser, fetchCampersToDisplay, fetchAssistanceForDisplayList, isDirigente, isAdmin, isStaff]);


  const handleToggleAssistance = async (userId: number) => {
    const camper = displayList.find(c => c.id === userId);
    if (!camper) return;

    const newHasAttended = camper.hasAttended === null ? true : !camper.hasAttended;

    // Optimistic update
    setDisplayList(prevList =>
      prevList.map(c =>
        c.id === userId ? { ...c, hasAttended: newHasAttended } : c
      )
    );

    try {
      const response = await api.assistance.record({
        userId,
        date: selectedDate,
        hasAttended: newHasAttended,
      });
      // Update with server response (e.g., if ID is generated or for consistency)
      setDisplayList(prevList =>
        prevList.map(c =>
          c.id === userId ? { ...c, assistanceId: response.data?.id, hasAttended: newHasAttended } : c
        )
      );
    } catch (err: any) {
      setError(err.message || 'Error al actualizar la asistencia.');
      console.error('Error toggling assistance:', err);
      // Revert optimistic update
      setDisplayList(prevList =>
        prevList.map(c =>
          c.id === userId ? { ...c, hasAttended: camper.hasAttended, assistanceId: camper.assistanceId } : c
        )
      );
    }
  };

  const handleDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedDate(event.target.value);
  };

  if (!currentUser) {
    return <div className="p-4 text-center">Por favor, inicia sesión para gestionar la asistencia.</div>;
  }

  if (!isDirigente && !isAdmin && !isStaff) {
    return <div className="p-4 text-center">No tienes permisos para gestionar la asistencia.</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-6 text-center">Gestión de Asistencia</h1>

      <div className="mb-6 flex justify-center">
        <div className="inline-flex flex-col items-start">
            <label htmlFor="assistance-date" className="block text-sm font-medium text-gray-700 mb-1">
            Seleccionar Fecha:
            </label>
            <input
            type="date"
            id="assistance-date"
            value={selectedDate}
            onChange={handleDateChange}
            className="px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
        </div>
      </div>

      {loading && <div className="p-4 text-center">Cargando...</div>}
      {error && <div className="p-4 text-red-500 text-center">Error: {error}</div>}

      {!loading && !error && (
        <>
          {displayList.length === 0 ? (
            <p className="text-center text-gray-600">
              {isDirigente ? "No tienes acampantes supervisados para mostrar." : "No hay acampantes para mostrar."}
            </p>
          ) : (
            <ul className="space-y-3 max-w-lg mx-auto">
              {displayList.map(camper => (
                <li
                  key={camper.id}
                  className={`p-4 rounded-lg shadow-md flex justify-between items-center transition-all duration-150 ease-in-out ${
                    camper.hasAttended === true ? 'bg-green-100 hover:bg-green-200' :
                    camper.hasAttended === false ? 'bg-red-100 hover:bg-red-200' : 'bg-gray-100 hover:bg-gray-200'
                  }`}
                >
                  <span className="text-lg font-medium text-gray-800">{camper.username}</span>
                  <button
                    onClick={() => handleToggleAssistance(camper.id)}
                    className={`px-4 py-2 rounded-md text-white font-semibold shadow-sm transition-transform transform hover:scale-105 ${
                      camper.hasAttended === true
                        ? 'bg-red-500 hover:bg-red-600 focus:ring-2 focus:ring-red-400 focus:ring-opacity-50' // Presente -> Marcar Ausente
                        : 'bg-green-500 hover:bg-green-600 focus:ring-2 focus:ring-green-400 focus:ring-opacity-50' // Ausente o sin marcar -> Marcar Presente
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
