# 📚 Guía de Creación y Migración de Base de Datos

## 🎯 Propósito

Esta carpeta contiene todos los scripts SQL necesarios para crear y configurar la base de datos del Sistema de Gestión de Querellas e Inspecciones de Policía de Neiva.

---

## 📁 Archivos Disponibles

| Archivo | Descripción | Orden de Ejecución |
|---------|-------------|-------------------|
| `schema-completo.sql` | **Script principal** - Crea toda la estructura de la BD desde cero | 1️⃣ |
| `catalogos-neiva.sql` | Carga comunas, corregimientos y barrios de Neiva | 2️⃣ |
| `estados-actualizados.sql` | Actualiza estados de querellas (para migraciones) | ⚠️ Solo si ya existe la BD |
| `migracion-completa.sql` | Script todo-en-uno para migraciones | ⚠️ Solo para actualizar BD existente |

---

## 🚀 Escenario 1: Creación de Base de Datos desde Cero

### Prerequisitos
- PostgreSQL 12 o superior instalado
- Usuario con permisos de creación de base de datos
- Cliente psql o herramienta de administración (pgAdmin, DBeaver, etc.)

### Paso 1: Crear la base de datos

```bash
# Conectarse a PostgreSQL como superusuario
psql -U postgres

# Crear la base de datos
CREATE DATABASE querillas;

# Crear el usuario (si no existe)
CREATE USER postgres WITH PASSWORD 'kibf6f1tniqayblk';

# Dar permisos
GRANT ALL PRIVILEGES ON DATABASE querillas TO postgres;

# Salir
\q
```

### Paso 2: Ejecutar el script principal

```bash
# Conectarse a la base de datos
psql -U postgres -d querillas

# Ejecutar el script de creación completa
\i /ruta/completa/a/schema-completo.sql

# O usando psql desde la línea de comandos
psql -U postgres -d querillas -f schema-completo.sql
```

**Este script creará:**
- ✅ 14 tablas con sus relaciones
- ✅ Todos los índices necesarios
- ✅ Constraints y validaciones
- ✅ Estados iniciales de querellas
- ✅ Transiciones de estados

### Paso 3: Cargar catálogos de Neiva

```bash
# Dentro de psql
\i /ruta/completa/a/catalogos-neiva.sql

# O desde línea de comandos
psql -U postgres -d querillas -f catalogos-neiva.sql
```

**Este script carga:**
- ✅ 10 Comunas de Neiva
- ✅ 11 Corregimientos
- ✅ ~70 Barrios distribuidos por comunas

### Paso 4: Verificar la creación

```sql
-- Ver todas las tablas creadas
\dt

-- Verificar datos iniciales
SELECT 'Comunas: ' || COUNT(*) FROM comuna
UNION ALL
SELECT 'Corregimientos: ' || COUNT(*) FROM corregimiento
UNION ALL
SELECT 'Barrios: ' || COUNT(*) FROM barrio
UNION ALL
SELECT 'Estados QUERELLA: ' || COUNT(*) FROM estado WHERE modulo = 'QUERELLA'
UNION ALL
SELECT 'Transiciones QUERELLA: ' || COUNT(*) FROM estado_transicion WHERE modulo = 'QUERELLA';
```

**Resultado esperado:**
```
Comunas: 10
Corregimientos: 11
Barrios: 70
Estados QUERELLA: 6
Transiciones QUERELLA: 5
```

---

## 🔄 Escenario 2: Migración de Base de Datos Existente

### ⚠️ IMPORTANTE: Hacer Backup Primero

```bash
# Crear backup de la base de datos existente
pg_dump -U postgres -d querillas -F c -f backup_querillas_$(date +%Y%m%d_%H%M%S).backup

# O en formato SQL
pg_dump -U postgres -d querillas > backup_querillas_$(date +%Y%m%d_%H%M%S).sql
```

### Opción A: Migración paso a paso

```bash
# 1. Conectarse a la base de datos
psql -U postgres -d querillas

# 2. Ejecutar script de migración completa
\i /ruta/completa/a/migracion-completa.sql
```

Este script:
- Crea las nuevas tablas (corregimiento, barrio, despacho_comisorio)
- Agrega las nuevas columnas a tablas existentes
- Actualiza los estados de querellas
- Carga los catálogos de Neiva

### Opción B: Solo actualizar estados

Si solo necesitas actualizar los estados de querellas:

```bash
psql -U postgres -d querillas -f estados-actualizados.sql
```

---

## 🏗️ Estructura de la Base de Datos

### Tablas Principales

