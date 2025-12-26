-- =====================================================
-- VERIFICACIÓN DE INSTALACIÓN
-- Sistema de Querellas - Alcaldía de Neiva
-- =====================================================
-- Este script verifica que la instalación esté completa
-- y que todos los componentes estén correctamente configurados
-- =====================================================

\echo ''
\echo '🔍 VERIFICACIÓN DE INSTALACIÓN DEL SISTEMA'
\echo '=========================================='
\echo ''

-- =====================================================
-- 1. VERIFICAR TABLAS
-- =====================================================

\echo '📋 1. VERIFICANDO TABLAS...'
\echo ''

SELECT
    '✅ Tablas creadas' AS status,
    COUNT(*) AS cantidad,
    STRING_AGG(tablename, ', ' ORDER BY tablename) AS tablas
FROM pg_tables
WHERE schemaname = 'public';

-- Verificar tablas específicas esperadas
WITH tablas_esperadas AS (
    SELECT unnest(ARRAY[
        'usuarios', 'querella', 'despacho_comisorio',
        'comunicaciones', 'adjuntos', 'notificaciones',
        'tema', 'comuna', 'estado', 'estado_transicion',
        'historial_estado', 'configuracion_sistema'
    ]) AS tabla_nombre
),
tablas_existentes AS (
    SELECT tablename FROM pg_tables WHERE schemaname = 'public'
)
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM tablas_esperadas) THEN '✅ Todas las tablas requeridas existen'
        ELSE '⚠️  Faltan ' || ((SELECT COUNT(*) FROM tablas_esperadas) - COUNT(*))::text || ' tablas'
    END AS resultado,
    COUNT(*) AS encontradas,
    (SELECT COUNT(*) FROM tablas_esperadas) AS esperadas
FROM tablas_esperadas te
WHERE EXISTS (SELECT 1 FROM tablas_existentes WHERE tablename = te.tabla_nombre);

\echo ''

-- =====================================================
-- 2. VERIFICAR ÍNDICES
-- =====================================================

\echo '🔍 2. VERIFICANDO ÍNDICES...'
\echo ''

SELECT
    '✅ Índices creados' AS status,
    COUNT(*) AS cantidad
FROM pg_indexes
WHERE schemaname = 'public';

-- Verificar índices críticos
SELECT
    '✅ Índices críticos' AS tipo,
    COUNT(*) FILTER (WHERE indexname LIKE 'idx_%') AS indices_normales,
    COUNT(*) FILTER (WHERE indexname LIKE '%_pkey') AS primary_keys,
    COUNT(*) FILTER (WHERE indexname LIKE '%_gin') AS indices_fulltext
FROM pg_indexes
WHERE schemaname = 'public';

\echo ''

-- =====================================================
-- 3. VERIFICAR CONSTRAINTS
-- =====================================================

\echo '🔒 3. VERIFICANDO CONSTRAINTS...'
\echo ''

SELECT
    '✅ Constraints configurados' AS status,
    COUNT(*) FILTER (WHERE contype = 'f') AS foreign_keys,
    COUNT(*) FILTER (WHERE contype = 'c') AS check_constraints,
    COUNT(*) FILTER (WHERE contype = 'u') AS unique_constraints,
    COUNT(*) FILTER (WHERE contype = 'p') AS primary_keys
FROM pg_constraint
WHERE connamespace = 'public'::regnamespace;

-- Verificar constraint crítico de usuarios.rol
SELECT
    CASE
        WHEN pg_get_constraintdef(oid) LIKE '%DIRECTORA%' THEN '✅ Constraint usuarios.rol CORRECTO (incluye DIRECTORA)'
        ELSE '❌ Constraint usuarios.rol INCORRECTO (falta DIRECTORA)'
    END AS verificacion,
    pg_get_constraintdef(oid) AS definicion
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass
  AND conname = 'usuarios_rol_check';

\echo ''

-- =====================================================
-- 4. VERIFICAR AUDITORÍA (CAMPOS)
-- =====================================================

\echo '📝 4. VERIFICANDO CAMPOS DE AUDITORÍA...'
\echo ''

