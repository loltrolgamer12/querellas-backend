-- ============================================================================
-- SCRIPT DE CREACIÓN COMPLETA DE LA BASE DE DATOS
-- Sistema de Gestión de Querellas e Inspecciones de Policía - Neiva
-- ============================================================================
-- Versión: 2.0.0
-- Fecha: 2025-12-10
-- Base de Datos: PostgreSQL 12+
-- Charset: UTF-8
-- Timezone: UTC
-- ============================================================================

-- IMPORTANTE: Este script crea toda la estructura desde cero
-- Si la base de datos ya existe, hacer backup antes de ejecutar

-- ============================================================================
-- EXTENSIONES DE POSTGRESQL
-- ============================================================================

-- Habilitar UUID (opcional, por si se necesita en el futuro)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- NIVEL 1: TABLAS INDEPENDIENTES (SIN FOREIGN KEYS)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: comuna
-- Descripción: Catálogo de comunas de la ciudad de Neiva
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comuna (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT comuna_nombre_key UNIQUE (nombre)
);

COMMENT ON TABLE comuna IS 'Catálogo de comunas de Neiva';
COMMENT ON COLUMN comuna.id IS 'Identificador único de la comuna';
COMMENT ON COLUMN comuna.nombre IS 'Nombre de la comuna (ej: Comuna 1 - Norte)';

-- ----------------------------------------------------------------------------
-- Tabla: corregimiento
-- Descripción: Catálogo de corregimientos de la ciudad de Neiva
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS corregimiento (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT corregimiento_nombre_key UNIQUE (nombre)
);

COMMENT ON TABLE corregimiento IS 'Catálogo de corregimientos de Neiva para asignación de inspectores';
COMMENT ON COLUMN corregimiento.id IS 'Identificador único del corregimiento';
COMMENT ON COLUMN corregimiento.nombre IS 'Nombre del corregimiento (ej: Caguán, Chapinero)';

-- ----------------------------------------------------------------------------
-- Tabla: tema
-- Descripción: Catálogo de temas/motivos de querellas
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tema (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT tema_nombre_key UNIQUE (nombre)
);

COMMENT ON TABLE tema IS 'Catálogo de temas o motivos de las querellas (ej: Ruidos, Amenazas)';
COMMENT ON COLUMN tema.id IS 'Identificador único del tema';
COMMENT ON COLUMN tema.nombre IS 'Nombre del tema';

-- ----------------------------------------------------------------------------
-- Tabla: inspeccion (LEGACY - deprecated)
-- Descripción: Catálogo de inspecciones (mantenido por compatibilidad)
-- Nota: Este sistema fue reemplazado por corregimientos
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inspeccion (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT inspeccion_nombre_key UNIQUE (nombre)
);

COMMENT ON TABLE inspeccion IS '[LEGACY] Inspecciones - Reemplazado por sistema de corregimientos';
COMMENT ON COLUMN inspeccion.id IS 'Identificador único de la inspección';
COMMENT ON COLUMN inspeccion.nombre IS 'Nombre de la inspección (ej: Inspección Tercera)';

-- ----------------------------------------------------------------------------
-- Tabla: estado
-- Descripción: Catálogo de estados para querellas y despachos comisorios
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estado (
    id                  BIGSERIAL PRIMARY KEY,
    modulo              VARCHAR(20) NOT NULL,
    nombre              VARCHAR(50) NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT estado_modulo_nombre_key UNIQUE (modulo, nombre),
    CONSTRAINT estado_modulo_check CHECK (modulo IN ('QUERELLA', 'DESPACHO'))
);

COMMENT ON TABLE estado IS 'Catálogo de estados del flujo de querellas y despachos';
COMMENT ON COLUMN estado.modulo IS 'Módulo al que pertenece: QUERELLA o DESPACHO';
COMMENT ON COLUMN estado.nombre IS 'Nombre del estado (ej: APERTURA, NOTIFICACION)';

-- ============================================================================
-- NIVEL 2: TABLAS CON FOREIGN KEYS SIMPLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: barrio
-- Descripción: Catálogo de barrios asociados a comunas
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS barrio (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    comuna_id           BIGINT,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_barrio_comuna FOREIGN KEY (comuna_id)
        REFERENCES comuna(id) ON DELETE SET NULL
);

