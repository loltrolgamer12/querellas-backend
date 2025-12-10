-- ========================================
-- MIGRACIÓN COMPLETA DEL SISTEMA DE QUERELLAS
-- Fecha: 2025-12-10
-- ========================================

-- IMPORTANTE: Hacer backup de la base de datos antes de ejecutar este script

-- ========================================
-- PASO 1: CREAR NUEVAS TABLAS
-- ========================================

-- Tabla corregimiento
CREATE TABLE IF NOT EXISTS corregimiento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- Tabla barrio (con relación a comuna)
CREATE TABLE IF NOT EXISTS barrio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    comuna_id BIGINT REFERENCES comuna(id),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- Tabla despacho_comisorio
CREATE TABLE IF NOT EXISTS despacho_comisorio (
    id BIGSERIAL PRIMARY KEY,
    fecha_recibido DATE NOT NULL,
    radicado_proceso VARCHAR(100) NOT NULL,
    numero_despacho VARCHAR(100) NOT NULL,
    entidad_procedente VARCHAR(300) NOT NULL,
    asunto VARCHAR(1000) NOT NULL,
    demandante_apoderado VARCHAR(500),
    corregimiento_id BIGINT REFERENCES corregimiento(id),
    fecha_devolucion DATE,
    creado_por BIGINT,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- ========================================
-- PASO 2: MODIFICAR TABLAS EXISTENTES
-- ========================================

-- Agregar columna corregimiento_id a usuarios
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS corregimiento_id BIGINT REFERENCES corregimiento(id);

-- Agregar columnas barrio_id y corregimiento_id a querella
ALTER TABLE querella ADD COLUMN IF NOT EXISTS barrio_id BIGINT REFERENCES barrio(id);
ALTER TABLE querella ADD COLUMN IF NOT EXISTS corregimiento_id BIGINT REFERENCES corregimiento(id);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_barrio_comuna ON barrio(comuna_id);
CREATE INDEX IF NOT EXISTS idx_usuario_corregimiento ON usuarios(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_querella_barrio ON querella(barrio_id);
CREATE INDEX IF NOT EXISTS idx_querella_corregimiento ON querella(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_despacho_corregimiento ON despacho_comisorio(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_recibido ON despacho_comisorio(fecha_recibido);
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_devolucion ON despacho_comisorio(fecha_devolucion);

-- ========================================
-- PASO 3: ACTUALIZAR ESTADOS DE QUERELLAS
-- ========================================

-- Eliminar transiciones antiguas
DELETE FROM estado_transicion WHERE modulo = 'QUERELLA';

-- Eliminar estados antiguos (esto también eliminará el historial)
-- PRECAUCIÓN: Comentar esta línea si se desea conservar el historial
-- DELETE FROM historial_estado WHERE modulo = 'QUERELLA';
-- DELETE FROM estado WHERE modulo = 'QUERELLA';

-- Insertar nuevos estados para QUERELLA
INSERT INTO estado (modulo, nombre, creado_en) VALUES
('QUERELLA', 'APERTURA', now()),
('QUERELLA', 'NOTIFICACION', now()),
('QUERELLA', 'AUDIENCIA_PUBLICA', now()),
('QUERELLA', 'DECISION', now()),
('QUERELLA', 'RECURSO', now()),
('QUERELLA', 'INADMISIBLE', now())
ON CONFLICT (modulo, nombre) DO NOTHING;

-- Insertar transiciones permitidas para QUERELLA
INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA')
    AND hacia_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
);

INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'INADMISIBLE')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA')
    AND hacia_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'INADMISIBLE')
);

INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
    AND hacia_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
);

INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
    AND hacia_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
);

INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECURSO')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
    AND hacia_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECURSO')
);

-- ========================================
-- PASO 4: CARGAR CATÁLOGOS DE NEIVA
-- ========================================

\i catalogos-neiva.sql

-- ========================================
-- FIN DE LA MIGRACIÓN
-- ========================================

-- Verificar que todo se creó correctamente
SELECT 'Corregimientos creados: ' || COUNT(*) FROM corregimiento;
SELECT 'Barrios creados: ' || COUNT(*) FROM barrio;
SELECT 'Comunas existentes: ' || COUNT(*) FROM comuna;
SELECT 'Estados QUERELLA: ' || COUNT(*) FROM estado WHERE modulo = 'QUERELLA';
SELECT 'Transiciones QUERELLA: ' || COUNT(*) FROM estado_transicion WHERE modulo = 'QUERELLA';

-- Mensaje de finalización
SELECT '✅ Migración completada exitosamente' as mensaje;
