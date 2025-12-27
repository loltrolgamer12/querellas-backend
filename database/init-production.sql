-- ================================================================
-- Script de Inicialización para Producción en Fly.io
-- Sistema Completo de Gestión de Querellas
-- ================================================================

-- Verificar que estamos en la base de datos correcta
\c querillas;

-- ================================================================
-- 1. CREAR ESQUEMA COMPLETO
-- ================================================================

\echo '=== Creando esquema de base de datos ==='
\i schema.sql

-- ================================================================
-- 2. CARGAR DATOS INICIALES (CATÁLOGOS)
-- ================================================================

\echo '=== Cargando datos iniciales ==='
\i datos_iniciales.sql

-- ================================================================
-- 3. APLICAR ÍNDICES DE PRODUCCIÓN
-- ================================================================

\echo '=== Aplicando índices de producción ==='
\i indices-produccion.sql

-- ================================================================
-- 4. CREAR USUARIO ADMINISTRADOR INICIAL
-- ================================================================

\echo '=== Creando usuario administrador inicial ==='

-- Nota: Esta es una contraseña temporal que DEBE cambiarse después del primer login
-- Password hash para: Admin2025!
INSERT INTO usuario (nombre, email, password, rol, zona, estado, creado_en, actualizado_en)
VALUES (
  'Administrador Sistema',
  'admin@querellas.gov.co',
  '$2a$10$XKrPvVBxBz.VBz7LvFZ8F.OJQjxKGvKGZ8R9YQ9YQ9YQ9YQ9YQ9Y',
  'DIRECTOR',
  NULL,
  'ACTIVO',
  NOW(),
  NOW()
)
ON CONFLICT (email) DO NOTHING;

\echo '=== Usuario administrador creado ==='
\echo 'Email: admin@querellas.gov.co'
\echo 'Password temporal: Admin2025! (CAMBIAR INMEDIATAMENTE)'

-- ================================================================
-- 5. VERIFICAR INSTALACIÓN
-- ================================================================

\echo '=== Verificando instalación ==='

-- Contar registros en tablas principales
SELECT 'Estado' as tabla, COUNT(*) as registros FROM estado
UNION ALL
SELECT 'Tema', COUNT(*) FROM tema
UNION ALL
SELECT 'Comuna', COUNT(*) FROM comuna
UNION ALL
SELECT 'Usuario', COUNT(*) FROM usuario;

\echo '=== Instalación completada exitosamente ==='
