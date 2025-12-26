-- =====================================================
-- SCRIPT DE MEJORAS - BASE DE DATOS
-- Sistema de Querellas - Alcaldía de Neiva
-- Fecha: 2025-12-22
-- Autor: Claude Code - Auditoría Completa
-- =====================================================

-- IMPORTANTE: Revisar AUDITORIA_BASE_DATOS.md antes de ejecutar

BEGIN;

-- =====================================================
-- FASE 1: CORRECCIONES CRÍTICAS
-- =====================================================

-- 1. CORREGIR CONSTRAINT INCORRECTO EN USUARIOS.ROL
-- ❌ Actual tiene 'DIRECTOR', debería ser 'DIRECTORA'
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('INSPECTOR', 'DIRECTORA', 'AUXILIAR'));

COMMENT ON CONSTRAINT usuarios_rol_check ON usuarios IS 'Valores permitidos: INSPECTOR, DIRECTORA, AUXILIAR';

-- 2. AGREGAR CAMPOS DE AUDITORÍA FALTANTES

-- 2.1 Tabla: comunicaciones
ALTER TABLE comunicaciones
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE;

ALTER TABLE comunicaciones
    ADD CONSTRAINT IF NOT EXISTS comunicaciones_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN comunicaciones.actualizado_por IS 'Usuario que realizó la última modificación';
COMMENT ON COLUMN comunicaciones.actualizado_en IS 'Fecha y hora de la última modificación';

-- 2.2 Tabla: adjuntos
ALTER TABLE adjuntos
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE;

ALTER TABLE adjuntos
    ADD CONSTRAINT IF NOT EXISTS adjuntos_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN adjuntos.actualizado_por IS 'Usuario que realizó la última modificación';
COMMENT ON COLUMN adjuntos.actualizado_en IS 'Fecha y hora de la última modificación';

-- 2.3 Tabla: notificaciones
ALTER TABLE notificaciones
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE;

ALTER TABLE notificaciones
    ADD CONSTRAINT IF NOT EXISTS notificaciones_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE notificaciones
    ADD CONSTRAINT IF NOT EXISTS notificaciones_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN notificaciones.creado_por IS 'Usuario que creó la notificación (sistema si es null)';
COMMENT ON COLUMN notificaciones.actualizado_por IS 'Usuario que realizó la última modificación';
COMMENT ON COLUMN notificaciones.actualizado_en IS 'Fecha y hora de la última modificación';

-- 2.4 Tabla: tema
ALTER TABLE tema
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE tema
    ADD CONSTRAINT IF NOT EXISTS tema_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE tema
    ADD CONSTRAINT IF NOT EXISTS tema_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN tema.creado_por IS 'Usuario que creó el tema';
COMMENT ON COLUMN tema.actualizado_por IS 'Usuario que realizó la última modificación';

-- 2.5 Tabla: comuna
ALTER TABLE comuna
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE comuna
    ADD CONSTRAINT IF NOT EXISTS comuna_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE comuna
    ADD CONSTRAINT IF NOT EXISTS comuna_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN comuna.creado_por IS 'Usuario que creó la comuna';
COMMENT ON COLUMN comuna.actualizado_por IS 'Usuario que realizó la última modificación';

-- 2.6 Tabla: usuarios
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE usuarios
    ADD CONSTRAINT IF NOT EXISTS usuarios_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT IF NOT EXISTS usuarios_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN usuarios.creado_por IS 'Usuario que creó esta cuenta';
COMMENT ON COLUMN usuarios.actualizado_por IS 'Usuario que realizó la última modificación';

-- 2.7 Tabla: estado
ALTER TABLE estado
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE estado
    ADD CONSTRAINT IF NOT EXISTS estado_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE estado
    ADD CONSTRAINT IF NOT EXISTS estado_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN estado.creado_por IS 'Usuario que creó el estado';
COMMENT ON COLUMN estado.actualizado_por IS 'Usuario que realizó la última modificación';

-- 2.8 Tabla: estado_transicion
ALTER TABLE estado_transicion
    ADD COLUMN IF NOT EXISTS creado_por BIGINT,
    ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE;

ALTER TABLE estado_transicion
    ADD CONSTRAINT IF NOT EXISTS estado_transicion_creado_por_fk
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE estado_transicion
    ADD CONSTRAINT IF NOT EXISTS estado_transicion_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN estado_transicion.creado_por IS 'Usuario que creó la transición';
COMMENT ON COLUMN estado_transicion.creado_en IS 'Fecha de creación';
COMMENT ON COLUMN estado_transicion.actualizado_por IS 'Usuario que realizó la última modificación';
COMMENT ON COLUMN estado_transicion.actualizado_en IS 'Fecha y hora de la última modificación';

-- 2.9 Tabla: historial_estado
ALTER TABLE historial_estado
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP WITH TIME ZONE;

