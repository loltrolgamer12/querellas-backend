-- =====================================================
-- SCRIPT DE VERIFICACIÓN DE INSTALACIÓN
-- Sistema de Querellas - Alcaldía de Neiva
-- =====================================================

\echo '========================================'
\echo 'VERIFICACIÓN DE INSTALACIÓN'
\echo '========================================'
\echo ''

-- 1. Verificar que estamos en la base de datos correcta
\echo '1. Base de datos actual:'
SELECT current_database();
\echo ''

-- 2. Verificar tablas creadas
\echo '2. Tablas creadas (esperadas: 11):'
SELECT COUNT(*) as total_tablas
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE';
\echo ''

-- 3. Listar todas las tablas
\echo '3. Lista de tablas:'
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
\echo ''

-- 4. Verificar secuencias
\echo '4. Secuencias creadas (esperadas: 1+):'
SELECT sequence_name
FROM information_schema.sequences
WHERE sequence_schema = 'public';
\echo ''

-- 5. Verificar funciones
\echo '5. Funciones creadas:'
SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_type = 'FUNCTION';
\echo ''

-- 6. Verificar triggers
\echo '6. Triggers creados:'
SELECT trigger_name, event_object_table
FROM information_schema.triggers
WHERE trigger_schema = 'public';
\echo ''

-- 7. Verificar índices
\echo '7. Índices creados (muestra primeros 10):'
SELECT tablename, indexname
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname
LIMIT 10;
\echo ''

-- 8. Verificar datos de catálogos
\echo '8. Datos de catálogos cargados:'
SELECT 'Inspecciones' as catalogo, COUNT(*) as cantidad FROM inspeccion
UNION ALL
SELECT 'Comunas', COUNT(*) FROM comuna
UNION ALL
SELECT 'Temas', COUNT(*) FROM tema
UNION ALL
SELECT 'Estados (QUERELLA)', COUNT(*) FROM estado WHERE modulo='QUERELLA'
UNION ALL
SELECT 'Transiciones', COUNT(*) FROM estado_transicion
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM usuarios;
\echo ''

-- 9. Verificar estados de QUERELLA
\echo '9. Estados de QUERELLA disponibles:'
SELECT id, nombre
FROM estado
WHERE modulo = 'QUERELLA'
ORDER BY id;
\echo ''

-- 10. Verificar usuarios creados
\echo '10. Usuarios del sistema:'
SELECT id, nombre, email, rol, estado, inspeccion_id
FROM usuarios
ORDER BY rol, id;
\echo ''

-- 11. Verificar foreign keys
\echo '11. Foreign Keys creados (muestra primeros 10):'
SELECT
    tc.table_name,
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name
LIMIT 10;
\echo ''

-- 12. Verificar constraints únicos
\echo '12. Unique Constraints:'
SELECT
    tc.table_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
WHERE tc.constraint_type = 'UNIQUE'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name;
\echo ''

-- 13. Verificar que el trigger funciona
\echo '13. Prueba de trigger generar_id_local:'
DO $$
DECLARE
    test_querella_id BIGINT;
    test_id_local VARCHAR;
BEGIN
    -- Insertar querella de prueba
    INSERT INTO querella (
        direccion, descripcion, tema_id, naturaleza,
        inspeccion_id, comuna_id, es_migrado, creado_en, actualizado_en
    ) VALUES (
        'TEST - Dirección de prueba',
        'TEST - Descripción de prueba',
        (SELECT id FROM tema LIMIT 1),
        'PERSONA',
        (SELECT id FROM inspeccion LIMIT 1),
        (SELECT id FROM comuna LIMIT 1),
        FALSE,
        NOW(),
        NOW()
    ) RETURNING id, id_local INTO test_querella_id, test_id_local;

    RAISE NOTICE 'Querella de prueba creada: ID=%, id_local=%', test_querella_id, test_id_local;

    -- Limpiar
    DELETE FROM querella WHERE id = test_querella_id;
    RAISE NOTICE 'Querella de prueba eliminada';

    IF test_id_local IS NOT NULL THEN
        RAISE NOTICE '✓ Trigger funciona correctamente';
    ELSE
        RAISE WARNING '✗ Trigger no generó id_local';
    END IF;
END $$;
\echo ''

-- 14. Verificar función obtener_estado_actual_querella
\echo '14. Prueba de función obtener_estado_actual_querella:'
DO $$
DECLARE
    test_querella_id BIGINT;
    test_estado VARCHAR;
    estado_inicial_id BIGINT;
