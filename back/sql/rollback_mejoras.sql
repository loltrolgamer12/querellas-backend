-- =====================================================
-- ROLLBACK DE MEJORAS - BASE DE DATOS
-- Sistema de Querellas - Alcaldía de Neiva
-- =====================================================

-- ADVERTENCIA: Este script ELIMINA todas las mejoras aplicadas
-- Solo usar en caso de problemas graves
-- NO ejecutar en producción sin backup

BEGIN;

-- =====================================================
-- ROLLBACK FASE 2: BÚSQUEDA FULLTEXT
-- =====================================================

-- Eliminar trigger y función de tsvector
DROP TRIGGER IF EXISTS trigger_querella_tsvector ON querella;
DROP FUNCTION IF EXISTS actualizar_querella_tsvector();

-- Eliminar columna tsvector
ALTER TABLE querella DROP COLUMN IF EXISTS busqueda_tsvector;

-- =====================================================
-- ROLLBACK FASE 2: TRIGGERS DE ACTUALIZACIÓN AUTOMÁTICA
-- =====================================================

-- Eliminar triggers
DROP TRIGGER IF EXISTS trigger_actualizar_comunicaciones ON comunicaciones;
DROP TRIGGER IF EXISTS trigger_actualizar_adjuntos ON adjuntos;
DROP TRIGGER IF EXISTS trigger_actualizar_notificaciones ON notificaciones;
DROP TRIGGER IF EXISTS trigger_actualizar_tema ON tema;
DROP TRIGGER IF EXISTS trigger_actualizar_comuna ON comuna;
DROP TRIGGER IF EXISTS trigger_actualizar_usuarios ON usuarios;
DROP TRIGGER IF EXISTS trigger_actualizar_estado ON estado;
DROP TRIGGER IF EXISTS trigger_actualizar_estado_transicion ON estado_transicion;
DROP TRIGGER IF EXISTS trigger_actualizar_historial ON historial_estado;
DROP TRIGGER IF EXISTS trigger_actualizar_configuracion ON configuracion_sistema;

-- Eliminar función genérica
DROP FUNCTION IF EXISTS actualizar_timestamp_modificacion();

-- =====================================================
-- ROLLBACK FASE 1: ÍNDICES
-- =====================================================

-- Índices de querella
DROP INDEX IF EXISTS idx_querella_es_migrado;
DROP INDEX IF EXISTS idx_querella_archivado;
DROP INDEX IF EXISTS idx_querella_comuna_creado;
DROP INDEX IF EXISTS idx_querella_tiene_fallo;
DROP INDEX IF EXISTS idx_querella_busqueda_gin;

-- Índices de comunicaciones
DROP INDEX IF EXISTS idx_comunicaciones_fecha_envio;
DROP INDEX IF EXISTS idx_comunicaciones_querella_estado;
DROP INDEX IF EXISTS idx_comunicaciones_actualizado_en;

-- Índices de adjuntos
DROP INDEX IF EXISTS idx_adjuntos_cargado_creado;
DROP INDEX IF EXISTS idx_adjuntos_tamano;

-- Índices de notificaciones
DROP INDEX IF EXISTS idx_notificaciones_tipo;
DROP INDEX IF EXISTS idx_notificaciones_usuario_tipo_leida;

-- Índices de despacho_comisorio
DROP INDEX IF EXISTS idx_despacho_fecha_devolucion;
DROP INDEX IF EXISTS idx_despacho_inspector_fecha;
DROP INDEX IF EXISTS idx_despacho_creado_por;

-- Índices de historial_estado
DROP INDEX IF EXISTS idx_historial_usuario;
DROP INDEX IF EXISTS idx_historial_modulo_estado_fecha;

-- Índice de configuracion_sistema
DROP INDEX IF EXISTS idx_configuracion_sistema_clave;

-- =====================================================
-- ROLLBACK FASE 1: CAMPOS DE AUDITORÍA
-- =====================================================

-- ADVERTENCIA: Esto ELIMINA datos de auditoría
-- Solo hacer si absolutamente necesario

-- Tabla: querella
ALTER TABLE querella DROP CONSTRAINT IF EXISTS querella_actualizado_por_fk;
ALTER TABLE querella DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: despacho_comisorio
ALTER TABLE despacho_comisorio DROP CONSTRAINT IF EXISTS despacho_actualizado_por_fk;
ALTER TABLE despacho_comisorio DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: comunicaciones
ALTER TABLE comunicaciones DROP CONSTRAINT IF EXISTS comunicaciones_actualizado_por_fk;
ALTER TABLE comunicaciones DROP COLUMN IF EXISTS actualizado_por;
ALTER TABLE comunicaciones DROP COLUMN IF EXISTS actualizado_en;

