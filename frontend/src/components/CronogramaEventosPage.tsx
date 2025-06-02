import React, { useState } from 'react';
import { Calendar, Clock, MapPin, User, Plus, Edit3, Trash2 } from 'lucide-react';

interface Evento {
  id: number;
  titulo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  horaInicio: string;
  horaFin: string;
  ubicacion: string;
  responsable: string;
  tipo: 'actividad' | 'taller' | 'reunion' | 'libre';
}

const CronogramaEventosPage: React.FC = () => {
  // Datos de ejemplo (en una implementación real, estos vendrían de la API)
  const [eventos] = useState<Evento[]>([
    {
      id: 1,
      titulo: 'Ceremonia de Apertura',
      descripcion: 'Bienvenida e introducción al campamento',
      fechaInicio: '2025-07-15',
      fechaFin: '2025-07-15',
      horaInicio: '09:00',
      horaFin: '10:00',
      ubicacion: 'Anfiteatro Principal',
      responsable: 'Dirigente Principal',
      tipo: 'reunion'
    },
    {
      id: 2,
      titulo: 'Caminata Matutina',
      descripcion: 'Exploración del sendero norte del campamento',
      fechaInicio: '2025-07-15',
      fechaFin: '2025-07-15',
      horaInicio: '10:30',
      horaFin: '12:00',
      ubicacion: 'Sendero Norte',
      responsable: 'Guía de Montaña',
      tipo: 'actividad'
    },
    {
      id: 3,
      titulo: 'Taller de Nudos',
      descripcion: 'Aprendizaje de nudos básicos para campismo',
      fechaInicio: '2025-07-15',
      fechaFin: '2025-07-15',
      horaInicio: '14:00',
      horaFin: '15:30',
      ubicacion: 'Área de Talleres',
      responsable: 'Instructor Técnico',
      tipo: 'taller'
    },
    {
      id: 4,
      titulo: 'Tiempo Libre',
      descripcion: 'Actividades recreativas y descanso',
      fechaInicio: '2025-07-15',
      fechaFin: '2025-07-15',
      horaInicio: '15:30',
      horaFin: '17:00',
      ubicacion: 'Área Recreativa',
      responsable: 'Libre',
      tipo: 'libre'
    },
    {
      id: 5,
      titulo: 'Fogata Nocturna',
      descripcion: 'Historias, canciones y reflexiones alrededor del fuego',
      fechaInicio: '2025-07-15',
      fechaFin: '2025-07-15',
      horaInicio: '20:00',
      horaFin: '22:00',
      ubicacion: 'Área de Fogata',
      responsable: 'Animador Cultural',
      tipo: 'actividad'
    }
  ]);

  const [filtroTipo, setFiltroTipo] = useState<string>('todos');
  const [showAgregarEvento, setShowAgregarEvento] = useState(false);

  const getColorTipo = (tipo: string) => {
    switch (tipo) {
      case 'actividad': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'taller': return 'bg-green-100 text-green-800 border-green-200';
      case 'reunion': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'libre': return 'bg-gray-100 text-gray-800 border-gray-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getIconoTipo = (tipo: string) => {
    switch (tipo) {
      case 'actividad': return <Calendar className="w-4 h-4" />;
      case 'taller': return <Edit3 className="w-4 h-4" />;
      case 'reunion': return <User className="w-4 h-4" />;
      case 'libre': return <Clock className="w-4 h-4" />;
      default: return <Calendar className="w-4 h-4" />;
    }
  };

  const eventosFiltrados = filtroTipo === 'todos' 
    ? eventos 
    : eventos.filter(evento => evento.tipo === filtroTipo);

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Cronograma de Eventos</h1>
          <p className="text-gray-600 mt-1">Planificación y organización de actividades del campamento</p>
        </div>
        <button
          onClick={() => setShowAgregarEvento(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
        >
          <Plus className="w-4 h-4" />
          Agregar Evento
        </button>
      </div>

      {/* Filtros */}
      <div className="mb-6">
        <div className="flex flex-wrap gap-2">
          {['todos', 'actividad', 'taller', 'reunion', 'libre'].map((tipo) => (
            <button
              key={tipo}
              onClick={() => setFiltroTipo(tipo)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                filtroTipo === tipo
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {tipo === 'todos' ? 'Todos' : tipo.charAt(0).toUpperCase() + tipo.slice(1)}
            </button>
          ))}
        </div>
      </div>

      {/* Lista de Eventos */}
      <div className="space-y-4">
        {eventosFiltrados.length === 0 ? (
          <div className="text-center py-12">
            <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">No hay eventos para mostrar</p>
            <p className="text-gray-400">
              {filtroTipo === 'todos' 
                ? 'Agrega tu primer evento para comenzar'
                : `No hay eventos de tipo "${filtroTipo}"`
              }
            </p>
          </div>
        ) : (
          eventosFiltrados.map((evento) => (
            <div key={evento.id} className="bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
              <div className="p-6">
                <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-start gap-3 mb-3">
                      <div className={`flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium border ${getColorTipo(evento.tipo)}`}>
                        {getIconoTipo(evento.tipo)}
                        {evento.tipo.charAt(0).toUpperCase() + evento.tipo.slice(1)}
                      </div>
                    </div>
                    
                    <h3 className="text-xl font-semibold text-gray-800 mb-2">{evento.titulo}</h3>
                    <p className="text-gray-600 mb-4">{evento.descripcion}</p>
                    
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-sm text-gray-600">
                      <div className="flex items-center gap-2">
                        <Calendar className="w-4 h-4 text-gray-400" />
                        <span>{new Date(evento.fechaInicio).toLocaleDateString('es-ES')}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Clock className="w-4 h-4 text-gray-400" />
                        <span>{evento.horaInicio} - {evento.horaFin}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-gray-400" />
                        <span>{evento.ubicacion}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <User className="w-4 h-4 text-gray-400" />
                        <span>{evento.responsable}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-2 lg:flex-col lg:gap-2">
                    <button className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit3 className="w-4 h-4" />
                    </button>
                    <button className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal para agregar evento (placeholder) */}
      {showAgregarEvento && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Agregar Nuevo Evento</h2>
            <p className="text-gray-600 mb-4">
              Funcionalidad en desarrollo. Próximamente podrás crear y editar eventos directamente desde aquí.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowAgregarEvento(false)}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={() => setShowAgregarEvento(false)}
                className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
              >
                Entendido
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Estadísticas */}
      <div className="mt-8 grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <div className="text-2xl font-bold text-blue-600">{eventos.length}</div>
          <div className="text-sm text-gray-600">Total Eventos</div>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <div className="text-2xl font-bold text-green-600">
            {eventos.filter(e => e.tipo === 'actividad').length}
          </div>
          <div className="text-sm text-gray-600">Actividades</div>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <div className="text-2xl font-bold text-purple-600">
            {eventos.filter(e => e.tipo === 'taller').length}
          </div>
          <div className="text-sm text-gray-600">Talleres</div>
        </div>
        <div className="bg-white p-4 rounded-lg border border-gray-200 text-center">
          <div className="text-2xl font-bold text-orange-600">
            {eventos.filter(e => e.tipo === 'reunion').length}
          </div>
          <div className="text-sm text-gray-600">Reuniones</div>
        </div>
      </div>
    </div>
  );
};

export default CronogramaEventosPage;