WITH auditoria_esperada AS (
    SELECT
        table_name,
        BOOL_OR(column_name = 'creado_en') AS tiene_creado_en,
        BOOL_OR(column_name = 'creado_por') AS tiene_creado_por,
        BOOL_OR(column_name = 'actualizado_en') AS tiene_actualizado_en,
        BOOL_OR(column_name = 'actualizado_por') AS tiene_actualizado_por
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name IN ('usuarios', 'querella', 'despacho_comisorio', 'comunicaciones',
                         'adjuntos', 'notificaciones', 'tema', 'comuna', 'estado',
                         'estado_transicion', 'historial_estado', 'configuracion_sistema')
    GROUP BY table_name
)
SELECT
    '📊 Cobertura de Auditoría' AS tipo,
    COUNT(*) FILTER (WHERE tiene_creado_en AND tiene_actualizado_en) AS con_timestamps,
    COUNT(*) FILTER (WHERE tiene_creado_por AND tiene_actualizado_por) AS con_usuarios,
    COUNT(*) AS total_tablas,
    ROUND(100.0 * COUNT(*) FILTER (WHERE tiene_creado_en AND tiene_creado_por AND tiene_actualizado_en AND tiene_actualizado_por) / COUNT(*), 1) || '%' AS cobertura_completa
FROM auditoria_esperada;

-- Detalle por tabla
SELECT
    table_name AS tabla,
    CASE WHEN tiene_creado_en THEN '✅' ELSE '❌' END AS creado_en,
    CASE WHEN tiene_creado_por THEN '✅' ELSE '❌' END AS creado_por,
    CASE WHEN tiene_actualizado_en THEN '✅' ELSE '❌' END AS actualizado_en,
    CASE WHEN tiene_actualizado_por THEN '✅' ELSE '❌' END AS actualizado_por,
    CASE
        WHEN tiene_creado_en AND tiene_creado_por AND tiene_actualizado_en AND tiene_actualizado_por THEN '✅ 100%'
        WHEN tiene_creado_en AND tiene_actualizado_en THEN '⚠️  75%'
        WHEN tiene_creado_en THEN '⚠️  50%'
        ELSE '❌ 0%'
    END AS score
FROM (
    SELECT
        table_name,
        BOOL_OR(column_name = 'creado_en') AS tiene_creado_en,
        BOOL_OR(column_name = 'creado_por') AS tiene_creado_por,
        BOOL_OR(column_name = 'actualizado_en') AS tiene_actualizado_en,
        BOOL_OR(column_name = 'actualizado_por') AS tiene_actualizado_por
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name IN ('usuarios', 'querella', 'despacho_comisorio', 'comunicaciones',
                         'adjuntos', 'notificaciones', 'tema', 'comuna', 'estado',
                         'estado_transicion', 'historial_estado', 'configuracion_sistema')
    GROUP BY table_name
) sub
ORDER BY table_name;

\echo ''

-- =====================================================
-- 5. VERIFICAR TRIGGERS
-- =====================================================

\echo '⚡ 5. VERIFICANDO TRIGGERS...'
\echo ''

SELECT
    '✅ Triggers configurados' AS status,
    COUNT(*) AS cantidad
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relnamespace = 'public'::regnamespace
  AND NOT t.tgisinternal;

-- Detalle de triggers
SELECT
    c.relname AS tabla,
    t.tgname AS trigger,
    CASE
        WHEN t.tgname LIKE '%actualizar%' THEN '🔄 Actualización automática'
        WHEN t.tgname LIKE '%generar%' THEN '🏷️  Generación de ID'
        WHEN t.tgname LIKE '%tsvector%' THEN '🔍 Búsqueda fulltext'
        ELSE '⚙️  Otro'
    END AS tipo
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relnamespace = 'public'::regnamespace
  AND NOT t.tgisinternal
ORDER BY c.relname, t.tgname;

\echo ''

-- =====================================================
-- 6. VERIFICAR FUNCIONES
-- =====================================================

\echo '🔧 6. VERIFICANDO FUNCIONES...'
\echo ''

SELECT
    '✅ Funciones creadas' AS status,
    COUNT(*) AS cantidad
FROM pg_proc p
JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE n.nspname = 'public'
  AND p.prokind = 'f';

-- Detalle de funciones
SELECT
    p.proname AS funcion,
    pg_get_function_arguments(p.oid) AS argumentos,
    pg_get_function_result(p.oid) AS retorna
FROM pg_proc p
JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE n.nspname = 'public'
  AND p.prokind = 'f'
ORDER BY p.proname;

\echo ''

-- =====================================================
-- 7. VERIFICAR DATOS INICIALES
-- =====================================================

\echo '🌱 7. VERIFICANDO DATOS INICIALES...'
\echo ''

-- Estados
SELECT
    '✅ Estados cargados' AS tipo,
    COUNT(*) FILTER (WHERE modulo = 'QUERELLA') AS querellas,
    COUNT(*) FILTER (WHERE modulo = 'DESPACHO') AS despachos,
    COUNT(*) AS total
FROM estado;

-- Transiciones
SELECT
    '✅ Transiciones configuradas' AS tipo,
    COUNT(*) FILTER (WHERE modulo = 'QUERELLA') AS querellas,
    COUNT(*) FILTER (WHERE modulo = 'DESPACHO') AS despachos,
    COUNT(*) AS total