COMMENT ON TABLE barrio IS 'Catálogo de barrios de Neiva asociados a comunas';
COMMENT ON COLUMN barrio.id IS 'Identificador único del barrio';
COMMENT ON COLUMN barrio.nombre IS 'Nombre del barrio';
COMMENT ON COLUMN barrio.comuna_id IS 'Comuna a la que pertenece el barrio';

-- ----------------------------------------------------------------------------
-- Tabla: estado_transicion
-- Descripción: Define las transiciones válidas entre estados
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estado_transicion (
    id                  BIGSERIAL PRIMARY KEY,
    modulo              VARCHAR(20) NOT NULL,
    desde_estado_id     BIGINT NOT NULL,
    hacia_estado_id     BIGINT NOT NULL,

    CONSTRAINT fk_estado_transicion_desde FOREIGN KEY (desde_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT fk_estado_transicion_hacia FOREIGN KEY (hacia_estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT estado_transicion_modulo_desde_hacia_key
        UNIQUE (modulo, desde_estado_id, hacia_estado_id),
    CONSTRAINT estado_transicion_modulo_check CHECK (modulo IN ('QUERELLA', 'DESPACHO'))
);

COMMENT ON TABLE estado_transicion IS 'Define las transiciones permitidas entre estados';
COMMENT ON COLUMN estado_transicion.modulo IS 'Módulo al que pertenece la transición';
COMMENT ON COLUMN estado_transicion.desde_estado_id IS 'Estado origen';
COMMENT ON COLUMN estado_transicion.hacia_estado_id IS 'Estado destino';

-- ----------------------------------------------------------------------------
-- Tabla: historial_estado
-- Descripción: Registra todos los cambios de estado (auditoría)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_estado (
    id                  BIGSERIAL PRIMARY KEY,
    modulo              VARCHAR(20) NOT NULL,
    caso_id             BIGINT NOT NULL,
    estado_id           BIGINT NOT NULL,
    motivo              TEXT NOT NULL,
    usuario_id          BIGINT,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_historial_estado FOREIGN KEY (estado_id)
        REFERENCES estado(id) ON DELETE CASCADE,
    CONSTRAINT historial_estado_modulo_check CHECK (modulo IN ('QUERELLA', 'DESPACHO'))
);

COMMENT ON TABLE historial_estado IS 'Historial de cambios de estado para auditoría';
COMMENT ON COLUMN historial_estado.modulo IS 'Módulo: QUERELLA o DESPACHO';
COMMENT ON COLUMN historial_estado.caso_id IS 'ID de la querella o despacho';
COMMENT ON COLUMN historial_estado.estado_id IS 'Estado aplicado';
COMMENT ON COLUMN historial_estado.motivo IS 'Razón del cambio de estado';
COMMENT ON COLUMN historial_estado.usuario_id IS 'Usuario que realizó el cambio';

-- ----------------------------------------------------------------------------
-- Tabla: usuarios
-- Descripción: Usuarios del sistema (inspectores, directores, auxiliares)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    email               VARCHAR(100) NOT NULL,
    telefono            VARCHAR(20),
    password            VARCHAR(100) NOT NULL,
    rol                 VARCHAR(20) NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    corregimiento_id    BIGINT,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT usuarios_email_key UNIQUE (email),
    CONSTRAINT fk_usuario_corregimiento FOREIGN KEY (corregimiento_id)
        REFERENCES corregimiento(id) ON DELETE SET NULL,
    CONSTRAINT usuarios_rol_check CHECK (rol IN ('INSPECTOR', 'DIRECTOR', 'AUXILIAR')),
    CONSTRAINT usuarios_estado_check CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'NO_DISPONIBLE'))
);

