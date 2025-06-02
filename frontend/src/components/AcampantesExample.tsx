import React, { useEffect } from 'react';
import { api } from '../services/api';
import { useApi, useApiList } from '../hooks/useApi';
import { Acampante } from '../types/api';

// Ejemplo de componente que maneja acampantes
const AcampantesExample: React.FC = () => {
  // Para obtener lista de acampantes
  const acampantesList = useApiList(api.acampantes.getAll);
  
  // Para crear un nuevo acampante
  const createAcampante = useApi(api.acampantes.create);
  
  // Para eliminar un acampante
  const deleteAcampante = useApi(api.acampantes.delete);

  // Cargar acampantes al montar el componente
  useEffect(() => {
    acampantesList.execute();
  }, []);

  const handleCreate = async () => {
    const newAcampante = {
      nombreCompleto: 'Juan Pérez',
      edad: 16,
      contactoEmergenciaNombre: 'María Pérez',
      contactoEmergenciaTelefono: '+1234567890'
    };
    
    const result = await createAcampante.execute(newAcampante);
    if (result?.success) {
      // Recargar la lista después de crear
      acampantesList.execute();
    }
  };

  const handleDelete = async (id: number) => {
    const result = await deleteAcampante.execute(id);
    if (result?.success) {
      // Recargar la lista después de eliminar
      acampantesList.execute();
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Gestión de Acampantes</h1>
        <button
          onClick={handleCreate}
          disabled={createAcampante.loading}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
        >
          {createAcampante.loading ? 'Creando...' : 'Crear Acampante'}
        </button>
      </div>

      {/* Mostrar errores */}
      {acampantesList.error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          Error: {acampantesList.error}
        </div>
      )}

      {createAcampante.error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          Error al crear: {createAcampante.error}
        </div>
      )}

      {deleteAcampante.error && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          Error al eliminar: {deleteAcampante.error}
        </div>
      )}

      {/* Loading state */}
      {acampantesList.loading && (
        <div className="flex justify-center items-center py-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      )}      {/* Lista de acampantes */}
      <div className="grid gap-4">
        {acampantesList.items.map((acampante: Acampante) => (
          <div key={acampante.id} className="p-4 border border-gray-200 rounded-lg shadow">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-semibold text-lg">{acampante.nombreCompleto}</h3>
                <p className="text-gray-600">Edad: {acampante.edad}</p>
                <p className="text-gray-600">
                  Contacto de emergencia: {acampante.contactoEmergenciaNombre} - {acampante.contactoEmergenciaTelefono}
                </p>
              </div>
              <button
                onClick={() => handleDelete(acampante.id)}
                disabled={deleteAcampante.loading}
                className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
              >
                {deleteAcampante.loading ? 'Eliminando...' : 'Eliminar'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Información de paginación */}
      {acampantesList.pagination.total > 0 && (
        <div className="mt-6 text-sm text-gray-600">
          Mostrando {acampantesList.items.length} de {acampantesList.pagination.total} acampantes
        </div>
      )}
    </div>
  );
};

export default AcampantesExample;
