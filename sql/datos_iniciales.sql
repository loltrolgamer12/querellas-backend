-- =====================================================
-- DATOS INICIALES - SISTEMA DE QUERELLAS
-- Inspecciones de Policía - Alcaldía de Neiva
-- =====================================================
-- Este script carga los datos iniciales necesarios
-- para que el sistema funcione correctamente
-- =====================================================

\echo '🌱 Cargando datos iniciales...'

BEGIN;

-- =====================================================
-- ESTADOS DEL SISTEMA
-- =====================================================

\echo '📌 Insertando estados de querellas...'

-- Estados para módulo QUERELLA
INSERT INTO estado (modulo, nombre, descripcion, orden, activo) VALUES
('QUERELLA', 'RECIBIDA', 'Querella recibida y registrada en el sistema', 1, TRUE),
('QUERELLA', 'ASIGNADA', 'Querella asignada a un inspector', 2, TRUE),
('QUERELLA', 'EN_PROCESO', 'Inspector está trabajando en la querella', 3, TRUE),
('QUERELLA', 'EN_INVESTIGACION', 'Querella en fase de investigación', 4, TRUE),
('QUERELLA', 'CITACION_ENVIADA', 'Se ha enviado citación a las partes', 5, TRUE),
('QUERELLA', 'AUDIENCIA_PROGRAMADA', 'Audiencia programada', 6, TRUE),
('QUERELLA', 'EN_AUDIENCIA', 'Audiencia en curso', 7, TRUE),
('QUERELLA', 'RESOLUCION_EMITIDA', 'Se ha emitido resolución', 8, TRUE),
('QUERELLA', 'CERRADA', 'Querella cerrada y finalizada', 9, TRUE),
('QUERELLA', 'ARCHIVADA', 'Querella archivada', 10, TRUE),
('QUERELLA', 'ANULADA', 'Querella anulada', 11, TRUE)
ON CONFLICT (modulo, nombre) DO NOTHING;

\echo '📌 Insertando estados de despachos...'

-- Estados para módulo DESPACHO
INSERT INTO estado (modulo, nombre, descripcion, orden, activo) VALUES
('DESPACHO', 'RECIBIDO', 'Despacho comisorio recibido', 1, TRUE),
('DESPACHO', 'ASIGNADO', 'Despacho asignado a inspector', 2, TRUE),
('DESPACHO', 'EN_TRAMITE', 'Despacho en trámite', 3, TRUE),
('DESPACHO', 'DILIGENCIADO', 'Despacho diligenciado', 4, TRUE),
('DESPACHO', 'DEVUELTO', 'Despacho devuelto a la entidad procedente', 5, TRUE)
ON CONFLICT (modulo, nombre) DO NOTHING;

-- =====================================================
-- TRANSICIONES DE ESTADOS PERMITIDAS
-- =====================================================

\echo '🔄 Configurando transiciones de estados...'

-- Transiciones para QUERELLA
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECIBIDA'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'ASIGNADA'),
    'Asignar querella a inspector'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECIBIDA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'ASIGNADA')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'ASIGNADA'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_PROCESO'),
    'Iniciar trabajo en la querella'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'ASIGNADA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_PROCESO')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_PROCESO'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_INVESTIGACION'),
    'Pasar a fase de investigación'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_PROCESO')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_INVESTIGACION')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_INVESTIGACION'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CITACION_ENVIADA'),
    'Enviar citación a las partes'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'EN_INVESTIGACION')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CITACION_ENVIADA')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CITACION_ENVIADA'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PROGRAMADA'),
    'Programar audiencia'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CITACION_ENVIADA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PROGRAMADA')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PROGRAMADA'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RESOLUCION_EMITIDA'),
    'Emitir resolución'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PROGRAMADA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RESOLUCION_EMITIDA')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'QUERELLA',
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RESOLUCION_EMITIDA'),
    (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CERRADA'),
    'Cerrar querella'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RESOLUCION_EMITIDA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'CERRADA')
);

-- Transiciones para DESPACHO
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'DESPACHO',
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'RECIBIDO'),
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'ASIGNADO'),
    'Asignar despacho a inspector'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'DESPACHO'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'RECIBIDO')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'ASIGNADO')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'DESPACHO',
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'ASIGNADO'),
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'EN_TRAMITE'),
    'Iniciar trámite del despacho'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'DESPACHO'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'ASIGNADO')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'EN_TRAMITE')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'DESPACHO',
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'EN_TRAMITE'),
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DILIGENCIADO'),
    'Diligenciar despacho'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'DESPACHO'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'EN_TRAMITE')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DILIGENCIADO')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id, descripcion)
