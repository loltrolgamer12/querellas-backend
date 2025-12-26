# 🗄️ Base de Datos - Sistema Completo de Gestión de Querellas

Guía de instalación y configuración de la base de datos PostgreSQL para el sistema de gestión de querellas.

---

## 📋 **Requisitos Previos**

- **PostgreSQL 12 o superior** instalado en el servidor
- Acceso como superusuario de PostgreSQL (postgres)
- Cliente `psql` o pgAdmin para ejecutar scripts
- Mínimo 2GB de espacio en disco para la base de datos
- Conexiones TCP/IP habilitadas en PostgreSQL

---

## 🚀 **Instalación Paso a Paso**

### **Paso 1: Conectarse al servidor PostgreSQL**

Desde el servidor donde está instalado PostgreSQL:

```bash
# Conectarse como usuario postgres
sudo -u postgres psql
```

O desde un cliente remoto:

```bash
psql -h <ip-servidor> -U postgres -d postgres
```

---

### **Paso 2: Crear la Base de Datos**

```sql
-- Crear la base de datos
CREATE DATABASE querillas_db
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'es_CO.UTF-8'
    LC_CTYPE = 'es_CO.UTF-8'
    TEMPLATE = template0;

-- Verificar creación
\l querillas_db
```

---

### **Paso 3: Crear Usuario de Aplicación** (Recomendado)

```sql
-- Crear usuario específico para la aplicación
CREATE USER querillas_app WITH PASSWORD 'CONTRASEÑA_SEGURA_AQUI';

-- Otorgar privilegios sobre la base de datos
GRANT ALL PRIVILEGES ON DATABASE querillas_db TO querillas_app;
```

**⚠️ IMPORTANTE:** Cambia `CONTRASEÑA_SEGURA_AQUI` por una contraseña fuerte.

---

### **Paso 4: Ejecutar Script de Esquema**

```bash
# Desde la línea de comandos del servidor
cd /ruta/al/proyecto/sql

# Ejecutar schema.sql
psql -h localhost -U postgres -d querillas_db -f schema.sql

# O si estás usando el usuario de aplicación:
psql -h localhost -U querillas_app -d querillas_db -f schema.sql
```

Deberías ver mensajes confirmando la creación de tablas e índices.

---

### **Paso 5: Cargar Datos Iniciales**

```bash
# Ejecutar data.sql
psql -h localhost -U postgres -d querillas_db -f data.sql

# Deberías ver un resumen al final con:
# - Cantidad de inspecciones
# - Cantidad de comunas
# - Cantidad de temas
# - Cantidad de estados
# - Cantidad de usuarios
```

---

### **Paso 6: Verificar Instalación**

Conectarse a la base de datos y verificar:

```sql
-- Conectar a la base de datos
\c querillas_db

-- Verificar tablas creadas
\dt

-- Verificar cantidad de registros
SELECT 'Inspecciones' as tabla, COUNT(*) FROM inspeccion
UNION ALL
SELECT 'Comunas', COUNT(*) FROM comuna
UNION ALL
SELECT 'Temas', COUNT(*) FROM tema
UNION ALL
SELECT 'Estados', COUNT(*) FROM estado
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM usuarios
UNION ALL
SELECT 'Transiciones', COUNT(*) FROM estado_transicion;

-- Verificar función de ID local
SELECT obtener_estado_actual_querella(999); -- Debería retornar NULL
```

---

### **Paso 7: Configurar Permisos (Usuario de Aplicación)**

Si creaste un usuario específico `querillas_app`:

```sql
-- Conectar como postgres
\c querillas_db postgres

-- Otorgar permisos sobre todas las tablas
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO querillas_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO querillas_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO querillas_app;

-- Permisos para futuras tablas
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO querillas_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO querillas_app;
```

---

## 🔐 **Credenciales de Acceso Inicial**

### **Usuarios del Sistema**

| Email | Contraseña | Rol |
|-------|-----------|-----|
| directora@inspecciones.neiva.gov.co | demo123 | DIRECTOR |
| auxiliar@inspecciones.neiva.gov.co | demo123 | AUXILIAR |
| inspector1@inspecciones.neiva.gov.co | demo123 | INSPECTOR |
| inspector2@inspecciones.neiva.gov.co | demo123 | INSPECTOR |
| ... (uno por inspección) | demo123 | INSPECTOR |

