-- ==========================================
-- MIGRACIÓN: Tabla configuracion_sistema
-- Para sistema de asignación automática Round-Robin
-- ==========================================

-- Crear tabla de configuración del sistema
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500),
    descripcion VARCHAR(200),
    actualizado_en TIMESTAMPTZ
);

-- Crear índice en la columna clave para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_configuracion_sistema_clave ON configuracion_sistema(clave);

-- Insertar configuración inicial para round-robin (opcional, se crea automáticamente)
INSERT INTO configuracion_sistema (clave, valor, descripcion, actualizado_en)
VALUES (
    'ROUND_ROBIN_ULTIMO_INSPECTOR_ID',
    NULL,
    'ID del último inspector asignado en el sistema round-robin de querellas',
    NOW()
)
ON CONFLICT (clave) DO NOTHING;

-- Comentarios en la tabla
COMMENT ON TABLE configuracion_sistema IS 'Almacena configuraciones generales del sistema';
COMMENT ON COLUMN configuracion_sistema.clave IS 'Identificador único de la configuración (ej: ROUND_ROBIN_ULTIMO_INSPECTOR_ID)';
COMMENT ON COLUMN configuracion_sistema.valor IS 'Valor de la configuración como string (puede ser ID, número, JSON, etc.)';
COMMENT ON COLUMN configuracion_sistema.descripcion IS 'Descripción legible de para qué sirve la configuración';
COMMENT ON COLUMN configuracion_sistema.actualizado_en IS 'Fecha y hora de la última actualización de esta configuración';
