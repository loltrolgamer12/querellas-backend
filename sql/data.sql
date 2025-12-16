-- =====================================================
-- DATOS INICIALES - SISTEMA DE QUERELLAS
-- Inspecciones de Policía - Alcaldía de Neiva
-- Versión: 1.0
-- =====================================================

-- Configuración
SET client_encoding = 'UTF8';

-- =====================================================
-- 1. CATÁLOGOS BASE
-- =====================================================

-- Inspecciones de Policía de Neiva
INSERT INTO inspeccion (nombre, creado_en) VALUES
('Inspección Primera', NOW()),
('Inspección Segunda', NOW()),
('Inspección Tercera', NOW()),
('Inspección Cuarta', NOW()),
('Inspección Quinta', NOW()),
('Inspección Sexta', NOW()),
('Inspección Séptima', NOW())
ON CONFLICT DO NOTHING;

-- Comunas de Neiva
INSERT INTO comuna (nombre, creado_en) VALUES
('Comuna 1', NOW()),
('Comuna 2', NOW()),
('Comuna 3', NOW()),
('Comuna 4', NOW()),
('Comuna 5', NOW()),
('Comuna 6', NOW()),
('Comuna 7', NOW()),
('Comuna 8', NOW()),
('Comuna 9', NOW()),
('Comuna 10', NOW())
ON CONFLICT DO NOTHING;

-- Temas comunes de querellas
INSERT INTO tema (nombre, creado_en) VALUES
('Ruidos y Perturbación', NOW()),
('Construcción sin permiso', NOW()),
('Ocupación de espacio público', NOW()),
('Basuras y mal olor', NOW()),
('Riñas y peleas', NOW()),
('Daños a propiedad', NOW()),
('Maltrato animal', NOW()),
('Cerramiento de vías', NOW()),
('Contaminación ambiental', NOW()),
('Establecimientos comerciales', NOW()),
('Venta de licor sin autorización', NOW()),
('Menores en situación de riesgo', NOW()),
('Violencia intrafamiliar', NOW()),
('Incumplimiento de medidas sanitarias', NOW()),
('Otros', NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- 2. ESTADOS Y FLUJO DE QUERELLAS
-- =====================================================

-- Estados para módulo QUERELLA
INSERT INTO estado (modulo, nombre, creado_en) VALUES
('QUERELLA', 'RECIBIDA', NOW()),
('QUERELLA', 'ASIGNADA', NOW()),
('QUERELLA', 'EN_VERIFICACION', NOW()),
('QUERELLA', 'EN_PROCESO', NOW()),
('QUERELLA', 'NOTIFICACION_ENVIADA', NOW()),
('QUERELLA', 'AUDIENCIA_PROGRAMADA', NOW()),
('QUERELLA', 'EN_AUDIENCIA', NOW()),
('QUERELLA', 'RESUELTA', NOW()),
('QUERELLA', 'CERRADA', NOW()),
('QUERELLA', 'ARCHIVADA', NOW()),
('QUERELLA', 'DESISTIDA', NOW()),
('QUERELLA', 'REMITIDA', NOW())
ON CONFLICT (modulo, nombre) DO NOTHING;

-- Obtener IDs de estados para las transiciones
DO $$
DECLARE
    id_recibida BIGINT;
    id_asignada BIGINT;
    id_verificacion BIGINT;
    id_proceso BIGINT;
    id_notificacion BIGINT;
    id_audiencia_prog BIGINT;
    id_audiencia BIGINT;
    id_resuelta BIGINT;
    id_cerrada BIGINT;
    id_archivada BIGINT;
    id_desistida BIGINT;
    id_remitida BIGINT;
BEGIN
    -- Obtener IDs
    SELECT id INTO id_recibida FROM estado WHERE modulo='QUERELLA' AND nombre='RECIBIDA';
    SELECT id INTO id_asignada FROM estado WHERE modulo='QUERELLA' AND nombre='ASIGNADA';
    SELECT id INTO id_verificacion FROM estado WHERE modulo='QUERELLA' AND nombre='EN_VERIFICACION';
    SELECT id INTO id_proceso FROM estado WHERE modulo='QUERELLA' AND nombre='EN_PROCESO';
    SELECT id INTO id_notificacion FROM estado WHERE modulo='QUERELLA' AND nombre='NOTIFICACION_ENVIADA';
    SELECT id INTO id_audiencia_prog FROM estado WHERE modulo='QUERELLA' AND nombre='AUDIENCIA_PROGRAMADA';
    SELECT id INTO id_audiencia FROM estado WHERE modulo='QUERELLA' AND nombre='EN_AUDIENCIA';
    SELECT id INTO id_resuelta FROM estado WHERE modulo='QUERELLA' AND nombre='RESUELTA';
    SELECT id INTO id_cerrada FROM estado WHERE modulo='QUERELLA' AND nombre='CERRADA';
    SELECT id INTO id_archivada FROM estado WHERE modulo='QUERELLA' AND nombre='ARCHIVADA';
    SELECT id INTO id_desistida FROM estado WHERE modulo='QUERELLA' AND nombre='DESISTIDA';
    SELECT id INTO id_remitida FROM estado WHERE modulo='QUERELLA' AND nombre='REMITIDA';

    -- Insertar transiciones permitidas
    -- Desde RECIBIDA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_recibida, id_asignada),
    ('QUERELLA', id_recibida, id_archivada),
    ('QUERELLA', id_recibida, id_remitida)
    ON CONFLICT DO NOTHING;

    -- Desde ASIGNADA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_asignada, id_verificacion),
    ('QUERELLA', id_asignada, id_proceso),
    ('QUERELLA', id_asignada, id_archivada)
    ON CONFLICT DO NOTHING;

    -- Desde EN_VERIFICACION
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_verificacion, id_proceso),
    ('QUERELLA', id_verificacion, id_archivada),
    ('QUERELLA', id_verificacion, id_remitida)
    ON CONFLICT DO NOTHING;

    -- Desde EN_PROCESO
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_proceso, id_notificacion),
    ('QUERELLA', id_proceso, id_audiencia_prog),
    ('QUERELLA', id_proceso, id_resuelta),
    ('QUERELLA', id_proceso, id_archivada),
    ('QUERELLA', id_proceso, id_desistida)
    ON CONFLICT DO NOTHING;

    -- Desde NOTIFICACION_ENVIADA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_notificacion, id_audiencia_prog),
    ('QUERELLA', id_notificacion, id_proceso),
    ('QUERELLA', id_notificacion, id_desistida)
    ON CONFLICT DO NOTHING;

    -- Desde AUDIENCIA_PROGRAMADA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_audiencia_prog, id_audiencia),
    ('QUERELLA', id_audiencia_prog, id_proceso),
    ('QUERELLA', id_audiencia_prog, id_desistida)
    ON CONFLICT DO NOTHING;

    -- Desde EN_AUDIENCIA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_audiencia, id_resuelta),
    ('QUERELLA', id_audiencia, id_proceso),
    ('QUERELLA', id_audiencia, id_audiencia_prog)
    ON CONFLICT DO NOTHING;

    -- Desde RESUELTA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_resuelta, id_cerrada),
    ('QUERELLA', id_resuelta, id_proceso)
    ON CONFLICT DO NOTHING;

    -- Desde cualquier estado a ARCHIVADA (excepto CERRADA)
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_cerrada, id_archivada)
    ON CONFLICT DO NOTHING;

    -- Desde DESISTIDA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_desistida, id_archivada)
    ON CONFLICT DO NOTHING;

    -- Desde REMITIDA
    INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id) VALUES
    ('QUERELLA', id_remitida, id_archivada)
    ON CONFLICT DO NOTHING;