**⚠️ CRÍTICO:** Cambiar estas contraseñas inmediatamente en producción.

---

## 🔧 **Configuración del Backend**

Actualizar el archivo `src/main/resources/application.properties`:

```properties
# Conexión a la base de datos
spring.datasource.url=jdbc:postgresql://<IP-SERVIDOR>:5432/querillas_db
spring.datasource.username=querillas_app
spring.datasource.password=CONTRASEÑA_SEGURA_AQUI

# Validar esquema (no crear automáticamente)
spring.jpa.hibernate.ddl-auto=validate
```

---

## 📊 **Estructura de la Base de Datos**

### **Tablas Principales**

1. **querella** - Registro de querellas ciudadanas
2. **usuarios** - Usuarios del sistema
3. **estado** - Catálogo de estados
4. **historial_estado** - Historial de cambios de estado
5. **comunicaciones** - Oficios y notificaciones
6. **adjuntos** - Archivos adjuntos
7. **notificaciones** - Notificaciones internas

### **Catálogos**

- **inspeccion** - 7 inspecciones de Neiva
- **comuna** - 10 comunas de Neiva
- **tema** - 15 temas comunes de querellas
- **estado_transicion** - Flujo de estados permitido

---

## 🔄 **Flujo de Estados de Querellas**

```
RECIBIDA
   ├─→ ASIGNADA
   │     ├─→ EN_VERIFICACION
   │     │     ├─→ EN_PROCESO
   │     │     │     ├─→ NOTIFICACION_ENVIADA
   │     │     │     │     ├─→ AUDIENCIA_PROGRAMADA
   │     │     │     │     │     ├─→ EN_AUDIENCIA
   │     │     │     │     │     │     └─→ RESUELTA
   │     │     │     │     │     │           └─→ CERRADA
   │     │     │     │     │     │                 └─→ ARCHIVADA
   │     │     │     ├─→ DESISTIDA → ARCHIVADA
   │     │     └─→ REMITIDA → ARCHIVADA
   └─→ ARCHIVADA (directamente)
```

---

## 🧪 **Pruebas de Verificación**

### **Test 1: Crear una querella de prueba**

```sql
-- Obtener IDs necesarios
SELECT id, nombre FROM inspeccion LIMIT 1;
SELECT id, nombre FROM comuna LIMIT 1;
SELECT id, nombre FROM tema LIMIT 1;
SELECT id FROM estado WHERE nombre='RECIBIDA';

-- Crear querella
INSERT INTO querella (
    direccion, descripcion, tema_id, naturaleza,
    inspeccion_id, comuna_id, es_migrado, creado_en, actualizado_en
) VALUES (
    'Calle 5 # 10-20, Barrio Centro',
    'Ruidos molestos por establecimiento comercial',
    1,  -- Tema ID
    'PERSONA',
    1,  -- Inspección ID
    1,  -- Comuna ID
    FALSE,
    NOW(),
    NOW()
);

-- Verificar radicado generado
SELECT id, radicado_interno, id_local FROM querella ORDER BY id DESC LIMIT 1;
```

### **Test 2: Registrar cambio de estado**

```sql
-- Obtener ID de la querella creada
-- Insertar en historial
INSERT INTO historial_estado (modulo, caso_id, estado_id, motivo, usuario_id, creado_en)
VALUES (
    'QUERELLA',
    1,  -- ID de la querella
    (SELECT id FROM estado WHERE nombre='ASIGNADA'),
    'Asignación inicial',
    1,  -- ID de usuario
    NOW()
);

-- Verificar estado actual
SELECT obtener_estado_actual_querella(1);
```

### **Test 3: Verificar trigger de ID local**

```sql
-- El id_local debe haberse generado automáticamente
-- Formato: <inspeccion_id>-<año>-<contador>
-- Ejemplo: 1-2025-0001

SELECT id, radicado_interno, id_local, inspeccion_id
FROM querella
WHERE id_local IS NOT NULL;
```

