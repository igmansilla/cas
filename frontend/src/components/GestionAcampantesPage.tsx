import React, { useEffect, useState } from 'react';
import { api, type Acampante, type AcampanteForm } from '../services/api';
import { useApiList, useApi } from '../hooks/useApi';

const GestionAcampantesPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [editingAcampante, setEditingAcampante] = useState<Acampante | null>(null);
  const [formData, setFormData] = useState<AcampanteForm>({
    nombreCompleto: '',
    edad: 0,
    contactoEmergenciaNombre: '',
    contactoEmergenciaTelefono: '',
  });

  // Hooks para API calls
  const acampantesList = useApiList<Acampante>(api.acampantes.getAll);
  const createAcampante = useApi(api.acampantes.create);
  const updateAcampante = useApi(api.acampantes.update);
  const deleteAcampante = useApi(api.acampantes.delete);  // Cargar acampantes al montar el componente
  useEffect(() => {
    acampantesList.execute();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const resetForm = () => {
    setFormData({
      nombreCompleto: '',
      edad: 0,
      contactoEmergenciaNombre: '',
      contactoEmergenciaTelefono: '',
    });
    setEditingAcampante(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    let result;
    if (editingAcampante) {
      result = await updateAcampante.execute(editingAcampante.id, formData);
    } else {
      result = await createAcampante.execute(formData);
    }

    if (result?.success) {
      resetForm();
      acampantesList.execute(); // Recargar lista
    }
  };

  const handleEdit = (acampante: Acampante) => {
    setFormData({
      nombreCompleto: acampante.nombreCompleto,
      edad: acampante.edad,
      contactoEmergenciaNombre: acampante.contactoEmergenciaNombre,
      contactoEmergenciaTelefono: acampante.contactoEmergenciaTelefono,
    });
    setEditingAcampante(acampante);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (confirm('¿Estás seguro de que quieres eliminar este acampante?')) {
      const result = await deleteAcampante.execute(id);
      if (result?.success) {
        acampantesList.execute(); // Recargar lista
      }
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'edad' ? parseInt(value) || 0 : value,
    }));
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Acampantes</h1>
        <button
          onClick={() => setShowForm(true)}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
        >
          Agregar Acampante
        </button>
      </div>

      {/* Mostrar errores */}
      {(acampantesList.error || createAcampante.error || updateAcampante.error || deleteAcampante.error) && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
          Error: {acampantesList.error || createAcampante.error || updateAcampante.error || deleteAcampante.error}
        </div>
      )}

      {/* Formulario Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">
              {editingAcampante ? 'Editar Acampante' : 'Agregar Acampante'}
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre Completo
                </label>
                <input
                  type="text"
                  name="nombreCompleto"
                  value={formData.nombreCompleto}
                  onChange={handleInputChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Edad
                </label>
                <input
                  type="number"
                  name="edad"
                  value={formData.edad}
                  onChange={handleInputChange}
                  required
                  min="1"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Contacto de Emergencia - Nombre
                </label>
                <input
                  type="text"
                  name="contactoEmergenciaNombre"
                  value={formData.contactoEmergenciaNombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Contacto de Emergencia - Teléfono
                </label>
                <input
                  type="tel"
                  name="contactoEmergenciaTelefono"
                  value={formData.contactoEmergenciaTelefono}
                  onChange={handleInputChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={resetForm}
                  className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={createAcampante.loading || updateAcampante.loading}
                  className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 transition-colors"
                >
                  {(createAcampante.loading || updateAcampante.loading) 
                    ? 'Guardando...' 
                    : editingAcampante ? 'Actualizar' : 'Crear'
                  }
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Loading state */}
      {acampantesList.loading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      )}

      {/* Lista de acampantes */}
      {!acampantesList.loading && (
        <div className="grid gap-4">
          {acampantesList.items.map((acampante) => (
            <div key={acampante.id} className="p-6 border border-gray-200 rounded-lg shadow-sm bg-white">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h3 className="font-semibold text-lg text-gray-800 mb-2">{acampante.nombreCompleto}</h3>
                  <div className="text-gray-600 space-y-1">
                    <p><span className="font-medium">Edad:</span> {acampante.edad} años</p>
                    <p><span className="font-medium">Contacto de emergencia:</span> {acampante.contactoEmergenciaNombre}</p>
                    <p><span className="font-medium">Teléfono:</span> {acampante.contactoEmergenciaTelefono}</p>
                  </div>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEdit(acampante)}
                    className="px-3 py-2 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition-colors"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleDelete(acampante.id)}
                    disabled={deleteAcampante.loading}
                    className="px-3 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 disabled:opacity-50 transition-colors"
                  >
                    {deleteAcampante.loading ? 'Eliminando...' : 'Eliminar'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Mensaje cuando no hay acampantes */}
      {!acampantesList.loading && acampantesList.items.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No hay acampantes registrados.</p>
          <button
            onClick={() => setShowForm(true)}
            className="mt-4 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
          >
            Agregar el primer acampante
          </button>
        </div>
      )}

      {/* Información de paginación */}
      {acampantesList.pagination.total > 0 && (
        <div className="mt-6 text-sm text-gray-600 text-center">
          Mostrando {acampantesList.items.length} de {acampantesList.pagination.total} acampantes
        </div>
      )}
    </div>
  );
};

export default GestionAcampantesPage;