FROM estado_transicion;

-- Catálogos
SELECT
    '✅ Temas' AS tipo,
    COUNT(*) AS cantidad
FROM tema
UNION ALL
SELECT
    '✅ Comunas',
    COUNT(*)
FROM comuna
UNION ALL
SELECT
    '✅ Configuraciones',
    COUNT(*)
FROM configuracion_sistema
UNION ALL
SELECT
    '✅ Usuarios iniciales',
    COUNT(*)
FROM usuarios;

\echo ''

-- =====================================================
-- 8. VERIFICAR BÚSQUEDA FULLTEXT
-- =====================================================

\echo '🔍 8. VERIFICANDO BÚSQUEDA FULLTEXT...'
\echo ''

SELECT
    CASE
        WHEN COUNT(*) > 0 THEN '✅ Columna busqueda_tsvector existe en querella'
        ELSE '❌ Falta columna busqueda_tsvector'
    END AS verificacion
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'querella'
  AND column_name = 'busqueda_tsvector';

SELECT
    CASE
        WHEN COUNT(*) > 0 THEN '✅ Índice GIN existe para búsqueda fulltext'
        ELSE '❌ Falta índice GIN'
    END AS verificacion
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'querella'
  AND indexname LIKE '%gin%';

\echo ''

-- =====================================================
-- 9. VERIFICAR SECUENCIAS
-- =====================================================

\echo '🔢 9. VERIFICANDO SECUENCIAS...'
\echo ''

SELECT
    '✅ Secuencias creadas' AS status,
    COUNT(*) AS cantidad
FROM information_schema.sequences
WHERE sequence_schema = 'public';

SELECT
    sequence_name AS secuencia,
    data_type AS tipo,
    start_value AS inicio,
    increment AS incremento
FROM information_schema.sequences
WHERE sequence_schema = 'public'
ORDER BY sequence_name;

\echo ''

-- =====================================================
-- 10. RESUMEN FINAL
-- =====================================================

\echo '📊 10. RESUMEN FINAL'
\echo ''

WITH verificaciones AS (
    SELECT
        (SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public') >= 12 AS tablas_ok,
        (SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public') >= 60 AS indices_ok,
        (SELECT COUNT(*) FROM estado) >= 15 AS estados_ok,
        (SELECT COUNT(*) FROM tema) >= 5 AS temas_ok,
        (SELECT COUNT(*) FROM comuna) >= 10 AS comunas_ok,
        (SELECT COUNT(*) FROM usuarios) >= 1 AS usuarios_ok,
        EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conrelid = 'usuarios'::regclass
              AND conname = 'usuarios_rol_check'
              AND pg_get_constraintdef(oid) LIKE '%DIRECTORA%'
        ) AS constraint_rol_ok,
        (SELECT COUNT(*) FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid
         WHERE c.relnamespace = 'public'::regnamespace AND NOT t.tgisinternal) >= 10 AS triggers_ok,
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'querella' AND column_name = 'busqueda_tsvector'
        ) AS fulltext_ok
)
SELECT
    '🎯 RESULTADO DE LA INSTALACIÓN' AS titulo,
    CASE
        WHEN tablas_ok AND indices_ok AND estados_ok AND temas_ok AND comunas_ok
             AND usuarios_ok AND constraint_rol_ok AND triggers_ok AND fulltext_ok
        THEN '✅ INSTALACIÓN COMPLETA Y CORRECTA'
        ELSE '⚠️  INSTALACIÓN INCOMPLETA - Revisar arriba'
    END AS estado,
    CASE WHEN tablas_ok THEN '✅' ELSE '❌' END AS tablas,
    CASE WHEN indices_ok THEN '✅' ELSE '❌' END AS indices,
    CASE WHEN estados_ok THEN '✅' ELSE '❌' END AS estados,
    CASE WHEN temas_ok THEN '✅' ELSE '❌' END AS temas,
    CASE WHEN comunas_ok THEN '✅' ELSE '❌' END AS comunas,
    CASE WHEN usuarios_ok THEN '✅' ELSE '❌' END AS usuarios,
    CASE WHEN constraint_rol_ok THEN '✅' ELSE '❌' END AS constraint_rol,
    CASE WHEN triggers_ok THEN '✅' ELSE '❌' END AS triggers,
    CASE WHEN fulltext_ok THEN '✅' ELSE '❌' END AS fulltext
FROM verificaciones;

\echo ''
\echo '=========================================='
\echo '✅ Verificación completada'
\echo '=========================================='
\echo ''
