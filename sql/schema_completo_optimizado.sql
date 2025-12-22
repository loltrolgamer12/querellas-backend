-- =====================================================
-- SCHEMA COMPLETO Y OPTIMIZADO - BASE DE DATOS
-- Sistema de Querellas - Inspecciones de Policía
-- Alcaldía de Neiva
-- =====================================================
-- Versión: 3.0 - PRODUCCIÓN OPTIMIZADA
-- Base de Datos: PostgreSQL 12+
-- Auditoría: 100% completa
-- Rendimiento: Optimizado para alto volumen
-- Escalabilidad: >1M registros
-- =====================================================

-- Configuración inicial
SET client_encoding = 'UTF8';
SET timezone = 'UTC';

\echo '🚀 Iniciando creación de base de datos optimizada...'

BEGIN;

-- =====================================================
-- SECUENCIAS
-- =====================================================

\echo '📝 Creando secuencias...'

-- Secuencia para generar radicados internos únicos
CREATE SEQUENCE IF NOT EXISTS seq_radicado_querella
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

-- Secuencia para número de despacho interno
CREATE SEQUENCE IF NOT EXISTS seq_numero_despacho
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

COMMENT ON SEQUENCE seq_radicado_querella IS 'Secuencia para generar radicados únicos de querellas';
COMMENT ON SEQUENCE seq_numero_despacho IS 'Secuencia para generar números de despacho internos';

-- =====================================================
-- TABLAS DE CATÁLOGOS BASE
-- =====================================================

\echo '📚 Creando tablas de catálogos...'

-- Tabla: tema
CREATE TABLE tema (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por BIGINT,
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT
);

CREATE INDEX idx_tema_activo ON tema(activo) WHERE activo = TRUE;
CREATE INDEX idx_tema_nombre ON tema(nombre);

COMMENT ON TABLE tema IS 'Catálogo de temas/motivos de querellas (Ruidos, Construcción, Espacio Público, etc.)';
COMMENT ON COLUMN tema.activo IS 'Indica si el tema está activo para nuevas querellas';

-- Tabla: comuna
CREATE TABLE comuna (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por BIGINT,
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT
);

CREATE INDEX idx_comuna_activo ON comuna(activo) WHERE activo = TRUE;
CREATE INDEX idx_comuna_nombre ON comuna(nombre);

COMMENT ON TABLE comuna IS 'Catálogo de comunas de Neiva';
COMMENT ON COLUMN comuna.activo IS 'Indica si la comuna está activa';

-- =====================================================
-- TABLAS DE ESTADOS Y FLUJO
-- =====================================================

\echo '🔄 Creando tablas de estados...'

-- Tabla: estado
CREATE TABLE estado (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200),
    orden INTEGER DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por BIGINT,
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT estado_modulo_nombre_key UNIQUE (modulo, nombre)
);

CREATE INDEX idx_estado_modulo ON estado(modulo);
CREATE INDEX idx_estado_activo ON estado(activo) WHERE activo = TRUE;
CREATE INDEX idx_estado_modulo_orden ON estado(modulo, orden);

COMMENT ON TABLE estado IS 'Catálogo de estados del sistema por módulo';
COMMENT ON COLUMN estado.modulo IS 'Módulo: QUERELLA, DESPACHO, etc.';
COMMENT ON COLUMN estado.nombre IS 'Nombre del estado: RECIBIDA, ASIGNADA, EN_PROCESO, etc.';
COMMENT ON COLUMN estado.orden IS 'Orden de visualización del estado';

