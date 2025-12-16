-- =====================================================
-- SCRIPT DE ROLLBACK - SISTEMA DE QUERELLAS
-- Inspecciones de Policía - Alcaldía de Neiva
-- =====================================================
-- ⚠️ ADVERTENCIA: Este script ELIMINARÁ TODOS los datos
-- y estructura de la base de datos.
-- USE SOLO SI NECESITA REINSTALAR COMPLETAMENTE.
-- =====================================================

\echo '========================================'
\echo '⚠️  SCRIPT DE ROLLBACK  ⚠️'
\echo '========================================'
\echo 'Este script eliminará TODAS las tablas y datos.'
\echo 'Presione Ctrl+C para cancelar en los próximos 5 segundos...'
\echo ''

-- Pausa de seguridad (comentar para ejecución automática)
SELECT pg_sleep(5);

\echo 'Iniciando rollback...'
\echo ''

-- Deshabilitar restricciones temporalmente
SET session_replication_role = replica;

-- =====================================================
-- 1. ELIMINAR TRIGGERS
-- =====================================================

\echo '1. Eliminando triggers...'

DROP TRIGGER IF EXISTS trigger_generar_id_local ON querella;

\echo '   ✓ Triggers eliminados'
\echo ''

-- =====================================================
-- 2. ELIMINAR FUNCIONES
-- =====================================================

\echo '2. Eliminando funciones...'

DROP FUNCTION IF EXISTS obtener_estado_actual_querella(BIGINT);
DROP FUNCTION IF EXISTS generar_id_local_querella();

\echo '   ✓ Funciones eliminadas'
\echo ''

-- =====================================================
-- 3. ELIMINAR TABLAS (en orden para respetar FK)
-- =====================================================

\echo '3. Eliminando tablas...'

-- Tablas dependientes primero
DROP TABLE IF EXISTS notificaciones CASCADE;
DROP TABLE IF EXISTS adjuntos CASCADE;
DROP TABLE IF EXISTS comunicaciones CASCADE;
DROP TABLE IF EXISTS historial_estado CASCADE;
DROP TABLE IF EXISTS estado_transicion CASCADE;
DROP TABLE IF EXISTS querella CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- Tablas de catálogos
DROP TABLE IF EXISTS estado CASCADE;
DROP TABLE IF EXISTS comuna CASCADE;
DROP TABLE IF EXISTS tema CASCADE;
DROP TABLE IF EXISTS inspeccion CASCADE;

\echo '   ✓ Tablas eliminadas'
\echo ''

-- =====================================================
-- 4. ELIMINAR SECUENCIAS
-- =====================================================

\echo '4. Eliminando secuencias...'

DROP SEQUENCE IF EXISTS seq_radicado_querella;

-- Secuencias de IDs (auto-creadas por SERIAL)
DROP SEQUENCE IF EXISTS inspeccion_id_seq;
DROP SEQUENCE IF EXISTS tema_id_seq;
DROP SEQUENCE IF EXISTS comuna_id_seq;
DROP SEQUENCE IF EXISTS estado_id_seq;
DROP SEQUENCE IF EXISTS estado_transicion_id_seq;
DROP SEQUENCE IF EXISTS usuarios_id_seq;
DROP SEQUENCE IF EXISTS querella_id_seq;
DROP SEQUENCE IF EXISTS comunicaciones_id_seq;
DROP SEQUENCE IF EXISTS adjuntos_id_seq;
DROP SEQUENCE IF EXISTS notificaciones_id_seq;
DROP SEQUENCE IF EXISTS historial_estado_id_seq;

\echo '   ✓ Secuencias eliminadas'
\echo ''

-- Restaurar restricciones
SET session_replication_role = DEFAULT;

-- =====================================================
-- 5. VERIFICACIÓN DE LIMPIEZA
-- =====================================================

\echo '5. Verificando limpieza...'

DO $$
DECLARE
    v_tablas INTEGER;
    v_secuencias INTEGER;
    v_funciones INTEGER;
    v_triggers INTEGER;
BEGIN
    -- Contar elementos restantes
    SELECT COUNT(*) INTO v_tablas
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

    SELECT COUNT(*) INTO v_secuencias
    FROM information_schema.sequences
    WHERE sequence_schema = 'public';

    SELECT COUNT(*) INTO v_funciones
    FROM information_schema.routines
    WHERE routine_schema = 'public' AND routine_type = 'FUNCTION'
      AND routine_name NOT LIKE 'pg_%';

    SELECT COUNT(*) INTO v_triggers
    FROM information_schema.triggers
    WHERE trigger_schema = 'public';

    RAISE NOTICE '   Tablas restantes: %', v_tablas;
    RAISE NOTICE '   Secuencias restantes: %', v_secuencias;
    RAISE NOTICE '   Funciones restantes: %', v_funciones;
    RAISE NOTICE '   Triggers restantes: %', v_triggers;

    IF v_tablas = 0 AND v_funciones = 0 AND v_triggers = 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '✓✓✓ ROLLBACK COMPLETADO EXITOSAMENTE ✓✓✓';
        RAISE NOTICE 'Base de datos limpiada completamente';
    ELSE
        RAISE WARNING '';
        RAISE WARNING '⚠ ADVERTENCIA: Aún quedan elementos en la base de datos';
        RAISE WARNING 'Puede ser necesario eliminar manualmente';
    END IF;
END $$;

\echo ''
\echo '========================================'
\echo 'ROLLBACK FINALIZADO'
\echo '========================================'
\echo 'La base de datos ha sido limpiada.'
\echo 'Puede ejecutar schema.sql y data.sql nuevamente.'
\echo ''
