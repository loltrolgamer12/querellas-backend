# 📁 Índice de Archivos SQL - Sistema de Querellas

Este directorio contiene todos los scripts necesarios para instalar y gestionar la base de datos del Sistema de Gestión de Querellas de la Alcaldía de Neiva.

---

## 📄 **Archivos Principales**

### 1. **schema.sql** (384 líneas)
**Propósito:** Script de creación de estructura de base de datos

**Contenido:**
- Creación de todas las tablas (11 tablas)
- Definición de secuencias
- Foreign keys y constraints
- Índices para optimización
- Triggers (generación automática de id_local)
- Funciones auxiliares
- Comentarios de documentación

**Uso:**
```bash
psql -U postgres -d querillas_db -f schema.sql
```

**Tablas creadas:**
- `querella` - Registro principal de querellas
- `usuarios` - Usuarios del sistema
- `estado` - Catálogo de estados
- `historial_estado` - Historial de cambios
- `estado_transicion` - Flujo de estados permitido
- `comunicaciones` - Oficios y notificaciones
- `adjuntos` - Archivos adjuntos
- `notificaciones` - Notificaciones internas
- `inspeccion` - Catálogo de inspecciones
- `tema` - Catálogo de temas
- `comuna` - Catálogo de comunas

---

### 2. **data.sql** (272 líneas)
**Propósito:** Datos iniciales para el sistema

**Contenido:**
- 7 Inspecciones de Neiva
- 10 Comunas de Neiva
- 15 Temas comunes de querellas
- 12 Estados de QUERELLA
- 20+ Transiciones de estado permitidas
- 9+ Usuarios iniciales (Directora, Auxiliar, Inspectores)

**Uso:**
```bash
psql -U postgres -d querillas_db -f data.sql
```

**Credenciales por defecto:**
- Email: `directora@inspecciones.neiva.gov.co`
- Password: `demo123`

⚠️ **Cambiar contraseñas en producción**

---

### 3. **README_DATABASE.md** (428 líneas)
**Propósito:** Guía completa de instalación y administración

**Contenido:**
- Requisitos previos
- Instalación paso a paso (7 pasos)
- Credenciales de acceso
- Configuración del backend
- Estructura de la base de datos
- Flujo de estados
- Pruebas de verificación
- Mantenimiento (backups, optimización)
- Monitoreo y consultas útiles
- Solución de problemas
- Checklist de instalación

**Uso:** Lectura y referencia durante la instalación

---

### 4. **verify_installation.sql** (303 líneas)
**Propósito:** Verificar que la instalación fue exitosa

**Contenido:**
- Verificación de tablas creadas (11 esperadas)
- Verificación de secuencias
- Verificación de funciones y triggers
- Verificación de índices
- Verificación de datos cargados
- Pruebas funcionales de triggers
- Pruebas de funciones auxiliares
- Resumen de verificación con ✓/✗

**Uso:**
```bash
psql -U postgres -d querillas_db -f verify_installation.sql
```

**Salida esperada:**
```
✓✓✓ INSTALACIÓN EXITOSA ✓✓✓
Base de datos lista para usar
```

---

### 5. **rollback.sql** (153 líneas)
**Propósito:** Deshacer la instalación completamente

**Contenido:**
- Eliminación de triggers
- Eliminación de funciones
- Eliminación de todas las tablas
- Eliminación de secuencias
- Verificación de limpieza

**Uso:**
```bash
psql -U postgres -d querillas_db -f rollback.sql
```

⚠️ **ADVERTENCIA:** Elimina TODO (tablas y datos). Solo para reinstalación.

---

### 6. **install_all.sh** (Script Bash)
**Propósito:** Instalación automatizada completa

**Contenido:**
- Script interactivo con menús
- Crea base de datos
- Crea usuario de aplicación (opcional)
- Ejecuta schema.sql
- Ejecuta data.sql
- Configura permisos
- Ejecuta verificación
- Muestra resumen

**Uso:**
```bash
chmod +x install_all.sh
./install_all.sh
```

**Características:**
- ✅ Instalación guiada paso a paso
- ✅ Validación de conexión
- ✅ Creación automática de usuario
- ✅ Verificación integrada
- ✅ Mensajes con colores
- ✅ Manejo de errores

---

## 🚀 **Orden de Ejecución Recomendado**

### **Opción A: Instalación Manual**

1. Leer `README_DATABASE.md`
2. Ejecutar `schema.sql`
3. Ejecutar `data.sql`
4. Ejecutar `verify_installation.sql`

```bash
# Paso a paso
psql -U postgres -d querillas_db -f schema.sql
psql -U postgres -d querillas_db -f data.sql
psql -U postgres -d querillas_db -f verify_installation.sql
```

### **Opción B: Instalación Automatizada** ⭐ Recomendado

```bash
./install_all.sh
```

El script hará todo automáticamente y te guiará en el proceso.

---

## 📊 **Resumen de Datos Iniciales**

| Categoría | Cantidad |
|-----------|----------|
| Inspecciones | 7 |
| Comunas | 10 |
| Temas | 15 |
| Estados | 12 |
| Transiciones | 20+ |
| Usuarios | 9 |

**Total de registros iniciales:** ~73

---

## 🔄 **En Caso de Problemas**

### **Reinstalar desde cero:**
```bash
# 1. Limpiar todo
psql -U postgres -d querillas_db -f rollback.sql

# 2. Instalar nuevamente
./install_all.sh
```

### **Verificar estado:**
```bash
psql -U postgres -d querillas_db -f verify_installation.sql
```

### **Ver logs de PostgreSQL:**
```bash
sudo tail -f /var/log/postgresql/postgresql-*.log
```

---

## 📞 **Soporte**

- **Documentación principal:** `README_DATABASE.md`
- **Proyecto principal:** `../README.md`
- **Issues:** Reportar en el repositorio de GitHub

---

## ✅ **Checklist Rápido**

Después de la instalación, verificar:

- [ ] Base de datos `querillas_db` existe
- [ ] 11 tablas creadas
- [ ] Trigger `trigger_generar_id_local` funciona
- [ ] Función `obtener_estado_actual_querella` funciona
- [ ] 7 inspecciones cargadas
- [ ] 12 estados de QUERELLA cargados
- [ ] Usuario directora puede hacer login
- [ ] Backend se conecta correctamente

---

**Última actualización:** Diciembre 2024
**Versión del esquema:** 1.0
**Compatible con:** PostgreSQL 12+