```
📋 CATÁLOGOS (Nivel 1 - Sin dependencias)
├── comuna              (10 registros)
├── corregimiento       (11 registros)
├── tema               (variable)
├── estado             (6 estados QUERELLA + futuros DESPACHO)
└── inspeccion         (LEGACY - deprecated)

📋 CATÁLOGOS CON RELACIONES (Nivel 2)
├── barrio             (70+ registros, relacionado con comuna)
└── estado_transicion  (5+ transiciones)

👥 USUARIOS Y SEGURIDAD (Nivel 2)
└── usuarios           (inspectores, directores, auxiliares)

📝 CASOS Y PROCESOS (Nivel 3)
├── querella           (casos del sistema)
└── despacho_comisorio (despachos de juzgados)

📊 AUDITORÍA Y COMUNICACIÓN (Nivel 4)
├── historial_estado   (auditoría de cambios)
├── notificaciones     (notificaciones a usuarios)
├── adjuntos          (archivos de querellas)
└── comunicaciones    (oficios, notificaciones, citaciones)
```

---

## 🔍 Consultas Útiles

### Ver estructura de una tabla

```sql
\d+ nombre_tabla
```

### Listar todas las foreign keys

```sql
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
```

### Ver todos los índices

```sql
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

### Verificar integridad referencial

```sql
-- Buscar registros huérfanos en querella
SELECT COUNT(*) as huerfanos_tema
FROM querella
WHERE tema_id IS NOT NULL
AND tema_id NOT IN (SELECT id FROM tema);

SELECT COUNT(*) as huerfanos_comuna
FROM querella
WHERE comuna_id IS NOT NULL
AND comuna_id NOT IN (SELECT id FROM comuna);
```

---

## 🛠️ Mantenimiento

### Actualizar estadísticas

```sql
ANALYZE;
VACUUM ANALYZE;
```

### Re-indexar todas las tablas

```sql
REINDEX DATABASE querillas;
```

### Ver tamaño de las tablas

```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## ❓ Troubleshooting

### Error: "relation already exists"

Si ya tienes la base de datos y quieres recrearla:

```sql
-- PRECAUCIÓN: Esto borra TODA la base de datos
DROP DATABASE IF EXISTS querillas;
CREATE DATABASE querillas;
```

O borrar solo las tablas:

```sql
-- Borrar todas las tablas (en orden inverso a las foreign keys)
DROP TABLE IF EXISTS comunicaciones CASCADE;
DROP TABLE IF EXISTS adjuntos CASCADE;
DROP TABLE IF EXISTS notificaciones CASCADE;
DROP TABLE IF EXISTS despacho_comisorio CASCADE;
DROP TABLE IF EXISTS querella CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS historial_estado CASCADE;
DROP TABLE IF EXISTS estado_transicion CASCADE;
DROP TABLE IF EXISTS barrio CASCADE;
DROP TABLE IF EXISTS estado CASCADE;
DROP TABLE IF EXISTS inspeccion CASCADE;
DROP TABLE IF EXISTS tema CASCADE;
DROP TABLE IF EXISTS corregimiento CASCADE;
DROP TABLE IF EXISTS comuna CASCADE;

-- Luego ejecutar schema-completo.sql
```

### Error: "permission denied"

```sql
-- Dar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE querillas TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
```

### Error: "could not open file"

Verifica la ruta completa del archivo:

```bash
# En lugar de usar ruta relativa
\i schema-completo.sql

# Usar ruta absoluta
\i /ruta/completa/al/proyecto/src/main/resources/db/schema-completo.sql
```

---

## 📞 Soporte

Para más información sobre la estructura de la base de datos, consultar:
- `CAMBIOS-SISTEMA.md` en la raíz del proyecto
- Documentación de entidades en `/src/main/java/com/neiva/querillas/domain/entity/`

---

## ✅ Checklist de Verificación

Después de ejecutar los scripts, verificar:

- [ ] Todas las tablas fueron creadas (14 tablas)
- [ ] Los índices están creados (20+ índices)
- [ ] Los estados de querellas están cargados (6 estados)
- [ ] Las transiciones de estados están configuradas (5 transiciones)
- [ ] Las comunas de Neiva están cargadas (10 comunas)
- [ ] Los corregimientos están cargados (11 corregimientos)
- [ ] Los barrios están cargados (~70 barrios)
- [ ] Las foreign keys funcionan correctamente
- [ ] No hay errores de integridad referencial

---

**Versión del documento:** 2.0.0
**Última actualización:** 2025-12-10