-- Tabla: estado_transicion
CREATE TABLE estado_transicion (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    desde_estado_id BIGINT NOT NULL,
    hacia_estado_id BIGINT NOT NULL,
    descripcion VARCHAR(200),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por BIGINT,
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT estado_transicion_desde_fk FOREIGN KEY (desde_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT estado_transicion_hacia_fk FOREIGN KEY (hacia_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT estado_transicion_modulo_desde_hacia_key
        UNIQUE (modulo, desde_estado_id, hacia_estado_id)
);

CREATE INDEX idx_estado_transicion_modulo ON estado_transicion(modulo);
CREATE INDEX idx_estado_transicion_desde ON estado_transicion(desde_estado_id);
CREATE INDEX idx_estado_transicion_hacia ON estado_transicion(hacia_estado_id);

COMMENT ON TABLE estado_transicion IS 'Define qué transiciones de estado están permitidas';

-- Tabla: historial_estado
CREATE TABLE historial_estado (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    caso_id BIGINT NOT NULL,
    estado_id BIGINT NOT NULL,
    motivo TEXT NOT NULL,
    usuario_id BIGINT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT historial_estado_estado_fk FOREIGN KEY (estado_id)
        REFERENCES estado(id) ON DELETE RESTRICT
);

CREATE INDEX idx_historial_modulo_caso_fecha ON historial_estado(modulo, caso_id, creado_en DESC);
CREATE INDEX idx_historial_estado ON historial_estado(estado_id);
CREATE INDEX idx_historial_usuario ON historial_estado(usuario_id);
CREATE INDEX idx_historial_modulo_estado_fecha ON historial_estado(modulo, estado_id, creado_en DESC);

COMMENT ON TABLE historial_estado IS 'Historial completo de cambios de estado de querellas/despachos';
COMMENT ON COLUMN historial_estado.modulo IS 'QUERELLA o DESPACHO';
COMMENT ON COLUMN historial_estado.caso_id IS 'ID de la querella o despacho';

-- =====================================================
-- TABLA DE USUARIOS
-- =====================================================

\echo '👥 Creando tabla de usuarios...'

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    zona VARCHAR(20),
    foto_url VARCHAR(500),
    ultimo_login TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creado_por BIGINT,
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT usuarios_rol_check CHECK (rol IN ('INSPECTOR', 'DIRECTORA', 'AUXILIAR')),
    CONSTRAINT usuarios_estado_check CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'NO_DISPONIBLE')),
    CONSTRAINT usuarios_zona_check CHECK (zona IN ('NEIVA', 'CORREGIMIENTO') OR zona IS NULL),
    CONSTRAINT usuarios_creado_por_fk FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT usuarios_actualizado_por_fk FOREIGN KEY (actualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_estado ON usuarios(estado);
CREATE INDEX idx_usuarios_zona ON usuarios(zona);
CREATE INDEX idx_usuarios_rol_estado ON usuarios(rol, estado) WHERE estado = 'ACTIVO';

COMMENT ON TABLE usuarios IS 'Usuarios del sistema con diferentes roles';
COMMENT ON COLUMN usuarios.password IS 'Contraseña hasheada con BCrypt';
COMMENT ON COLUMN usuarios.rol IS 'INSPECTOR, DIRECTORA, o AUXILIAR';
COMMENT ON COLUMN usuarios.estado IS 'ACTIVO, BLOQUEADO, o NO_DISPONIBLE';
COMMENT ON COLUMN usuarios.zona IS 'NEIVA o CORREGIMIENTO (solo para INSPECTOR)';

-- =====================================================
-- TABLA PRINCIPAL: QUERELLA
-- =====================================================

\echo '📋 Creando tabla de querellas...'

CREATE TABLE querella (
    id BIGSERIAL PRIMARY KEY,
    radicado_interno VARCHAR(20),
    direccion TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    tema_id BIGINT,
    naturaleza VARCHAR(20) NOT NULL,
    inspector_asignado_id BIGINT,
    asignado_por BIGINT,
    comuna_id BIGINT,
    id_alcaldia VARCHAR(50),
    es_migrado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_por BIGINT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_por BIGINT,
    id_local VARCHAR(20),
    querellante_nombre VARCHAR(200),
    querellante_contacto VARCHAR(100),
    barrio VARCHAR(200),
    genero_querellante VARCHAR(20),
    genero_querellado VARCHAR(20),
    normas_aplicables TEXT,
    observaciones TEXT,
    tiene_fallo BOOLEAN,
    tiene_apelacion BOOLEAN,
    archivado BOOLEAN DEFAULT FALSE,
    materializacion_medida BOOLEAN,
    busqueda_tsvector tsvector,
    CONSTRAINT querella_tema_fk FOREIGN KEY (tema_id)
        REFERENCES tema(id) ON DELETE SET NULL,
    CONSTRAINT querella_inspector_asignado_fk FOREIGN KEY (inspector_asignado_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_asignado_por_fk FOREIGN KEY (asignado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_comuna_fk FOREIGN KEY (comuna_id)
        REFERENCES comuna(id) ON DELETE SET NULL,
    CONSTRAINT querella_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_naturaleza_check CHECK (naturaleza IN ('OFICIO', 'PERSONA', 'ANONIMA'))
);

-- Índices optimizados para querella
CREATE UNIQUE INDEX idx_querella_radicado ON querella(radicado_interno);
CREATE INDEX idx_querella_id_local ON querella(id_local);
CREATE INDEX idx_querella_inspector_creado ON querella(inspector_asignado_id, creado_en DESC);
CREATE INDEX idx_querella_asignado_por ON querella(asignado_por);
CREATE INDEX idx_querella_comuna ON querella(comuna_id);
CREATE INDEX idx_querella_tema ON querella(tema_id);
CREATE INDEX idx_querella_creado_en ON querella(creado_en DESC);
CREATE INDEX idx_querella_naturaleza ON querella(naturaleza);
CREATE INDEX idx_querella_es_migrado ON querella(es_migrado);
CREATE INDEX idx_querella_archivado ON querella(archivado) WHERE archivado = TRUE;
CREATE INDEX idx_querella_comuna_creado ON querella(comuna_id, creado_en DESC);
CREATE INDEX idx_querella_tiene_fallo ON querella(tiene_fallo);
CREATE INDEX idx_querella_busqueda_gin ON querella USING GIN(busqueda_tsvector);

COMMENT ON TABLE querella IS 'Registro principal de querellas ciudadanas';
COMMENT ON COLUMN querella.radicado_interno IS 'Radicado único generado: Q-YYYY-NNNNNN';
COMMENT ON COLUMN querella.naturaleza IS 'OFICIO, PERSONA, o ANONIMA';
COMMENT ON COLUMN querella.id_local IS 'ID local por inspector (generado por trigger)';
COMMENT ON COLUMN querella.busqueda_tsvector IS 'Vector de búsqueda fulltext optimizado (español)';

-- =====================================================
-- TABLA: DESPACHOS COMISORIOS
-- =====================================================

\echo '📄 Creando tabla de despachos comisorios...'

CREATE TABLE despacho_comisorio (
    id BIGSERIAL PRIMARY KEY,
    fecha_recibido TIMESTAMPTZ NOT NULL,
    radicado_proceso VARCHAR(50),
    numero_despacho VARCHAR(50) NOT NULL,
    entidad_procedente VARCHAR(255) NOT NULL,
    asunto TEXT NOT NULL,
    demandante_apoderado TEXT,
    demandado_apoderado TEXT,
    inspector_asignado_id BIGINT,
    asignado_por BIGINT,
    fecha_devolucion TIMESTAMPTZ,
    observaciones TEXT,
    creado_por BIGINT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_por BIGINT,
    CONSTRAINT despacho_inspector_fk FOREIGN KEY (inspector_asignado_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT despacho_asignado_por_fk FOREIGN KEY (asignado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT despacho_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT despacho_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Índices optimizados para despacho_comisorio
CREATE INDEX idx_despacho_fecha_recibido ON despacho_comisorio(fecha_recibido DESC);
CREATE INDEX idx_despacho_inspector ON despacho_comisorio(inspector_asignado_id);
CREATE INDEX idx_despacho_numero ON despacho_comisorio(numero_despacho);
CREATE INDEX idx_despacho_entidad ON despacho_comisorio(entidad_procedente);
CREATE INDEX idx_despacho_fecha_devolucion ON despacho_comisorio(fecha_devolucion);
CREATE INDEX idx_despacho_inspector_fecha ON despacho_comisorio(inspector_asignado_id, fecha_recibido DESC);
CREATE INDEX idx_despacho_creado_por ON despacho_comisorio(creado_por);

COMMENT ON TABLE despacho_comisorio IS 'Despachos comisorios recibidos de otras entidades judiciales';
COMMENT ON COLUMN despacho_comisorio.fecha_devolucion IS 'Fecha en que se devolvió el despacho diligenciado';

-- =====================================================
-- TABLAS RELACIONADAS A QUERELLAS
-- =====================================================

\echo '📎 Creando tablas relacionadas...'

-- Tabla: comunicaciones
CREATE TABLE comunicaciones (
    id BIGSERIAL PRIMARY KEY,
    querella_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    numero_radicado VARCHAR(50),
    asunto VARCHAR(300) NOT NULL,
    contenido TEXT,
    fecha_envio DATE,
    destinatario VARCHAR(200) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_por BIGINT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT comunicaciones_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT comunicaciones_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT comunicaciones_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT comunicaciones_tipo_check CHECK (tipo IN ('OFICIO', 'NOTIFICACION', 'CITACION', 'AUTO', 'RESOLUCION')),
    CONSTRAINT comunicaciones_estado_check CHECK (estado IN ('BORRADOR', 'ENVIADO', 'RECIBIDO'))
);

CREATE INDEX idx_comunicaciones_querella ON comunicaciones(querella_id);
CREATE INDEX idx_comunicaciones_creado_por ON comunicaciones(creado_por);
CREATE INDEX idx_comunicaciones_tipo ON comunicaciones(tipo);
CREATE INDEX idx_comunicaciones_estado ON comunicaciones(estado);
CREATE INDEX idx_comunicaciones_fecha_envio ON comunicaciones(fecha_envio);
CREATE INDEX idx_comunicaciones_querella_estado ON comunicaciones(querella_id, estado);
CREATE INDEX idx_comunicaciones_actualizado_en ON comunicaciones(actualizado_en DESC);

COMMENT ON TABLE comunicaciones IS 'Oficios, notificaciones y otros documentos oficiales';

-- Tabla: adjuntos
CREATE TABLE adjuntos (
    id BIGSERIAL PRIMARY KEY,
    querella_id BIGINT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_archivo VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    ruta_storage VARCHAR(500) NOT NULL,
    descripcion VARCHAR(500),
    cargado_por BIGINT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT adjuntos_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT adjuntos_cargado_por_fk FOREIGN KEY (cargado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT adjuntos_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_adjuntos_querella ON adjuntos(querella_id);
CREATE INDEX idx_adjuntos_cargado_por ON adjuntos(cargado_por);
CREATE INDEX idx_adjuntos_cargado_creado ON adjuntos(cargado_por, creado_en DESC);
CREATE INDEX idx_adjuntos_tamano ON adjuntos(tamano_bytes);

COMMENT ON TABLE adjuntos IS 'Archivos adjuntos (fotos, documentos, PDFs, etc.)';

-- Tabla: notificaciones
CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    querella_id BIGINT,
    usuario_id BIGINT NOT NULL,
    creado_por BIGINT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ,
    actualizado_por BIGINT,
    CONSTRAINT notificaciones_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT notificaciones_usuario_fk FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT notificaciones_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT notificaciones_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT notificaciones_tipo_check CHECK (tipo IN ('ASIGNACION', 'CAMBIO_ESTADO', 'COMENTARIO', 'SISTEMA', 'RECORDATORIO'))
);

CREATE INDEX idx_notificaciones_usuario_leida_fecha ON notificaciones(usuario_id, leida, creado_en DESC);
CREATE INDEX idx_notificaciones_querella ON notificaciones(querella_id);
CREATE INDEX idx_notificaciones_tipo ON notificaciones(tipo);
CREATE INDEX idx_notificaciones_usuario_tipo_leida ON notificaciones(usuario_id, tipo, leida, creado_en DESC);

COMMENT ON TABLE notificaciones IS 'Notificaciones internas del sistema';

-- =====================================================
-- TABLA: CONFIGURACION_SISTEMA
-- =====================================================

\echo '⚙️  Creando tabla de configuración...'

CREATE TABLE configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500),
    descripcion VARCHAR(200),
    tipo_dato VARCHAR(20) DEFAULT 'STRING',
    actualizado_en TIMESTAMPTZ,
    creado_por BIGINT,
    actualizado_por BIGINT,
    CONSTRAINT configuracion_sistema_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT configuracion_sistema_actualizado_por_fk FOREIGN KEY (actualizado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT configuracion_tipo_check CHECK (tipo_dato IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON'))
);

CREATE INDEX idx_configuracion_sistema_clave ON configuracion_sistema(clave);

COMMENT ON TABLE configuracion_sistema IS 'Configuraciones generales del sistema (round-robin, parámetros, etc.)';
COMMENT ON COLUMN configuracion_sistema.tipo_dato IS 'Tipo de dato almacenado: STRING, INTEGER, BOOLEAN, JSON';

-- =====================================================
-- FUNCIONES Y TRIGGERS
-- =====================================================

\echo '🔧 Creando funciones y triggers...'

-- Función: Actualizar actualizado_en automáticamente
CREATE OR REPLACE FUNCTION actualizar_timestamp_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION actualizar_timestamp_modificacion() IS 'Actualiza automáticamente actualizado_en en UPDATE';

-- Función: Generar ID local por inspector
CREATE OR REPLACE FUNCTION generar_id_local_querella()
RETURNS TRIGGER AS $$
DECLARE
    contador INTEGER;
    año_actual INTEGER;
BEGIN
    IF NEW.inspector_asignado_id IS NOT NULL AND NEW.id_local IS NULL THEN
        año_actual := EXTRACT(YEAR FROM NEW.creado_en);

        SELECT COUNT(*) + 1 INTO contador
        FROM querella
        WHERE inspector_asignado_id = NEW.inspector_asignado_id
          AND EXTRACT(YEAR FROM creado_en) = año_actual;

        NEW.id_local := NEW.inspector_asignado_id || '-' || año_actual || '-' || LPAD(contador::TEXT, 4, '0');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generar_id_local_querella() IS 'Genera ID local único por inspector y año';

-- Función: Actualizar tsvector para búsqueda fulltext
CREATE OR REPLACE FUNCTION actualizar_querella_tsvector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.busqueda_tsvector :=
        setweight(to_tsvector('spanish', COALESCE(NEW.radicado_interno, '')), 'A') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.direccion, '')), 'B') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.descripcion, '')), 'C') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.barrio, '')), 'B') ||
        setweight(to_tsvector('spanish', COALESCE(NEW.querellante_nombre, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION actualizar_querella_tsvector() IS 'Actualiza vector de búsqueda fulltext en español';

-- Función: Obtener estado actual de querella
CREATE OR REPLACE FUNCTION obtener_estado_actual_querella(p_querella_id BIGINT)
RETURNS VARCHAR AS $$
DECLARE
    estado_nombre VARCHAR;
BEGIN
    SELECT e.nombre INTO estado_nombre
    FROM historial_estado he
    JOIN estado e ON e.id = he.estado_id
    WHERE he.modulo = 'QUERELLA'
      AND he.caso_id = p_querella_id
    ORDER BY he.creado_en DESC
    LIMIT 1;

    RETURN estado_nombre;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION obtener_estado_actual_querella IS 'Retorna el estado actual de una querella';

-- Aplicar triggers a todas las tablas
\echo '⚡ Aplicando triggers...'

-- Triggers para actualizado_en
CREATE TRIGGER trigger_actualizar_usuarios
    BEFORE UPDATE ON usuarios FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_tema
    BEFORE UPDATE ON tema FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_comuna
    BEFORE UPDATE ON comuna FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_estado
    BEFORE UPDATE ON estado FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_estado_transicion
    BEFORE UPDATE ON estado_transicion FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_historial
    BEFORE UPDATE ON historial_estado FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_querella
    BEFORE UPDATE ON querella FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_despacho
    BEFORE UPDATE ON despacho_comisorio FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_comunicaciones
    BEFORE UPDATE ON comunicaciones FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_adjuntos
    BEFORE UPDATE ON adjuntos FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_notificaciones
    BEFORE UPDATE ON notificaciones FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_configuracion
    BEFORE UPDATE ON configuracion_sistema FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- Trigger para generar ID local de querella
CREATE TRIGGER trigger_generar_id_local
    BEFORE INSERT OR UPDATE ON querella FOR EACH ROW
    EXECUTE FUNCTION generar_id_local_querella();

-- Trigger para actualizar búsqueda fulltext
CREATE TRIGGER trigger_querella_tsvector
    BEFORE INSERT OR UPDATE ON querella FOR EACH ROW
    EXECUTE FUNCTION actualizar_querella_tsvector();

-- =====================================================
-- PERMISOS Y SEGURIDAD
-- =====================================================

\echo '🔒 Configurando seguridad...'

-- Revocar permisos públicos por defecto
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;

-- Los permisos específicos deben configurarse según el usuario
-- de aplicación creado por el DBA

COMMIT;

-- =====================================================
-- VERIFICACIÓN FINAL
-- =====================================================

\echo ''
\echo '✅ Base de datos creada exitosamente'
\echo ''

-- Resumen de objetos creados
SELECT
    '📊 RESUMEN DE CREACIÓN' AS titulo,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE') AS tablas,
    (SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public') AS indices,
    (SELECT COUNT(*) FROM information_schema.sequences WHERE sequence_schema = 'public') AS secuencias,
    (SELECT COUNT(*) FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid WHERE c.relnamespace = 'public'::regnamespace AND NOT t.tgisinternal) AS triggers,
    (SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = 'public' AND p.prokind = 'f') AS funciones;

-- Listar tablas creadas
\echo ''
\echo '📋 TABLAS CREADAS:'
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

\echo ''
\echo '🎉 Schema completo y optimizado listo para usar'
\echo '📖 Ver schema_completo_optimizado.sql para más detalles'
\echo '⚠️  Ejecutar datos_iniciales.sql para cargar datos base'
