-- =====================================================
-- ESQUEMA DE BASE DE DATOS - SISTEMA DE QUERELLAS
-- Inspecciones de Policía - Alcaldía de Neiva
-- Versión: 2.0 - SIN INSPECCIONES, CON INSPECTORES
-- Base de Datos: PostgreSQL 12+
-- =====================================================

-- Configuración inicial
SET client_encoding = 'UTF8';
SET timezone = 'UTC';

-- =====================================================
-- SECUENCIAS
-- =====================================================

-- Secuencia para generar radicados internos únicos
CREATE SEQUENCE IF NOT EXISTS seq_radicado_querella
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

-- =====================================================
-- TABLAS DE CATÁLOGOS BASE
-- =====================================================

-- Tabla: tema
-- Catálogo de temas o motivos de querellas
CREATE TABLE tema (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE tema IS 'Catálogo de temas/motivos de querellas (Ruidos, Construcción, etc.)';

-- Tabla: comuna
-- Catálogo de comunas de Neiva
CREATE TABLE comuna (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE comuna IS 'Catálogo de comunas de Neiva';

-- =====================================================
-- TABLAS DE ESTADOS Y FLUJO
-- =====================================================

-- Tabla: estado
-- Catálogo de estados para diferentes módulos (QUERELLA, DESPACHO, etc.)
CREATE TABLE estado (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE,
    CONSTRAINT estado_modulo_nombre_key UNIQUE (modulo, nombre)
);

COMMENT ON TABLE estado IS 'Catálogo de estados del sistema por módulo';
COMMENT ON COLUMN estado.modulo IS 'Módulo al que pertenece: QUERELLA, DESPACHO, etc.';
COMMENT ON COLUMN estado.nombre IS 'Nombre del estado: RECIBIDA, ASIGNADA, EN_PROCESO, etc.';

-- Tabla: estado_transicion
-- Define las transiciones permitidas entre estados
CREATE TABLE estado_transicion (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    desde_estado_id BIGINT NOT NULL,
    hacia_estado_id BIGINT NOT NULL,
    CONSTRAINT estado_transicion_desde_fk FOREIGN KEY (desde_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT estado_transicion_hacia_fk FOREIGN KEY (hacia_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT estado_transicion_modulo_desde_estado_id_hacia_estado_id_key
        UNIQUE (modulo, desde_estado_id, hacia_estado_id)
);

COMMENT ON TABLE estado_transicion IS 'Define qué transiciones de estado están permitidas';
COMMENT ON COLUMN estado_transicion.modulo IS 'Módulo: QUERELLA, DESPACHO, etc.';

-- Tabla: historial_estado
-- Registra todos los cambios de estado de casos
CREATE TABLE historial_estado (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    caso_id BIGINT NOT NULL,
    estado_id BIGINT NOT NULL,
    motivo TEXT NOT NULL,
    usuario_id BIGINT,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT historial_estado_estado_fk FOREIGN KEY (estado_id)
        REFERENCES estado(id) ON DELETE RESTRICT
);

COMMENT ON TABLE historial_estado IS 'Historial de cambios de estado de querellas/despachos';
COMMENT ON COLUMN historial_estado.modulo IS 'QUERELLA o DESPACHO';
COMMENT ON COLUMN historial_estado.caso_id IS 'ID de la querella o despacho';
COMMENT ON COLUMN historial_estado.motivo IS 'Razón del cambio de estado';

-- =====================================================
-- TABLA DE USUARIOS
-- =====================================================

-- Tabla: usuarios
-- Usuarios del sistema (Directora, Auxiliares, Inspectores)
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    zona VARCHAR(20),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE,
    CONSTRAINT usuarios_rol_check CHECK (rol IN ('INSPECTOR', 'DIRECTOR', 'AUXILIAR')),
    CONSTRAINT usuarios_estado_check CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'NO_DISPONIBLE')),
    CONSTRAINT usuarios_zona_check CHECK (zona IN ('NEIVA', 'CORREGIMIENTO') OR zona IS NULL)
);

COMMENT ON TABLE usuarios IS 'Usuarios del sistema con diferentes roles';
COMMENT ON COLUMN usuarios.password IS 'Contraseña hasheada con BCrypt';
COMMENT ON COLUMN usuarios.rol IS 'INSPECTOR, DIRECTOR, o AUXILIAR';
COMMENT ON COLUMN usuarios.estado IS 'ACTIVO, BLOQUEADO, o NO_DISPONIBLE';
COMMENT ON COLUMN usuarios.zona IS 'NEIVA o CORREGIMIENTO (solo para INSPECTOR)';

-- =====================================================
-- TABLA PRINCIPAL: QUERELLA
-- =====================================================

-- Tabla: querella
-- Registro principal de querellas
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
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_local VARCHAR(20),
    querellante_nombre VARCHAR(200),
    querellante_contacto VARCHAR(100),
    barrio VARCHAR(200),
    genero_querellante VARCHAR(20),
    genero_querellado VARCHAR(20),
    normas_aplicables VARCHAR(1024),
    observaciones TEXT,
    tiene_fallo BOOLEAN,
    tiene_apelacion BOOLEAN,
    archivado BOOLEAN,
    materializacion_medida BOOLEAN,
    CONSTRAINT querella_tema_fk FOREIGN KEY (tema_id)
        REFERENCES tema(id) ON DELETE SET NULL,
    CONSTRAINT querella_inspector_asignado_fk FOREIGN KEY (inspector_asignado_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_asignado_por_fk FOREIGN KEY (asignado_por)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT querella_comuna_fk FOREIGN KEY (comuna_id)
        REFERENCES comuna(id) ON DELETE SET NULL,
    CONSTRAINT querella_naturaleza_check CHECK (naturaleza IN ('OFICIO', 'PERSONA', 'ANONIMA'))
);

COMMENT ON TABLE querella IS 'Registro principal de querellas ciudadanas';
COMMENT ON COLUMN querella.radicado_interno IS 'Radicado único generado: Q-YYYY-NNNNNN';
COMMENT ON COLUMN querella.naturaleza IS 'OFICIO, PERSONA, o ANONIMA';
COMMENT ON COLUMN querella.inspector_asignado_id IS 'Inspector asignado a la querella';
COMMENT ON COLUMN querella.asignado_por IS 'Usuario que asignó el inspector';
COMMENT ON COLUMN querella.id_local IS 'ID local por inspector (generado por trigger)';
COMMENT ON COLUMN querella.id_alcaldia IS 'ID del sistema de la alcaldía (migración)';
COMMENT ON COLUMN querella.es_migrado IS 'TRUE si viene del sistema antiguo';

-- =====================================================
-- TABLAS RELACIONADAS A QUERELLAS
-- =====================================================

-- Tabla: comunicaciones
-- Comunicaciones oficiales relacionadas a querellas
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
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT comunicaciones_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT comunicaciones_creado_por_fk FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT comunicaciones_tipo_check CHECK (tipo IN ('OFICIO', 'NOTIFICACION', 'CITACION', 'AUTO', 'RESOLUCION')),
    CONSTRAINT comunicaciones_estado_check CHECK (estado IN ('BORRADOR', 'ENVIADO', 'RECIBIDO'))
);

