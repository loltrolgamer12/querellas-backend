-- =====================================================
-- ESQUEMA: DESPACHOS COMISORIOS
-- Inspecciones de Policía - Alcaldía de Neiva
-- =====================================================

-- Tabla de Despachos Comisorios
CREATE TABLE IF NOT EXISTS despacho_comisorio (
    id BIGSERIAL PRIMARY KEY,

    -- Datos básicos del despacho
    fecha_recibido TIMESTAMP WITH TIME ZONE NOT NULL,
    radicado_proceso VARCHAR(50),
    numero_despacho VARCHAR(50) NOT NULL,
    entidad_procedente VARCHAR(255) NOT NULL,
    asunto TEXT NOT NULL,

    -- Partes involucradas
    demandante_apoderado TEXT,
    demandado_apoderado TEXT,

    -- Asignación
    inspector_asignado_id BIGINT,
    asignado_por BIGINT,

    -- Control
    fecha_devolucion TIMESTAMP WITH TIME ZONE,
    observaciones TEXT,

    -- Metadatos
    creado_por BIGINT,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT despacho_inspector_fk FOREIGN KEY (inspector_asignado_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT despacho_asignado_por_fk FOREIGN KEY (asignado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT despacho_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Índices para mejorar performance
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_recibido ON despacho_comisorio(fecha_recibido);
CREATE INDEX IF NOT EXISTS idx_despacho_inspector ON despacho_comisorio(inspector_asignado_id);
CREATE INDEX IF NOT EXISTS idx_despacho_numero ON despacho_comisorio(numero_despacho);
CREATE INDEX IF NOT EXISTS idx_despacho_entidad ON despacho_comisorio(entidad_procedente);

-- Historial de estados para despachos (opcional, similar a querellas)
-- Estados: RECIBIDO, ASIGNADO, EN_TRAMITE, DILIGENCIADO, DEVUELTO
INSERT INTO estado (modulo, nombre, creado_en) VALUES
('DESPACHO', 'RECIBIDO', NOW()),
('DESPACHO', 'ASIGNADO', NOW()),
('DESPACHO', 'EN_TRAMITE', NOW()),
('DESPACHO', 'DILIGENCIADO', NOW()),
('DESPACHO', 'DEVUELTO', NOW())
ON CONFLICT (modulo, nombre) DO NOTHING;

-- Secuencia para número de despacho interno (si se requiere)
CREATE SEQUENCE IF NOT EXISTS seq_numero_despacho START 1;

-- Comentarios
COMMENT ON TABLE despacho_comisorio IS 'Despachos comisorios recibidos de otras entidades judiciales';
COMMENT ON COLUMN despacho_comisorio.numero_despacho IS 'Número del despacho comisorio';
COMMENT ON COLUMN despacho_comisorio.entidad_procedente IS 'Juzgado o entidad que envía el despacho';
COMMENT ON COLUMN despacho_comisorio.fecha_devolucion IS 'Fecha en que se devolvió el despacho diligenciado';