ALTER TABLE historial_estado
    ADD CONSTRAINT IF NOT EXISTS historial_estado_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN historial_estado.actualizado_por IS 'Usuario que realizó la última modificación';
COMMENT ON COLUMN historial_estado.actualizado_en IS 'Fecha y hora de la última modificación';

-- 2.10 Tabla: querella (agregar actualizado_por)
ALTER TABLE querella
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE querella
    ADD CONSTRAINT IF NOT EXISTS querella_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN querella.actualizado_por IS 'Usuario que realizó la última modificación';

-- 2.11 Tabla: despacho_comisorio (agregar actualizado_por)
ALTER TABLE despacho_comisorio
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

ALTER TABLE despacho_comisorio
    ADD CONSTRAINT IF NOT EXISTS despacho_actualizado_por_fk
    FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMENT ON COLUMN despacho_comisorio.actualizado_por IS 'Usuario que realizó la última modificación';

-- 3. INTEGRAR TABLA CONFIGURACION_SISTEMA AL SCHEMA PRINCIPAL
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500),
    descripcion VARCHAR(200),
    actualizado_en TIMESTAMPTZ,
    creado_por BIGINT,
    actualizado_por BIGINT,
    CONSTRAINT configuracion_sistema_creado_por_fk
        FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT configuracion_sistema_actualizado_por_fk
        FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_configuracion_sistema_clave ON configuracion_sistema(clave);

COMMENT ON TABLE configuracion_sistema IS 'Configuraciones generales del sistema (round-robin, etc.)';
COMMENT ON COLUMN configuracion_sistema.clave IS 'Identificador único de la configuración';
COMMENT ON COLUMN configuracion_sistema.valor IS 'Valor de la configuración como string';

-- Insertar configuración inicial para round-robin
INSERT INTO configuracion_sistema (clave, valor, descripcion, actualizado_en)
VALUES (
    'ROUND_ROBIN_ULTIMO_INSPECTOR_ID',
    NULL,
    'ID del último inspector asignado en round-robin de querellas',
    NOW()
)
ON CONFLICT (clave) DO NOTHING;

-- 4. CREAR ÍNDICES CRÍTICOS PARA RENDIMIENTO

-- 4.1 Índices en querella
CREATE INDEX IF NOT EXISTS idx_querella_es_migrado ON querella(es_migrado);
CREATE INDEX IF NOT EXISTS idx_querella_archivado ON querella(archivado) WHERE archivado = TRUE;
CREATE INDEX IF NOT EXISTS idx_querella_comuna_creado ON querella(comuna_id, creado_en DESC);
CREATE INDEX IF NOT EXISTS idx_querella_tiene_fallo ON querella(tiene_fallo);

-- 4.2 Índices en comunicaciones
CREATE INDEX IF NOT EXISTS idx_comunicaciones_fecha_envio ON comunicaciones(fecha_envio);
CREATE INDEX IF NOT EXISTS idx_comunicaciones_querella_estado ON comunicaciones(querella_id, estado);
CREATE INDEX IF NOT EXISTS idx_comunicaciones_actualizado_en ON comunicaciones(actualizado_en DESC);

-- 4.3 Índices en adjuntos
CREATE INDEX IF NOT EXISTS idx_adjuntos_cargado_creado ON adjuntos(cargado_por, creado_en DESC);
CREATE INDEX IF NOT EXISTS idx_adjuntos_tamano ON adjuntos(tamano_bytes);

-- 4.4 Índices en notificaciones
CREATE INDEX IF NOT EXISTS idx_notificaciones_tipo ON notificaciones(tipo);
CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario_tipo_leida ON notificaciones(usuario_id, tipo, leida, creado_en DESC);

-- 4.5 Índices en despacho_comisorio
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_devolucion ON despacho_comisorio(fecha_devolucion);
CREATE INDEX IF NOT EXISTS idx_despacho_inspector_fecha ON despacho_comisorio(inspector_asignado_id, fecha_recibido DESC);
CREATE INDEX IF NOT EXISTS idx_despacho_creado_por ON despacho_comisorio(creado_por);

-- 4.6 Índices en historial_estado
CREATE INDEX IF NOT EXISTS idx_historial_usuario ON historial_estado(usuario_id);
CREATE INDEX IF NOT EXISTS idx_historial_modulo_estado_fecha ON historial_estado(modulo, estado_id, creado_en DESC);

-- =====================================================
-- FASE 2: MEJORAS IMPORTANTES
-- =====================================================

-- 5. TRIGGERS PARA ACTUALIZAR actualizado_en AUTOMÁTICAMENTE

-- Función genérica para actualizar updated_at
CREATE OR REPLACE FUNCTION actualizar_timestamp_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION actualizar_timestamp_modificacion() IS 'Actualiza automáticamente actualizado_en en UPDATE';