COMMENT ON TABLE usuarios IS 'Usuarios del sistema con roles y permisos';
COMMENT ON COLUMN usuarios.id IS 'Identificador único del usuario';
COMMENT ON COLUMN usuarios.nombre IS 'Nombre completo del usuario';
COMMENT ON COLUMN usuarios.email IS 'Correo electrónico (único)';
COMMENT ON COLUMN usuarios.password IS 'Contraseña hasheada con BCrypt';
COMMENT ON COLUMN usuarios.rol IS 'Rol: INSPECTOR, DIRECTOR, AUXILIAR';
COMMENT ON COLUMN usuarios.estado IS 'Estado: ACTIVO, BLOQUEADO, NO_DISPONIBLE';
COMMENT ON COLUMN usuarios.corregimiento_id IS 'Corregimiento asignado (solo para inspectores)';

-- ============================================================================
-- NIVEL 3: TABLAS CON FOREIGN KEYS MÚLTIPLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: querella
-- Descripción: Querellas/casos del sistema
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS querella (
    id                      BIGSERIAL PRIMARY KEY,
    radicado_interno        VARCHAR(20),
    direccion               TEXT NOT NULL,
    descripcion             TEXT NOT NULL,
    tema_id                 BIGINT,
    naturaleza              VARCHAR(20) NOT NULL,
    inspeccion_id           BIGINT,
    comuna_id               BIGINT,
    barrio_id               BIGINT,
    corregimiento_id        BIGINT,
    id_alcaldia             VARCHAR(50),
    es_migrado              BOOLEAN NOT NULL DEFAULT FALSE,
    creado_por              BIGINT,
    creado_en               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    id_local                VARCHAR(20),
    querellante_nombre      VARCHAR(200),
    querellante_contacto    VARCHAR(100),
    genero_querellante      VARCHAR(20),
    genero_querellado       VARCHAR(20),
    normas_aplicables       VARCHAR(1024),
    observaciones           TEXT,
    tiene_fallo             BOOLEAN,
    tiene_apelacion         BOOLEAN,
    archivado               BOOLEAN,
    materializacion_medida  BOOLEAN,

    CONSTRAINT fk_querella_tema FOREIGN KEY (tema_id)
        REFERENCES tema(id) ON DELETE SET NULL,
    CONSTRAINT fk_querella_inspeccion FOREIGN KEY (inspeccion_id)
        REFERENCES inspeccion(id) ON DELETE SET NULL,
    CONSTRAINT fk_querella_comuna FOREIGN KEY (comuna_id)
        REFERENCES comuna(id) ON DELETE SET NULL,
    CONSTRAINT fk_querella_barrio FOREIGN KEY (barrio_id)
        REFERENCES barrio(id) ON DELETE SET NULL,
    CONSTRAINT fk_querella_corregimiento FOREIGN KEY (corregimiento_id)
        REFERENCES corregimiento(id) ON DELETE SET NULL,
    CONSTRAINT querella_naturaleza_check CHECK (naturaleza IN ('OFICIO', 'PERSONA', 'ANONIMA'))
);

COMMENT ON TABLE querella IS 'Querellas o casos del sistema';
COMMENT ON COLUMN querella.radicado_interno IS 'Número de radicado interno generado automáticamente';
COMMENT ON COLUMN querella.naturaleza IS 'Tipo: OFICIO, PERSONA, ANONIMA';
COMMENT ON COLUMN querella.inspeccion_id IS '[DEPRECATED] Usar corregimiento_id';
COMMENT ON COLUMN querella.corregimiento_id IS 'Corregimiento asignado para la querella';
COMMENT ON COLUMN querella.es_migrado IS 'Indica si el registro fue migrado de un sistema anterior';

-- ----------------------------------------------------------------------------
-- Tabla: despacho_comisorio
-- Descripción: Despachos comisorios recibidos de otras entidades
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS despacho_comisorio (
    id                      BIGSERIAL PRIMARY KEY,
    fecha_recibido          DATE NOT NULL,
    radicado_proceso        VARCHAR(100) NOT NULL,
    numero_despacho         VARCHAR(100) NOT NULL,
    entidad_procedente      VARCHAR(300) NOT NULL,
    asunto                  VARCHAR(1000) NOT NULL,
    demandante_apoderado    VARCHAR(500),
    corregimiento_id        BIGINT,
    fecha_devolucion        DATE,
    creado_por              BIGINT,
    creado_en               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_despacho_corregimiento FOREIGN KEY (corregimiento_id)
        REFERENCES corregimiento(id) ON DELETE SET NULL
);