COMMENT ON TABLE comunicaciones IS 'Oficios, notificaciones y otros documentos oficiales';
COMMENT ON COLUMN comunicaciones.tipo IS 'OFICIO, NOTIFICACION, CITACION, AUTO, RESOLUCION';
COMMENT ON COLUMN comunicaciones.estado IS 'BORRADOR, ENVIADO, RECIBIDO';

-- Tabla: adjuntos
-- Archivos adjuntos a querellas
CREATE TABLE adjuntos (
    id BIGSERIAL PRIMARY KEY,
    querella_id BIGINT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_archivo VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    ruta_storage VARCHAR(500) NOT NULL,
    descripcion VARCHAR(500),
    cargado_por BIGINT NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT adjuntos_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT adjuntos_cargado_por_fk FOREIGN KEY (cargado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT
);

COMMENT ON TABLE adjuntos IS 'Archivos adjuntos (fotos, documentos, etc.)';
COMMENT ON COLUMN adjuntos.ruta_storage IS 'Ruta donde se almacena el archivo en el servidor';

-- Tabla: notificaciones
-- Notificaciones del sistema para usuarios
CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    querella_id BIGINT,
    usuario_id BIGINT NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notificaciones_querella_fk FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT notificaciones_usuario_fk FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT notificaciones_tipo_check CHECK (tipo IN ('ASIGNACION', 'CAMBIO_ESTADO', 'COMENTARIO', 'SISTEMA', 'RECORDATORIO'))
);