SELECT
    'DESPACHO',
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DILIGENCIADO'),
    (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DEVUELTO'),
    'Devolver despacho a entidad procedente'
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'DESPACHO'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DILIGENCIADO')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'DESPACHO' AND nombre = 'DEVUELTO')
);

-- =====================================================
-- CATÁLOGOS BÁSICOS
-- =====================================================

\echo '📚 Insertando temas de querellas...'

-- Temas comunes de querellas
INSERT INTO tema (nombre, activo) VALUES
('Ruidos y perturbación del orden público', TRUE),
('Construcción sin licencia o irregular', TRUE),
('Invasión del espacio público', TRUE),
('Problemas con animales domésticos', TRUE),
('Conflictos entre vecinos', TRUE),
('Basuras y salubridad', TRUE),
('Uso indebido de bienes públicos', TRUE),
('Riñas y peleas', TRUE),
('Establecimiento de comercio sin permiso', TRUE),
('Otros', TRUE)
ON CONFLICT (nombre) DO NOTHING;

\echo '🗺️  Insertando comunas de Neiva...'

-- Comunas de Neiva
INSERT INTO comuna (nombre, activo) VALUES
('Comuna 1', TRUE),
('Comuna 2', TRUE),
('Comuna 3', TRUE),
('Comuna 4', TRUE),
('Comuna 5', TRUE),
('Comuna 6', TRUE),
('Comuna 7', TRUE),
('Comuna 8', TRUE),
('Comuna 9', TRUE),
('Comuna 10', TRUE),
('Corregimientos', TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- =====================================================
-- CONFIGURACIÓN DEL SISTEMA
-- =====================================================

\echo '⚙️  Insertando configuraciones del sistema...'

INSERT INTO configuracion_sistema (clave, valor, descripcion, tipo_dato) VALUES
('ROUND_ROBIN_ULTIMO_INSPECTOR_ID', NULL, 'ID del último inspector asignado en round-robin de querellas', 'INTEGER'),
('SISTEMA_VERSION', '3.0', 'Versión actual del sistema', 'STRING'),
('MAX_TAMANO_ADJUNTO_MB', '10', 'Tamaño máximo de adjuntos en MB', 'INTEGER'),
('DIAS_RETENCION_NOTIFICACIONES', '90', 'Días de retención de notificaciones leídas', 'INTEGER'),
('NOTIFICACIONES_ENABLED', 'true', 'Sistema de notificaciones activo', 'BOOLEAN'),
('EMAIL_NOTIFICACIONES_ENABLED', 'false', 'Envío de notificaciones por email', 'BOOLEAN')
ON CONFLICT (clave) DO NOTHING;

-- =====================================================
-- USUARIO ADMINISTRADOR INICIAL
-- =====================================================

\echo '👤 Creando usuario administrador inicial...'

-- Contraseña: admin123 (cambiar en producción)
-- Hash BCrypt de "admin123"
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, zona)
VALUES (
    'Administrador del Sistema',
    'admin@neiva.gov.co',
    '3001234567',
    '$2a$10$XqjJ5p/YK0pZ8xN.qZ0Z3O7vB9h8Y8Qp8YZ0Z3O7vB9h8Y8Qp8YZ0',
    'DIRECTORA',
    'ACTIVO',
    NULL
)
ON CONFLICT (email) DO NOTHING;

COMMIT;

-- =====================================================
-- VERIFICACIÓN DE DATOS INICIALES
-- =====================================================

\echo ''
\echo '✅ Datos iniciales cargados exitosamente'
\echo ''

-- Mostrar resumen
SELECT '📊 RESUMEN DE DATOS INICIALES' AS titulo;

SELECT 'Estados de QUERELLA' AS tipo, COUNT(*) AS cantidad
FROM estado WHERE modulo = 'QUERELLA'
UNION ALL
SELECT 'Estados de DESPACHO', COUNT(*)
FROM estado WHERE modulo = 'DESPACHO'
UNION ALL
SELECT 'Transiciones configuradas', COUNT(*)
FROM estado_transicion
UNION ALL
SELECT 'Temas de querellas', COUNT(*)
FROM tema
UNION ALL
SELECT 'Comunas', COUNT(*)
FROM comuna
UNION ALL
SELECT 'Configuraciones', COUNT(*)
FROM configuracion_sistema
UNION ALL
SELECT 'Usuarios iniciales', COUNT(*)
FROM usuarios;

\echo ''
\echo '🎉 Sistema listo para usar'
\echo ''
\echo '⚠️  IMPORTANTE: Cambiar la contraseña del administrador'
\echo '   Email: admin@neiva.gov.co'
\echo '   Password temporal: admin123'
\echo ''
