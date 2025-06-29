import React, { useEffect, useState } from 'react';
// Update imports: Remove AcampanteForm if NewAcampanteCreateRequest replaces its use for the form.
// Keep Acampante for the list type for now, or change to NewAcampanteResponse.
import {
  api,
  type Acampante, // This is the old Acampante type for the list.
                  // Consider fetching acampantes linked to the dirigente in the future.
  type NewAcampanteCreateRequest, // New
  type NewAcampanteResponse      // New
} from '../../services/api';
import { useApiList, useApi } from '../../hooks/useApi'; // Assuming useApi can take the new request/response types

const GestionAcampantesPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  // Editing state might need to be updated if we allow editing of new acampante type properties
  const [editingAcampante, setEditingAcampante] = useState<Acampante | null>(null); // Keep Acampante for now for edit form

  // Update formData state to use NewAcampanteCreateRequest
  const [formData, setFormData] = useState<NewAcampanteCreateRequest>({
    username: '',
    password: '', // Optional field
    nombreCompleto: '',
    edad: 0,
    contactoEmergenciaNombre: '',
    contactoEmergenciaTelefono: '',
  });

  // Hooks para API calls
  // The list currently fetches from /api/acampantes. This might need to change in the future.
  const acampantesList = useApiList<Acampante>(api.acampantes.getAll);

  // Use the new API method for creating acampantes via Dirigente endpoint
  const createAcampanteViaDirigente = useApi<NewAcampanteResponse, [NewAcampanteCreateRequest]>(
    api.dirigentes.createAcampanteByDirigente
  );

  // Update and Delete still point to old /api/acampantes endpoints.
  // This is fine for now as the task focuses on adding.
  const updateAcampanteApi = useApi(api.acampantes.update);
  const deleteAcampanteApi = useApi(api.acampantes.delete);

  useEffect(() => {
    acampantesList.execute();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const resetForm = () => {
    setFormData({
      username: '',
      password: '',
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
      // For editing, we'd need to map formData to the AcampanteForm or whatever the update endpoint expects.
      // The current editingAcampante is of type Acampante (old type).
      // For simplicity, let's assume the fields for update are compatible for now.
      const updateData = { // Map to AcampanteForm if necessary
          nombreCompleto: formData.nombreCompleto,
          edad: formData.edad,
          contactoEmergenciaNombre: formData.contactoEmergenciaNombre,
          contactoEmergenciaTelefono: formData.contactoEmergenciaTelefono,
      };
      result = await updateAcampanteApi.execute(editingAcampante.id, updateData);
    } else {
      // Use the new create method
      // Password is optional, if empty, backend should handle it
      const dataToSend: NewAcampanteCreateRequest = { ...formData };
      if (!dataToSend.password?.trim()) {
        delete dataToSend.password; // Remove password if it's empty or just whitespace
      }
      result = await createAcampanteViaDirigente.execute(dataToSend);
    }

    if (result?.success) {
      resetForm();
      acampantesList.execute(); // Recargar lista (still from /api/acampantes)
      // TODO: Consider if this list should show the newly created acampante user.
      // If acampantesList.execute() fetches from /api/acampantes (old system),
      // the new user (created via /api/dirigentes/me/acampantes) might not appear unless
      // that old endpoint also lists users created the new way OR this list is changed
      // to fetch acampantes supervised by the dirigente.
    }
  };

  // handleEdit needs to populate all fields of NewAcampanteCreateRequest if the form is shared.
  // However, `username` and `password` are not part of the `Acampante` type from the list.
  // For now, editing will only populate the old fields.
  const handleEdit = (acampante: Acampante) => {
    setFormData({
      username: '', // Username cannot be edited here, or fetched from existing acampante if it had one
      password: '', // Password is not for editing
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
      const result = await deleteAcampanteApi.execute(id);
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

  // The form needs to be updated with Username and Password fields
  // ... (JSX structure below will be updated) ...
  return (
    <div className="p-6">
      {/* ... (Title and Add button) ... */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Acampantes (Dirigente)</h1>
        <button
          onClick={() => {
            setEditingAcampante(null); // Ensure we are in "create" mode
            resetForm(); // Reset with new fields
            setShowForm(true);
          }}
          className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
        >
          Registrar Nuevo Acampante
        </button>
      </div>

      {/* Mostrar errores */}
      {(acampantesList.error || createAcampanteViaDirigente.error || updateAcampanteApi.error || deleteAcampanteApi.error) && (
        <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
          Error: {acampantesList.error || createAcampanteViaDirigente.error || updateAcampanteApi.error || deleteAcampanteApi.error}
        </div>
      )}

      {/* Formulario Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg w-full max-w-md overflow-y-auto max-h-[90vh]">
            <h2 className="text-xl font-bold mb-4">
              {editingAcampante ? 'Editar Acampante (Info Básica)' : 'Registrar Nuevo Acampante'}
            </h2>
            <form onSubmit={handleSubmit}>
              {/* Fields for NewAcampanteCreateRequest */}
              {!editingAcampante && ( // Username and Password only for new acampantes
                <>
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Username (para la cuenta del acampante)
                    </label>
                    <input
                      type="text"
                      name="username"
                      value={formData.username}
                      onChange={handleInputChange}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Password (opcional, se generará si se deja vacío)
                    </label>
                    <input
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </>
              )}
              {/* Common fields */}
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre Completo del Acampante
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
                  disabled={createAcampanteViaDirigente.loading || updateAcampanteApi.loading}
                  className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 transition-colors"
                >
                  {(createAcampanteViaDirigente.loading || updateAcampanteApi.loading)
                    ? 'Guardando...' 
                    : editingAcampante ? 'Actualizar Info Básica' : 'Registrar Acampante'
                  }
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ... (Loading state and List rendering - keeping it as is for now) ... */}
      {/* Loading state */}
      {acampantesList.loading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      )}

      {/* Lista de acampantes */}
      {!acampantesList.loading && acampantesList.items.length > 0 && (
        <div className="mt-8">
          <h2 className="text-2xl font-semibold text-gray-700 mb-4">Lista de Acampantes (desde /api/acampantes)</h2>
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
                      {/* If acampante is NewAcampanteResponse and has username */}
                      {/* {acampante.username && <p><span className="font-medium">Username:</span> {acampante.username}</p>} */}
                    </div>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleEdit(acampante)}
                      className="px-3 py-2 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition-colors"
                    >
                      Editar (Básico)
                    </button>
                    <button
                      onClick={() => handleDelete(acampante.id)}
                      disabled={deleteAcampanteApi.loading}
                      className="px-3 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 disabled:opacity-50 transition-colors"
                    >
                      {deleteAcampanteApi.loading ? 'Eliminando...' : 'Eliminar'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
       {!acampantesList.loading && acampantesList.items.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No hay acampantes registrados en la lista de /api/acampantes.</p>
        </div>
      )}
      {/* ... (Pagination - keeping as is) ... */}
       {acampantesList.pagination.total > 0 && (
        <div className="mt-6 text-sm text-gray-600 text-center">
          Mostrando {acampantesList.items.length} de {acampantesList.pagination.total} acampantes (desde /api/acampantes)
        </div>
      )}
    </div>
  );
};

export default GestionAcampantesPage;