COMMENT ON TABLE despacho_comisorio IS 'Despachos comisorios recibidos de juzgados y otras entidades';
COMMENT ON COLUMN despacho_comisorio.fecha_recibido IS 'Fecha en que se recibió el despacho';
COMMENT ON COLUMN despacho_comisorio.radicado_proceso IS 'Radicado del proceso judicial';
COMMENT ON COLUMN despacho_comisorio.numero_despacho IS 'Número del despacho comisorio';
COMMENT ON COLUMN despacho_comisorio.entidad_procedente IS 'Juzgado o entidad que envía el despacho';
COMMENT ON COLUMN despacho_comisorio.asunto IS 'Descripción del asunto del despacho';
COMMENT ON COLUMN despacho_comisorio.demandante_apoderado IS 'Nombre del demandante y/o apoderado';
COMMENT ON COLUMN despacho_comisorio.corregimiento_id IS 'Corregimiento asignado para tramitar';
COMMENT ON COLUMN despacho_comisorio.fecha_devolucion IS 'Fecha en que se devolvió al juzgado';

-- ============================================================================
-- NIVEL 4: TABLAS QUE DEPENDEN DE USUARIOS Y QUERELLAS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: notificaciones
-- Descripción: Notificaciones del sistema para usuarios
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notificaciones (
    id                  BIGSERIAL PRIMARY KEY,
    titulo              VARCHAR(200) NOT NULL,
    mensaje             VARCHAR(500) NOT NULL,
    tipo                VARCHAR(20) NOT NULL,
    leida               BOOLEAN NOT NULL DEFAULT FALSE,
    querella_id         BIGINT,
    usuario_id          BIGINT NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_notificacion_querella FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT notificaciones_tipo_check
        CHECK (tipo IN ('ASIGNACION', 'CAMBIO_ESTADO', 'COMENTARIO', 'SISTEMA', 'RECORDATORIO'))
);

COMMENT ON TABLE notificaciones IS 'Notificaciones internas del sistema para usuarios';
COMMENT ON COLUMN notificaciones.tipo IS 'Tipo: ASIGNACION, CAMBIO_ESTADO, COMENTARIO, SISTEMA, RECORDATORIO';
COMMENT ON COLUMN notificaciones.leida IS 'Indica si la notificación fue leída';

-- ----------------------------------------------------------------------------
-- Tabla: adjuntos
-- Descripción: Archivos adjuntos a las querellas
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS adjuntos (
    id                  BIGSERIAL PRIMARY KEY,
    querella_id         BIGINT NOT NULL,
    nombre_archivo      VARCHAR(255) NOT NULL,
    tipo_archivo        VARCHAR(100) NOT NULL,
    tamano_bytes        BIGINT NOT NULL,
    ruta_storage        VARCHAR(500) NOT NULL,
    descripcion         VARCHAR(500),
    cargado_por         BIGINT NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_adjunto_querella FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT fk_adjunto_usuario FOREIGN KEY (cargado_por)
        REFERENCES usuarios(id) ON DELETE CASCADE
);

COMMENT ON TABLE adjuntos IS 'Archivos adjuntos a las querellas (PDFs, imágenes, documentos)';
COMMENT ON COLUMN adjuntos.nombre_archivo IS 'Nombre original del archivo';
COMMENT ON COLUMN adjuntos.tipo_archivo IS 'MIME type (ej: application/pdf, image/jpeg)';
COMMENT ON COLUMN adjuntos.tamano_bytes IS 'Tamaño del archivo en bytes';
COMMENT ON COLUMN adjuntos.ruta_storage IS 'Ruta donde se almacena el archivo';