-- Tabla: adjuntos
ALTER TABLE adjuntos DROP CONSTRAINT IF EXISTS adjuntos_actualizado_por_fk;
ALTER TABLE adjuntos DROP COLUMN IF EXISTS actualizado_por;
ALTER TABLE adjuntos DROP COLUMN IF EXISTS actualizado_en;

-- Tabla: notificaciones
ALTER TABLE notificaciones DROP CONSTRAINT IF EXISTS notificaciones_creado_por_fk;
ALTER TABLE notificaciones DROP CONSTRAINT IF EXISTS notificaciones_actualizado_por_fk;
ALTER TABLE notificaciones DROP COLUMN IF EXISTS creado_por;
ALTER TABLE notificaciones DROP COLUMN IF EXISTS actualizado_por;
ALTER TABLE notificaciones DROP COLUMN IF EXISTS actualizado_en;

-- Tabla: tema
ALTER TABLE tema DROP CONSTRAINT IF EXISTS tema_creado_por_fk;
ALTER TABLE tema DROP CONSTRAINT IF EXISTS tema_actualizado_por_fk;
ALTER TABLE tema DROP COLUMN IF EXISTS creado_por;
ALTER TABLE tema DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: comuna
ALTER TABLE comuna DROP CONSTRAINT IF EXISTS comuna_creado_por_fk;
ALTER TABLE comuna DROP CONSTRAINT IF EXISTS comuna_actualizado_por_fk;
ALTER TABLE comuna DROP COLUMN IF EXISTS creado_por;
ALTER TABLE comuna DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: usuarios
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_creado_por_fk;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_actualizado_por_fk;
ALTER TABLE usuarios DROP COLUMN IF EXISTS creado_por;
ALTER TABLE usuarios DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: estado
ALTER TABLE estado DROP CONSTRAINT IF EXISTS estado_creado_por_fk;
ALTER TABLE estado DROP CONSTRAINT IF EXISTS estado_actualizado_por_fk;
ALTER TABLE estado DROP COLUMN IF EXISTS creado_por;
ALTER TABLE estado DROP COLUMN IF EXISTS actualizado_por;

-- Tabla: estado_transicion
ALTER TABLE estado_transicion DROP CONSTRAINT IF EXISTS estado_transicion_creado_por_fk;
ALTER TABLE estado_transicion DROP CONSTRAINT IF EXISTS estado_transicion_actualizado_por_fk;
ALTER TABLE estado_transicion DROP COLUMN IF EXISTS creado_por;
ALTER TABLE estado_transicion DROP COLUMN IF EXISTS creado_en;
ALTER TABLE estado_transicion DROP COLUMN IF EXISTS actualizado_por;
ALTER TABLE estado_transicion DROP COLUMN IF EXISTS actualizado_en;

-- Tabla: historial_estado
ALTER TABLE historial_estado DROP CONSTRAINT IF EXISTS historial_estado_actualizado_por_fk;
ALTER TABLE historial_estado DROP COLUMN IF EXISTS actualizado_por;
ALTER TABLE historial_estado DROP COLUMN IF EXISTS actualizado_en;

-- =====================================================
-- ROLLBACK FASE 1: TABLA CONFIGURACION_SISTEMA
-- =====================================================

-- ADVERTENCIA: Esto elimina la configuración de round-robin
DROP TABLE IF EXISTS configuracion_sistema CASCADE;

-- =====================================================
-- ROLLBACK FASE 1: CONSTRAINT DE USUARIOS.ROL
-- =====================================================

-- Volver al constraint incorrecto (por si se necesita rollback completo)
-- NOTA: Esto puede causar problemas, considerar bien antes de ejecutar
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('INSPECTOR', 'DIRECTOR', 'AUXILIAR'));

COMMIT;

-- =====================================================
-- MENSAJE FINAL
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '⚠️  Rollback completado';
    RAISE NOTICE '❌ Todas las mejoras han sido revertidas';
    RAISE NOTICE '📊 Verificar estado de la base de datos';
    RAISE NOTICE '💾 Restaurar desde backup si es necesario';
END $$;
