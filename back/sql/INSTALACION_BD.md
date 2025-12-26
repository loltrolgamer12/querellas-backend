# 🚀 Guía de Instalación de Base de Datos
## Sistema de Querellas - Alcaldía de Neiva

**Versión:** 3.0 - Producción Optimizada
**Base de Datos:** PostgreSQL 12+
**Última actualización:** 2025-12-22

---

## 📋 Índice

1. [Requisitos Previos](#requisitos-previos)
2. [Instalación Rápida](#instalación-rápida)
3. [Instalación Paso a Paso](#instalación-paso-a-paso)
4. [Verificación](#verificación)
5. [Configuración Post-Instalación](#configuración-post-instalación)
6. [Solución de Problemas](#solución-de-problemas)
7. [Características del Schema](#características-del-schema)

---

## ✅ Requisitos Previos

### Software Necesario

- **PostgreSQL**: Versión 12 o superior
- **Cliente psql**: Incluido con PostgreSQL
- **Permisos**: Usuario con privilegios de creación de base de datos

### Verificar Instalación de PostgreSQL

```bash
# Verificar versión de PostgreSQL
psql --version

# Debería mostrar: psql (PostgreSQL) 12.x o superior
```

---

## ⚡ Instalación Rápida (5 minutos)

Para instalación rápida en desarrollo:

```bash
# 1. Crear base de datos
createdb -U postgres querillas_db

# 2. Ejecutar schema completo
psql -U postgres -d querillas_db -f schema_completo_optimizado.sql

# 3. Cargar datos iniciales
psql -U postgres -d querillas_db -f datos_iniciales.sql

# 4. Verificar instalación
psql -U postgres -d querillas_db -f verificar_instalacion.sql

# ✅ Listo para usar
```

---

## 📖 Instalación Paso a Paso

### Paso 1: Crear Base de Datos

```bash
# Conectar a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE querillas_db
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'es_CO.UTF-8'
    LC_CTYPE = 'es_CO.UTF-8'
    TEMPLATE = template0;

# Conectar a la nueva base de datos
\c querillas_db

# Salir
\q
```

**O usando createdb:**

```bash
createdb -U postgres -E UTF8 -l es_CO.UTF-8 querillas_db
```

### Paso 2: Ejecutar Schema Principal

```bash
psql -U postgres -d querillas_db -f schema_completo_optimizado.sql
```

**Salida esperada:**

```
🚀 Iniciando creación de base de datos optimizada...
📝 Creando secuencias...
📚 Creando tablas de catálogos...
🔄 Creando tablas de estados...
👥 Creando tabla de usuarios...
📋 Creando tabla de querellas...
📄 Creando tabla de despachos comisorios...
📎 Creando tablas relacionadas...
⚙️  Creando tabla de configuración...
🔧 Creando funciones y triggers...
⚡ Aplicando triggers...
🔒 Configurando seguridad...
✅ Base de datos creada exitosamente

📊 RESUMEN DE CREACIÓN
├── Tablas: 12
├── Índices: 65+
├── Secuencias: 2
├── Triggers: 13
└── Funciones: 4

🎉 Schema completo y optimizado listo para usar
```

### Paso 3: Cargar Datos Iniciales

```bash
psql -U postgres -d querillas_db -f datos_iniciales.sql
```

**Datos que se cargan:**

- ✅ 11 estados para QUERELLA
- ✅ 5 estados para DESPACHO
- ✅ Transiciones de estados permitidas
- ✅ 10+ temas de querellas
- ✅ 11 comunas de Neiva
- ✅ 6 configuraciones del sistema
- ✅ 1 usuario administrador inicial

**Credenciales de acceso inicial:**

```
Email: admin@neiva.gov.co
Password: admin123
```

⚠️ **IMPORTANTE:** Cambiar la contraseña en producción

### Paso 4: Verificar Instalación

```bash
psql -U postgres -d querillas_db -f verificar_instalacion.sql
```

**Verificación exitosa muestra:**

```
🔍 VERIFICACIÓN DE INSTALACIÓN DEL SISTEMA
==========================================

📋 1. VERIFICANDO TABLAS...
✅ Tablas creadas: 12
✅ Todas las tablas requeridas existen

🔍 2. VERIFICANDO ÍNDICES...
✅ Índices creados: 65+

🔒 3. VERIFICANDO CONSTRAINTS...
✅ Constraint usuarios.rol CORRECTO (incluye DIRECTORA)

📝 4. VERIFICANDO CAMPOS DE AUDITORÍA...
📊 Cobertura de Auditoría: 100%

⚡ 5. VERIFICANDO TRIGGERS...
✅ Triggers configurados: 13

🔧 6. VERIFICANDO FUNCIONES...
✅ Funciones creadas: 4

🌱 7. VERIFICANDO DATOS INICIALES...
✅ Estados cargados (QUERELLA: 11, DESPACHO: 5)
✅ Temas: 10
✅ Comunas: 11

📊 10. RESUMEN FINAL
✅ INSTALACIÓN COMPLETA Y CORRECTA
```

---

## 🔧 Configuración Post-Instalación

### 1. Crear Usuario de Aplicación

Por seguridad, crear un usuario específico para la aplicación:

```sql
-- Conectar como superusuario
psql -U postgres -d querillas_db

-- Crear usuario
CREATE USER querillas_app WITH PASSWORD 'tu_password_seguro_aqui';

-- Otorgar permisos
GRANT CONNECT ON DATABASE querillas_db TO querillas_app;
GRANT USAGE ON SCHEMA public TO querillas_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO querillas_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO querillas_app;

-- Permisos para tablas futuras
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO querillas_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO querillas_app;
```

### 2. Configurar application.properties

```properties
# Configuración de la base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/querillas_db
spring.datasource.username=querillas_app
spring.datasource.password=tu_password_seguro_aqui
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### 3. Cambiar Contraseña del Administrador

```sql
-- Generar nuevo hash BCrypt (usar herramienta online o código)
-- Ejemplo: password "MiPassword2025!" = $2a$10$...

UPDATE usuarios
SET password = '$2a$10$nuevo_hash_aqui'
WHERE email = 'admin@neiva.gov.co';
```

### 4. Crear Usuarios Adicionales

```sql
-- Inspector de Neiva
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, zona, creado_por)
VALUES (
    'Juan Pérez',
    'jperez@neiva.gov.co',
    '3001234567',
    '$2a$10$hash_bcrypt_aqui',
    'INSPECTOR',
    'ACTIVO',
    'NEIVA',
    1  -- ID del admin
);

-- Inspector de Corregimientos
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, zona, creado_por)
VALUES (
    'María González',
    'mgonzalez@neiva.gov.co',
    '3007654321',
    '$2a$10$hash_bcrypt_aqui',
    'INSPECTOR',
    'ACTIVO',
    'CORREGIMIENTO',
    1
);

-- Auxiliar
INSERT INTO usuarios (nombre, email, telefono, password, rol, estado, creado_por)
VALUES (
    'Pedro Ramírez',
    'pramirez@neiva.gov.co',
    '3009876543',
    '$2a$10$hash_bcrypt_aqui',
    'AUXILIAR',
    'ACTIVO',
    NULL,
    1
);
```

---

## 🐛 Solución de Problemas

### Problema: Error de codificación

```
ERROR: encoding "UTF8" does not match locale "C"
```

**Solución:**

```bash
# Opción 1: Especificar locale al crear BD
createdb -U postgres -E UTF8 -l es_CO.UTF-8 -T template0 querillas_db

# Opción 2: Usar locale C
createdb -U postgres -E UTF8 -l C querillas_db
```

### Problema: Usuario sin permisos

```
ERROR: permission denied for schema public
```

**Solución:**

```sql
-- Como superusuario
GRANT ALL ON SCHEMA public TO nombre_usuario;
```

### Problema: Tablas ya existen

```
ERROR: relation "usuarios" already exists
```

**Solución:**

```bash
# Opción 1: Eliminar y recrear BD
dropdb -U postgres querillas_db
createdb -U postgres querillas_db
psql -U postgres -d querillas_db -f schema_completo_optimizado.sql

# Opción 2: Limpiar tablas existentes
psql -U postgres -d querillas_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

### Problema: Verificación falla en fulltext

```
❌ Falta columna busqueda_tsvector
```

**Solución:**

```sql
-- Agregar manualmente si falta
ALTER TABLE querella ADD COLUMN busqueda_tsvector tsvector;
CREATE INDEX idx_querella_busqueda_gin ON querella USING GIN(busqueda_tsvector);
```

### Problema: No se pueden insertar usuarios DIRECTORA

```
ERROR: new row violates check constraint "usuarios_rol_check"
```

**Solución:**

```sql
-- Verificar constraint
SELECT pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass AND conname = 'usuarios_rol_check';

-- Si no incluye DIRECTORA, corregir:
ALTER TABLE usuarios DROP CONSTRAINT usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('INSPECTOR', 'DIRECTORA', 'AUXILIAR'));
```

---

## 🎯 Características del Schema

### ✅ Auditoría Completa (100%)

Todas las tablas incluyen:
- `creado_en` - Timestamp de creación
- `creado_por` - Usuario que creó
- `actualizado_en` - Timestamp de última modificación (auto-actualizado)
- `actualizado_por` - Usuario que modificó

### ⚡ Rendimiento Optimizado

- **65+ índices** para consultas rápidas
- **Índices compuestos** para filtros frecuentes
- **Índices GIN** para búsqueda fulltext
- **Índices parciales** para datos activos
- **Foreign keys indexados** automáticamente

### 🔍 Búsqueda Fulltext

- Búsqueda en español con `tsvector`
- Soporte para sinónimos y stemming
- Actualización automática con triggers
- Ponderación por relevancia (A, B, C)

### 🔄 Triggers Automáticos

- Actualización de `actualizado_en` en UPDATE
- Generación de `id_local` por inspector
- Actualización de `busqueda_tsvector`
- Validación de transiciones de estado

### 🔒 Seguridad

- Permisos revocados por defecto
- Constraints en todos los enums
- Foreign keys con ON DELETE apropiado
- Validación de datos en BD

### 📈 Escalabilidad

- Preparado para >1M registros
- Índices optimizados para grandes volúmenes
- Estructura lista para particionamiento futuro
- Configuración de VACUUM recomendada

---

## 📊 Estructura de la Base de Datos

### Tablas Principales

| Tabla | Descripción | Registros estimados/año |
|-------|-------------|------------------------|
| `querella` | Querellas ciudadanas | 10,000 - 50,000 |
| `despacho_comisorio` | Despachos judiciales | 1,000 - 5,000 |
| `comunicaciones` | Oficios y notificaciones | 20,000 - 100,000 |
| `adjuntos` | Archivos adjuntos | 5,000 - 20,000 |
| `notificaciones` | Notificaciones del sistema | 100,000 - 500,000 |
| `historial_estado` | Cambios de estado | 50,000 - 200,000 |

### Tablas de Catálogo

| Tabla | Descripción | Registros aprox |
|-------|-------------|----------------|
| `tema` | Temas de querellas | 10-50 |
| `comuna` | Comunas de Neiva | 11 |
| `estado` | Estados del sistema | 16 |
| `usuarios` | Usuarios del sistema | 10-100 |
| `configuracion_sistema` | Configuraciones | 10-50 |

---

## 🔄 Mantenimiento Recomendado

### Backup Diario

```bash
# Script de backup automático
pg_dump -U postgres -d querillas_db -F c > backup_querillas_$(date +%Y%m%d).dump

# Con compresión
pg_dump -U postgres -d querillas_db | gzip > backup_querillas_$(date +%Y%m%d).sql.gz
```

### VACUUM Regular

```sql
-- Configurar autovacuum (recomendado)
ALTER TABLE querella SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE historial_estado SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE notificaciones SET (autovacuum_vacuum_scale_factor = 0.1);

-- VACUUM manual si es necesario
VACUUM ANALYZE;
```

### Monitoreo de Rendimiento

```sql
-- Ver queries lentos
SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;

-- Ver tamaño de tablas
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Ver índices no utilizados
SELECT * FROM pg_stat_user_indexes WHERE idx_scan = 0;
```

---

## 📞 Soporte

Para problemas o preguntas:

1. Revisar esta guía completa
2. Ejecutar `verificar_instalacion.sql`
3. Consultar logs de PostgreSQL
4. Contactar al equipo de desarrollo

---

## 📚 Archivos del Proyecto

| Archivo | Descripción | Cuándo usar |
|---------|-------------|-------------|
| `schema_completo_optimizado.sql` | Schema principal | Primera instalación |
| `datos_iniciales.sql` | Datos base del sistema | Después del schema |
| `verificar_instalacion.sql` | Script de verificación | Validar instalación |
| `INSTALACION_BD.md` | Esta guía | Referencia |
| `AUDITORIA_BASE_DATOS.md` | Auditoría completa | Análisis técnico |

---

**Última actualización:** 2025-12-22
**Mantenido por:** Equipo de Desarrollo - Sistema de Querellas
**Versión del Schema:** 3.0 - Producción Optimizada