-- ----------------------------------------------------------------------------
-- Tabla: comunicaciones
-- Descripción: Comunicaciones oficiales relacionadas con querellas
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comunicaciones (
    id                  BIGSERIAL PRIMARY KEY,
    querella_id         BIGINT NOT NULL,
    tipo                VARCHAR(20) NOT NULL,
    numero_radicado     VARCHAR(50),
    asunto              VARCHAR(300) NOT NULL,
    contenido           TEXT,
    fecha_envio         DATE,
    destinatario        VARCHAR(200) NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_por          BIGINT NOT NULL,
    creado_en           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_comunicacion_querella FOREIGN KEY (querella_id)
        REFERENCES querella(id) ON DELETE CASCADE,
    CONSTRAINT fk_comunicacion_usuario FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT comunicaciones_tipo_check
        CHECK (tipo IN ('OFICIO', 'NOTIFICACION', 'CITACION', 'AUTO', 'RESOLUCION')),
    CONSTRAINT comunicaciones_estado_check
        CHECK (estado IN ('BORRADOR', 'ENVIADO', 'RECIBIDO'))
);

COMMENT ON TABLE comunicaciones IS 'Comunicaciones oficiales (oficios, notificaciones, citaciones)';
COMMENT ON COLUMN comunicaciones.tipo IS 'Tipo: OFICIO, NOTIFICACION, CITACION, AUTO, RESOLUCION';
COMMENT ON COLUMN comunicaciones.estado IS 'Estado: BORRADOR, ENVIADO, RECIBIDO';
COMMENT ON COLUMN comunicaciones.destinatario IS 'Persona o entidad a quien se dirige';

-- ============================================================================
-- ÍNDICES PARA OPTIMIZACIÓN DE CONSULTAS
-- ============================================================================

-- Índices en tabla barrio
CREATE INDEX IF NOT EXISTS idx_barrio_comuna ON barrio(comuna_id);

-- Índices en tabla usuarios
CREATE INDEX IF NOT EXISTS idx_usuario_corregimiento ON usuarios(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuarios(rol);
CREATE INDEX IF NOT EXISTS idx_usuario_estado ON usuarios(estado);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuarios(email);

-- Índices en tabla querella
CREATE INDEX IF NOT EXISTS idx_querella_tema ON querella(tema_id);
CREATE INDEX IF NOT EXISTS idx_querella_comuna ON querella(comuna_id);
CREATE INDEX IF NOT EXISTS idx_querella_barrio ON querella(barrio_id);
CREATE INDEX IF NOT EXISTS idx_querella_corregimiento ON querella(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_querella_naturaleza ON querella(naturaleza);
CREATE INDEX IF NOT EXISTS idx_querella_creado_en ON querella(creado_en DESC);
CREATE INDEX IF NOT EXISTS idx_querella_radicado ON querella(radicado_interno);

-- Índices en tabla despacho_comisorio
CREATE INDEX IF NOT EXISTS idx_despacho_corregimiento ON despacho_comisorio(corregimiento_id);
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_recibido ON despacho_comisorio(fecha_recibido DESC);
CREATE INDEX IF NOT EXISTS idx_despacho_fecha_devolucion ON despacho_comisorio(fecha_devolucion);
CREATE INDEX IF NOT EXISTS idx_despacho_entidad ON despacho_comisorio(entidad_procedente);

-- Índices en tabla historial_estado
CREATE INDEX IF NOT EXISTS idx_historial_modulo_caso ON historial_estado(modulo, caso_id);
CREATE INDEX IF NOT EXISTS idx_historial_estado ON historial_estado(estado_id);
CREATE INDEX IF NOT EXISTS idx_historial_creado_en ON historial_estado(creado_en DESC);

-- Índices en tabla notificaciones
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario ON notificaciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_querella ON notificaciones(querella_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_leida ON notificaciones(leida);

-- Índices en tabla adjuntos
CREATE INDEX IF NOT EXISTS idx_adjunto_querella ON adjuntos(querella_id);
CREATE INDEX IF NOT EXISTS idx_adjunto_usuario ON adjuntos(cargado_por);

-- Índices en tabla comunicaciones
CREATE INDEX IF NOT EXISTS idx_comunicacion_querella ON comunicaciones(querella_id);
CREATE INDEX IF NOT EXISTS idx_comunicacion_estado ON comunicaciones(estado);
CREATE INDEX IF NOT EXISTS idx_comunicacion_tipo ON comunicaciones(tipo);

-- Índices en tabla estado_transicion
CREATE INDEX IF NOT EXISTS idx_estado_transicion_modulo ON estado_transicion(modulo);

-- ============================================================================
-- SECUENCIAS Y VALORES INICIALES
-- ============================================================================

-- Las secuencias se crean automáticamente con BIGSERIAL
-- Asegurar que empiecen en 1
SELECT setval('comuna_id_seq', 1, false);
SELECT setval('corregimiento_id_seq', 1, false);
SELECT setval('tema_id_seq', 1, false);
SELECT setval('inspeccion_id_seq', 1, false);
SELECT setval('estado_id_seq', 1, false);
SELECT setval('barrio_id_seq', 1, false);
SELECT setval('estado_transicion_id_seq', 1, false);
SELECT setval('historial_estado_id_seq', 1, false);
SELECT setval('usuarios_id_seq', 1, false);
SELECT setval('querella_id_seq', 1, false);
SELECT setval('despacho_comisorio_id_seq', 1, false);
SELECT setval('notificaciones_id_seq', 1, false);
SELECT setval('adjuntos_id_seq', 1, false);
SELECT setval('comunicaciones_id_seq', 1, false);

-- ============================================================================
-- DATOS INICIALES - ESTADOS DE QUERELLAS
-- ============================================================================

INSERT INTO estado (modulo, nombre, creado_en) VALUES
('QUERELLA', 'APERTURA', now()),
('QUERELLA', 'NOTIFICACION', now()),
('QUERELLA', 'AUDIENCIA_PUBLICA', now()),
('QUERELLA', 'DECISION', now()),
('QUERELLA', 'RECURSO', now()),
('QUERELLA', 'INADMISIBLE', now())
ON CONFLICT (modulo, nombre) DO NOTHING;

-- ============================================================================
-- TRANSICIONES DE ESTADOS PARA QUERELLAS
-- ============================================================================

-- De APERTURA puede ir a NOTIFICACION o INADMISIBLE
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
);

INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'INADMISIBLE')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'INADMISIBLE')
);