BEGIN
    -- Insertar querella de prueba
    INSERT INTO querella (
        direccion, descripcion, tema_id, naturaleza,
        inspeccion_id, comuna_id, es_migrado, creado_en, actualizado_en
    ) VALUES (
        'TEST - Dirección de prueba',
        'TEST - Descripción de prueba',
        (SELECT id FROM tema LIMIT 1),
        'PERSONA',
        (SELECT id FROM inspeccion LIMIT 1),
        (SELECT id FROM comuna LIMIT 1),
        FALSE,
        NOW(),
        NOW()
    ) RETURNING id INTO test_querella_id;

    -- Insertar estado inicial
    SELECT id INTO estado_inicial_id FROM estado WHERE modulo='QUERELLA' AND nombre='RECIBIDA';

    INSERT INTO historial_estado (modulo, caso_id, estado_id, motivo, usuario_id, creado_en)
    VALUES ('QUERELLA', test_querella_id, estado_inicial_id, 'Prueba', NULL, NOW());

    -- Obtener estado actual
    test_estado := obtener_estado_actual_querella(test_querella_id);

    RAISE NOTICE 'Estado obtenido: %', test_estado;

    -- Limpiar
    DELETE FROM historial_estado WHERE caso_id = test_querella_id AND modulo='QUERELLA';
    DELETE FROM querella WHERE id = test_querella_id;

    IF test_estado = 'RECIBIDA' THEN
        RAISE NOTICE '✓ Función obtener_estado_actual_querella funciona correctamente';
    ELSE
        RAISE WARNING '✗ Función retornó valor incorrecto: %', test_estado;
    END IF;
END $$;
\echo ''

-- 15. Resumen final
\echo '========================================'
\echo 'RESUMEN DE VERIFICACIÓN'
\echo '========================================'

DO $$
DECLARE
    v_tablas INTEGER;
    v_inspecciones INTEGER;
    v_comunas INTEGER;
    v_temas INTEGER;
    v_estados INTEGER;
    v_transiciones INTEGER;
    v_usuarios INTEGER;
    v_errores INTEGER := 0;
BEGIN
    -- Contar elementos
    SELECT COUNT(*) INTO v_tablas FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

    SELECT COUNT(*) INTO v_inspecciones FROM inspeccion;
    SELECT COUNT(*) INTO v_comunas FROM comuna;
    SELECT COUNT(*) INTO v_temas FROM tema;
    SELECT COUNT(*) INTO v_estados FROM estado WHERE modulo='QUERELLA';
    SELECT COUNT(*) INTO v_transiciones FROM estado_transicion;
    SELECT COUNT(*) INTO v_usuarios FROM usuarios;

    -- Verificar valores esperados
    IF v_tablas < 11 THEN
        RAISE WARNING '✗ Faltan tablas (encontradas: %, esperadas: 11+)', v_tablas;
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Tablas: % OK', v_tablas;
    END IF;

    IF v_inspecciones < 1 THEN
        RAISE WARNING '✗ Sin inspecciones cargadas';
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Inspecciones: % OK', v_inspecciones;
    END IF;

    IF v_comunas < 1 THEN
        RAISE WARNING '✗ Sin comunas cargadas';
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Comunas: % OK', v_comunas;
    END IF;

    IF v_temas < 1 THEN
        RAISE WARNING '✗ Sin temas cargados';
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Temas: % OK', v_temas;
    END IF;

    IF v_estados < 10 THEN
        RAISE WARNING '✗ Estados insuficientes (encontrados: %, esperados: 12)', v_estados;
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Estados: % OK', v_estados;
    END IF;

    IF v_transiciones < 10 THEN
        RAISE WARNING '✗ Transiciones insuficientes (encontradas: %)', v_transiciones;
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Transiciones: % OK', v_transiciones;
    END IF;

    IF v_usuarios < 3 THEN
        RAISE WARNING '✗ Usuarios insuficientes (encontrados: %, esperados: 3+)', v_usuarios;
        v_errores := v_errores + 1;
    ELSE
        RAISE NOTICE '✓ Usuarios: % OK', v_usuarios;
    END IF;

    RAISE NOTICE '========================================';
    IF v_errores = 0 THEN
        RAISE NOTICE '✓✓✓ INSTALACIÓN EXITOSA ✓✓✓';
        RAISE NOTICE 'Base de datos lista para usar';
    ELSE
        RAISE WARNING '✗✗✗ INSTALACIÓN CON ERRORES: % ✗✗✗', v_errores;
        RAISE WARNING 'Revisar mensajes anteriores';
    END IF;
    RAISE NOTICE '========================================';
END $$;

\echo ''
\echo 'Verificación completada.'
\echo 'Para conectar la aplicación, usar:'
\echo '  URL: jdbc:postgresql://HOST:5432/querillas_db'
\echo '  Usuario: querillas_app (o configurado)'
\echo ''