-- Aplicar trigger a todas las tablas con actualizado_en
DROP TRIGGER IF EXISTS trigger_actualizar_comunicaciones ON comunicaciones;
CREATE TRIGGER trigger_actualizar_comunicaciones
    BEFORE UPDATE ON comunicaciones
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_adjuntos ON adjuntos;
CREATE TRIGGER trigger_actualizar_adjuntos
    BEFORE UPDATE ON adjuntos
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_notificaciones ON notificaciones;
CREATE TRIGGER trigger_actualizar_notificaciones
    BEFORE UPDATE ON notificaciones
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_tema ON tema;
CREATE TRIGGER trigger_actualizar_tema
    BEFORE UPDATE ON tema
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_comuna ON comuna;
CREATE TRIGGER trigger_actualizar_comuna
    BEFORE UPDATE ON comuna
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_usuarios ON usuarios;
CREATE TRIGGER trigger_actualizar_usuarios
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_estado ON estado;
CREATE TRIGGER trigger_actualizar_estado
    BEFORE UPDATE ON estado
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_estado_transicion ON estado_transicion;
CREATE TRIGGER trigger_actualizar_estado_transicion
    BEFORE UPDATE ON estado_transicion
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_historial ON historial_estado;
CREATE TRIGGER trigger_actualizar_historial
    BEFORE UPDATE ON historial_estado
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

DROP TRIGGER IF EXISTS trigger_actualizar_configuracion ON configuracion_sistema;
CREATE TRIGGER trigger_actualizar_configuracion
    BEFORE UPDATE ON configuracion_sistema
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- 6. ÍNDICES PARA BÚSQUEDA DE TEXTO (FULLTEXT)

-- Crear columna tsvector para búsqueda fulltext en querella
ALTER TABLE querella ADD COLUMN IF NOT EXISTS busqueda_tsvector tsvector;

-- Índice GIN para búsqueda rápida
CREATE INDEX IF NOT EXISTS idx_querella_busqueda_gin
    ON querella USING GIN(busqueda_tsvector);

-- Función para actualizar tsvector
CREATE OR REPLACE FUNCTION actualizar_querella_tsvector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.busqueda_tsvector :=
        setweight(to_tsvector('spanish', COALESCE(NEW.radicado_interno, '')), 'A') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.direccion, '')), 'B') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.descripcion, '')), 'C') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.barrio, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar tsvector automáticamente
DROP TRIGGER IF EXISTS trigger_querella_tsvector ON querella;
CREATE TRIGGER trigger_querella_tsvector
    BEFORE INSERT OR UPDATE ON querella
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_querella_tsvector();

-- Actualizar tsvector para registros existentes
UPDATE querella SET busqueda_tsvector =
    setweight(to_tsvector('spanish', COALESCE(radicado_interno, '')), 'A') ||
    setweight(to_tsvector('spanish', COALESCE(direccion, '')), 'B') ||
    setweight(to_tsvector('spanish', COALESCE(descripcion, '')), 'C') ||
    setweight(to_tsvector('spanish', COALESCE(barrio, '')), 'B')
WHERE busqueda_tsvector IS NULL;

COMMENT ON COLUMN querella.busqueda_tsvector IS 'Vector de búsqueda fulltext (español)';

-- =====================================================
-- VERIFICACIÓN Y ESTADÍSTICAS
-- =====================================================

-- Contar columnas de auditoría agregadas
SELECT
    'Auditoría completa' AS status,
    COUNT(*) FILTER (WHERE column_name LIKE '%creado_por%') AS creado_por_count,
    COUNT(*) FILTER (WHERE column_name LIKE '%actualizado_por%') AS actualizado_por_count,
    COUNT(*) FILTER (WHERE column_name LIKE '%creado_en%') AS creado_en_count,
    COUNT(*) FILTER (WHERE column_name LIKE '%actualizado_en%') AS actualizado_en_count
FROM information_schema.columns
WHERE table_schema = 'public';

-- Contar índices creados
SELECT
    'Índices optimizados' AS status,
    COUNT(*) AS total_indices
FROM pg_indexes
WHERE schemaname = 'public';

-- Verificar triggers
SELECT
    'Triggers configurados' AS status,
    COUNT(*) AS total_triggers
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relnamespace = 'public'::regnamespace
  AND NOT tgisinternal;

COMMIT;

-- =====================================================
-- MENSAJE FINAL
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '✅ Mejoras aplicadas exitosamente';
    RAISE NOTICE '📊 Revisar output de verificación arriba';
    RAISE NOTICE '📖 Ver AUDITORIA_BASE_DATOS.md para más detalles';
    RAISE NOTICE '⚠️  Actualizar entidades Java para incluir nuevos campos de auditoría';
END $$;