COMMENT ON TABLE notificaciones IS 'Notificaciones internas del sistema';
COMMENT ON COLUMN notificaciones.tipo IS 'ASIGNACION, CAMBIO_ESTADO, COMENTARIO, SISTEMA, RECORDATORIO';

-- =====================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =====================================================

-- Índices en querella para búsquedas frecuentes
CREATE INDEX idx_querella_radicado_interno ON querella(radicado_interno);
CREATE INDEX idx_querella_id_local ON querella(id_local);
CREATE INDEX idx_querella_inspector_creado ON querella(inspector_asignado_id, creado_en DESC);
CREATE INDEX idx_querella_asignado_por ON querella(asignado_por);
CREATE INDEX idx_querella_comuna ON querella(comuna_id);
CREATE INDEX idx_querella_tema ON querella(tema_id);
CREATE INDEX idx_querella_creado_en ON querella(creado_en DESC);
CREATE INDEX idx_querella_naturaleza ON querella(naturaleza);

-- Índices en historial_estado para consultas de estado actual
CREATE INDEX idx_historial_modulo_caso ON historial_estado(modulo, caso_id, creado_en DESC);
CREATE INDEX idx_historial_estado ON historial_estado(estado_id);

-- Índices en usuarios
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_estado ON usuarios(estado);
CREATE INDEX idx_usuarios_zona ON usuarios(zona);

-- Índices en comunicaciones
CREATE INDEX idx_comunicaciones_querella ON comunicaciones(querella_id);
CREATE INDEX idx_comunicaciones_creado_por ON comunicaciones(creado_por);
CREATE INDEX idx_comunicaciones_tipo ON comunicaciones(tipo);
CREATE INDEX idx_comunicaciones_estado ON comunicaciones(estado);

-- Índices en adjuntos
CREATE INDEX idx_adjuntos_querella ON adjuntos(querella_id);
CREATE INDEX idx_adjuntos_cargado_por ON adjuntos(cargado_por);

-- Índices en notificaciones
CREATE INDEX idx_notificaciones_usuario_leida ON notificaciones(usuario_id, leida, creado_en DESC);
CREATE INDEX idx_notificaciones_querella ON notificaciones(querella_id);

-- Índices en estado_transicion
CREATE INDEX idx_estado_transicion_modulo_desde ON estado_transicion(modulo, desde_estado_id);

-- =====================================================
-- TRIGGER: Generar ID Local por Inspector
-- =====================================================

-- Función para generar id_local automáticamente
CREATE OR REPLACE FUNCTION generar_id_local_querella()
RETURNS TRIGGER AS $$
DECLARE
    contador INTEGER;
    año_actual INTEGER;
BEGIN
    -- Solo genera si se asignó un inspector y no existe id_local
    IF NEW.inspector_asignado_id IS NOT NULL AND NEW.id_local IS NULL THEN
        año_actual := EXTRACT(YEAR FROM NEW.creado_en);

        -- Contar querellas de ese inspector en el año actual
        SELECT COUNT(*) + 1 INTO contador
        FROM querella
        WHERE inspector_asignado_id = NEW.inspector_asignado_id
          AND EXTRACT(YEAR FROM creado_en) = año_actual;

        -- Formato: INSPECTOR-ID-AÑO-CONTADOR (ej: 5-2025-0001)
        NEW.id_local := NEW.inspector_asignado_id || '-' || año_actual || '-' || LPAD(contador::TEXT, 4, '0');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger
CREATE TRIGGER trigger_generar_id_local
    BEFORE INSERT OR UPDATE ON querella
    FOR EACH ROW
    EXECUTE FUNCTION generar_id_local_querella();

COMMENT ON FUNCTION generar_id_local_querella() IS 'Genera ID local único por inspector y año';

-- =====================================================
-- FUNCIÓN AUXILIAR: Obtener estado actual de querella
-- =====================================================

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

-- =====================================================
-- PERMISOS Y SEGURIDAD
-- =====================================================

-- Revocar permisos públicos por defecto
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;

-- Nota: Los permisos específicos deben configurarse según el usuario
-- de aplicación creado por el DBA de la alcaldía

-- =====================================================
-- FIN DEL ESQUEMA
-- =====================================================

-- Verificación de integridad
SELECT 'Esquema creado exitosamente' AS status;
SELECT COUNT(*) || ' tablas creadas' FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