-- De NOTIFICACION puede ir a AUDIENCIA_PUBLICA
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
);

-- De AUDIENCIA_PUBLICA puede ir a DECISION
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
);

-- De DECISION puede ir a RECURSO
INSERT INTO estado_transicion (modulo, desde_estado_id, hacia_estado_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECURSO')
WHERE NOT EXISTS (
    SELECT 1 FROM estado_transicion
    WHERE modulo = 'QUERELLA'
    AND desde_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION')
    AND hacia_estado_id = (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECURSO')
);

-- ============================================================================
-- VERIFICACIÓN DE CREACIÓN
-- ============================================================================

DO $$
DECLARE
    tabla RECORD;
    contador INTEGER := 0;
BEGIN
    FOR tabla IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_type = 'BASE TABLE'
        ORDER BY table_name
    LOOP
        contador := contador + 1;
        RAISE NOTICE 'Tabla creada: %', tabla.table_name;
    END LOOP;

    RAISE NOTICE '';
    RAISE NOTICE '============================================================================';
    RAISE NOTICE 'RESUMEN DE CREACIÓN';
    RAISE NOTICE '============================================================================';
    RAISE NOTICE 'Total de tablas creadas: %', contador;
    RAISE NOTICE 'Estados QUERELLA: %', (SELECT COUNT(*) FROM estado WHERE modulo = 'QUERELLA');
    RAISE NOTICE 'Transiciones QUERELLA: %', (SELECT COUNT(*) FROM estado_transicion WHERE modulo = 'QUERELLA');
    RAISE NOTICE '============================================================================';
    RAISE NOTICE '✅ Base de datos creada exitosamente';
    RAISE NOTICE '============================================================================';
END $$;

-- ============================================================================
-- SIGUIENTE PASO: EJECUTAR EL SCRIPT DE CATÁLOGOS
-- ============================================================================
-- Ejecutar después de este script:
-- \i catalogos-neiva.sql
-- ============================================================================
