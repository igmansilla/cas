-- Tabla de reuniones
CREATE TABLE IF NOT EXISTS reuniones (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_reunion TIMESTAMP NOT NULL,
    lugar VARCHAR(255),
    es_obligatoria BOOLEAN DEFAULT FALSE,
    estado VARCHAR(20) DEFAULT 'PROGRAMADA' CHECK (estado IN ('PROGRAMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de asistencias
CREATE TABLE IF NOT EXISTS asistencias (
    id BIGSERIAL PRIMARY KEY,
    reunion_id BIGINT NOT NULL REFERENCES reuniones(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_asistencia VARCHAR(20) DEFAULT 'PRESENTE' CHECK (estado_asistencia IN ('PRESENTE', 'AUSENTE', 'TARDANZA', 'JUSTIFICADO')),
    hora_llegada TIMESTAMP,
    hora_salida TIMESTAMP,
    observaciones TEXT,
    registrado_por VARCHAR(255),
    UNIQUE(reunion_id, user_id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_reuniones_fecha ON reuniones(fecha_reunion);
CREATE INDEX IF NOT EXISTS idx_reuniones_estado ON reuniones(estado);
CREATE INDEX IF NOT EXISTS idx_reuniones_obligatoria ON reuniones(es_obligatoria);
CREATE INDEX IF NOT EXISTS idx_asistencias_reunion ON asistencias(reunion_id);
CREATE INDEX IF NOT EXISTS idx_asistencias_usuario ON asistencias(user_id);
CREATE INDEX IF NOT EXISTS idx_asistencias_estado ON asistencias(estado_asistencia);
CREATE INDEX IF NOT EXISTS idx_asistencias_fecha_registro ON asistencias(fecha_registro);

-- Función para actualizar la fecha de actualización automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar automáticamente la fecha de actualización en reuniones
DROP TRIGGER IF EXISTS update_reuniones_updated_at ON reuniones;
CREATE TRIGGER update_reuniones_updated_at
    BEFORE UPDATE ON reuniones
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comentarios en las tablas
COMMENT ON TABLE reuniones IS 'Tabla para almacenar información de las reuniones';
COMMENT ON TABLE asistencias IS 'Tabla para registrar la asistencia de usuarios a las reuniones';

COMMENT ON COLUMN reuniones.nombre IS 'Nombre descriptivo de la reunión';
COMMENT ON COLUMN reuniones.descripcion IS 'Descripción detallada de la reunión';
COMMENT ON COLUMN reuniones.fecha_reunion IS 'Fecha y hora programada para la reunión';
COMMENT ON COLUMN reuniones.lugar IS 'Lugar donde se realizará la reunión';
COMMENT ON COLUMN reuniones.es_obligatoria IS 'Indica si la asistencia a la reunión es obligatoria';
COMMENT ON COLUMN reuniones.estado IS 'Estado actual de la reunión (PROGRAMADA, EN_CURSO, FINALIZADA, CANCELADA)';

COMMENT ON COLUMN asistencias.reunion_id IS 'ID de la reunión a la que se refiere la asistencia';
COMMENT ON COLUMN asistencias.user_id IS 'ID del usuario que asiste o no a la reunión';
COMMENT ON COLUMN asistencias.fecha_registro IS 'Fecha y hora en que se registró la asistencia';
COMMENT ON COLUMN asistencias.estado_asistencia IS 'Estado de la asistencia (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)';
COMMENT ON COLUMN asistencias.hora_llegada IS 'Hora en que el usuario llegó a la reunión';
COMMENT ON COLUMN asistencias.hora_salida IS 'Hora en que el usuario se retiró de la reunión';
COMMENT ON COLUMN asistencias.observaciones IS 'Comentarios adicionales sobre la asistencia';
COMMENT ON COLUMN asistencias.registrado_por IS 'Usuario que registró la asistencia';