END $$;

-- =====================================================
-- 3. USUARIOS INICIALES DEL SISTEMA
-- =====================================================

-- IMPORTANTE: Todas las contraseñas son "demo123"
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- CAMBIAR CONTRASEÑAS EN PRODUCCIÓN

-- Usuario: Directora
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, inspeccion_id, creado_en)
VALUES (
    'María Elena Torres',
    'directora@inspecciones.neiva.gov.co',
    '3001234567',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'DIRECTOR',
    'ACTIVO',
    NULL,
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Usuario: Auxiliar Administrativa
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, inspeccion_id, creado_en)
VALUES (
    'Carolina Ramírez García',
    'auxiliar@inspecciones.neiva.gov.co',
    '3009876543',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'AUXILIAR',
    'ACTIVO',
    NULL,
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Inspectores por Inspección
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, inspeccion_id, creado_en)
SELECT
    'Inspector ' || i.nombre,
    'inspector' || i.id || '@inspecciones.neiva.gov.co',
    '300' || LPAD(i.id::TEXT, 7, '0'),
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'INSPECTOR',
    'ACTIVO',
    i.id,
    NOW()
FROM inspeccion i
ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- VERIFICACIÓN DE DATOS CARGADOS
-- =====================================================

-- Resumen de datos insertados
DO $$
DECLARE
    count_inspecciones INTEGER;
    count_comunas INTEGER;
    count_temas INTEGER;
    count_estados INTEGER;
    count_transiciones INTEGER;
    count_usuarios INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_inspecciones FROM inspeccion;
    SELECT COUNT(*) INTO count_comunas FROM comuna;
    SELECT COUNT(*) INTO count_temas FROM tema;
    SELECT COUNT(*) INTO count_estados FROM estado WHERE modulo='QUERELLA';
    SELECT COUNT(*) INTO count_transiciones FROM estado_transicion WHERE modulo='QUERELLA';
    SELECT COUNT(*) INTO count_usuarios FROM usuarios;

    RAISE NOTICE '========================================';
    RAISE NOTICE 'DATOS INICIALES CARGADOS EXITOSAMENTE';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Inspecciones: %', count_inspecciones;
    RAISE NOTICE 'Comunas: %', count_comunas;
    RAISE NOTICE 'Temas: %', count_temas;
    RAISE NOTICE 'Estados: %', count_estados;
    RAISE NOTICE 'Transiciones: %', count_transiciones;
    RAISE NOTICE 'Usuarios: %', count_usuarios;
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Credenciales de acceso:';
    RAISE NOTICE 'Usuario: directora@inspecciones.neiva.gov.co';
    RAISE NOTICE 'Contraseña: demo123';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'IMPORTANTE: Cambiar contraseñas en producción';
    RAISE NOTICE '========================================';
END $$;

-- =====================================================
-- FIN DE DATOS INICIALES
-- =====================================================