---

## 🛠️ **Mantenimiento**

### **Backup de la Base de Datos**

```bash
# Backup completo
pg_dump -h localhost -U postgres -d querillas_db -F c -f backup_querillas_$(date +%Y%m%d).dump

# Backup solo datos
pg_dump -h localhost -U postgres -d querillas_db --data-only -f datos_querillas_$(date +%Y%m%d).sql
```

### **Restaurar desde Backup**

```bash
# Restaurar desde dump
pg_restore -h localhost -U postgres -d querillas_db backup_querillas_20250101.dump

# Restaurar desde SQL
psql -h localhost -U postgres -d querillas_db -f datos_querillas_20250101.sql
```

### **Limpiar y Optimizar**

```sql
-- Actualizar estadísticas
ANALYZE;

-- Vacuum completo (ejecutar en horario de bajo uso)
VACUUM FULL;

-- Reindexar base de datos
REINDEX DATABASE querillas_db;
```

---

## 📈 **Monitoreo**

### **Consultas Útiles**

```sql
-- Total de querellas por estado
SELECT e.nombre, COUNT(*)
FROM querella q
JOIN historial_estado he ON he.caso_id = q.id
JOIN estado e ON e.id = he.estado_id
WHERE he.id IN (
    SELECT MAX(id) FROM historial_estado
    WHERE modulo='QUERELLA'
    GROUP BY caso_id
)
GROUP BY e.nombre;

-- Querellas por inspección
SELECT i.nombre, COUNT(*)
FROM querella q
JOIN inspeccion i ON i.id = q.inspeccion_id
GROUP BY i.nombre;

-- Actividad reciente (últimas 24 horas)
SELECT COUNT(*) FROM querella WHERE creado_en > NOW() - INTERVAL '24 hours';
```

---

## ⚠️ **Advertencias de Seguridad**

1. **Cambiar contraseñas** de todos los usuarios después de la instalación
2. **Configurar firewall** para PostgreSQL (puerto 5432)
3. **Habilitar SSL** para conexiones remotas
4. **Configurar pg_hba.conf** con restricciones de IP
5. **Realizar backups** automáticos diarios
6. **Actualizar PostgreSQL** regularmente
7. **Monitorear logs** en `/var/log/postgresql/`

---

## 🆘 **Solución de Problemas**

### **Error: "database does not exist"**
```bash
# Verificar que la base de datos existe
psql -U postgres -l | grep querillas
```

### **Error: "permission denied"**
```sql
-- Otorgar permisos nuevamente
GRANT ALL PRIVILEGES ON DATABASE querillas_db TO querillas_app;
```

### **Error: "relation does not exist"**
```bash
# Verificar que schema.sql se ejecutó correctamente
psql -U postgres -d querillas_db -c "\dt"
```

### **Error de conexión desde la aplicación**
```bash
# Verificar que PostgreSQL acepta conexiones TCP/IP
# Editar postgresql.conf:
listen_addresses = '*'

# Editar pg_hba.conf:
host    querillas_db    querillas_app    0.0.0.0/0    md5

# Reiniciar PostgreSQL
sudo systemctl restart postgresql
```

---

## 📞 **Soporte**

Para soporte técnico:
- **Email:** soporte.ti@neiva.gov.co
- **Documentación:** Ver README.md del proyecto
- **Logs:** Revisar `/var/log/postgresql/` y logs de la aplicación

---

## ✅ **Checklist de Instalación**

- [ ] PostgreSQL instalado y funcionando
- [ ] Base de datos `querillas_db` creada
- [ ] Usuario `querillas_app` creado con contraseña segura
- [ ] Script `schema.sql` ejecutado exitosamente
- [ ] Script `data.sql` ejecutado exitosamente
- [ ] Verificadas las tablas y datos iniciales
- [ ] Permisos configurados correctamente
- [ ] Contraseñas de usuarios iniciales cambiadas
- [ ] Backup inicial realizado
- [ ] `application.properties` actualizado
- [ ] Conexión desde la aplicación probada
- [ ] Firewall y seguridad configurados

---

**Fecha de última actualización:** Diciembre 2024
**Versión del esquema:** 1.0
