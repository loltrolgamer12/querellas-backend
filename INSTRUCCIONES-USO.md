# 🚀 INSTRUCCIONES DE USO - Sistema de Querellas

## Inicio Rápido (Para Desarrolladores)

### Opción 1: Script Automático (Recomendado)

```bash
./iniciar-sistema.sh
```

Selecciona la opción que necesites:
- **1**: Solo Backend (Puerto 8081)
- **2**: Solo Frontend (Puerto 3000)
- **3**: Ambos servicios
- **4**: Compilar Backend

### Opción 2: Manual

**Iniciar Backend**:
```bash
cd querellas-backend
./mvnw spring-boot:run
```

**Iniciar Frontend**:
```bash
cd Frontend
npm run dev
```

---

## 📋 Primera Vez

### 1. Verificar Requisitos

```bash
java -version    # Necesita Java 17+
node --version   # Necesita Node 18+
npm --version    # Debe estar instalado
```

### 2. Instalar Dependencias del Frontend

```bash
cd Frontend
npm install
```

### 3. Aplicar Índices a la Base de Datos (Opcional pero Recomendado)

```bash
psql -h vps-be502614.vps.ovh.ca -U postgres -d querillas -f indices-produccion.sql
```

---

## 🔐 Acceso al Sistema

### URLs del Sistema

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html

### Usuarios de Desarrollo

Solo disponibles en modo desarrollo (perfil "dev"):

| Email | Contraseña | Rol |
|-------|------------|-----|
| director@querellas.com | password123 | DIRECTOR |
| auxiliar@querellas.com | password123 | AUXILIAR |
| inspector1@querellas.com | password123 | INSPECTOR |

**⚠️ IMPORTANTE**: En producción estos usuarios NO se crean automáticamente.

---

## 🎯 Funcionalidades por Rol

### DIRECTOR (Acceso Completo)
- ✅ Crear, ver, editar y eliminar querellas
- ✅ Asignar inspectores a querellas
- ✅ Asignación automática Round-Robin
- ✅ Gestionar usuarios del sistema
- ✅ Gestionar despachos comisorios
- ✅ Ver todos los reportes
- ✅ Exportar a Excel
- ✅ Administrar catálogos (temas, comunas, estados)

### AUXILIAR
- ✅ Crear y ver querellas
- ✅ Gestionar despachos comisorios
- ✅ Ver reportes
- ✅ Exportar a Excel
- ❌ No puede asignar inspectores
- ❌ No puede gestionar usuarios
- ❌ No puede modificar catálogos

### INSPECTOR
- ✅ Ver querellas asignadas a él
- ✅ Actualizar estado de sus querellas
- ✅ Ver despachos asignados
- ✅ Agregar comunicaciones y adjuntos
- ❌ No puede crear querellas
- ❌ No puede ver querellas de otros inspectores
- ❌ No puede acceder a reportes generales

---

## 📊 Flujo de Trabajo Típico

### 1. Recepción de Querella

1. Usuario DIRECTOR o AUXILIAR inicia sesión
2. Va a "Crear Querella"
3. Completa el formulario:
   - Descripción del problema
   - Tema (seleccionar de catálogo)
   - Comuna
   - Dirección
   - Datos del querellante
4. Guarda la querella

### 2. Asignación de Inspector

**Opción A - Manual**:
1. Ir a detalle de la querella
2. Clic en "Asignar Inspector"
3. Seleccionar inspector de la lista
4. Confirmar

**Opción B - Automática (Round-Robin)**:
1. Ir a "Querellas" → "Asignar Automático"
2. Seleccionar las querellas a asignar (checkbox)
3. Clic en "Asignar Automáticamente"
4. El sistema distribuye equitativamente entre inspectores activos

### 3. Investigación (Inspector)

1. Inspector inicia sesión
2. Ve sus querellas asignadas
3. Cambia estado a "EN_INVESTIGACION"
4. Agrega comunicaciones sobre avances
5. Sube adjuntos (fotos, documentos)
6. Cambia estado a "RESUELTA" al terminar

### 4. Cierre (Director/Auxiliar)

1. Revisar querella resuelta
2. Verificar documentación
3. Cambiar estado a "CERRADA"

---

## 📁 Gestión de Despachos Comisorios

### Crear Despacho

1. Ir a "Despachos Comisorios"
2. Clic en "Nuevo Despacho"
3. Completar:
   - Número de despacho
   - Entidad procedente (obligatorio)
   - Asunto
   - Fecha de recibido
   - Demandante/Apoderado
   - Demandado/Apoderado
4. Asignar inspector (opcional)
5. Guardar

### Marcar como Devuelto

1. Abrir despacho
2. Clic en "Marcar como Devuelto"
3. Ingresar fecha de devolución
4. Agregar observaciones
5. Confirmar

---

## 📈 Reportes y Estadísticas

### Dashboard

Disponible en la página principal para todos los roles:
- Total de querellas
- Querellas por estado
- Querellas por inspector
- Despachos pendientes

### Reportes Trimestrales

Solo DIRECTOR y AUXILIAR:
1. Ir a "Reportes"
2. Seleccionar rango de fechas
3. Filtrar por inspector (opcional)
4. Ver estadísticas
5. Exportar a Excel

---

## 🔍 Búsqueda y Filtros

### Filtros Disponibles

En la lista de querellas:
- **Texto libre**: Busca en descripción y dirección
- **Tema**: Filtrar por tema específico
- **Comuna**: Filtrar por comuna
- **Estado**: Filtrar por estado actual
- **Inspector**: Ver querellas de un inspector
- **Rango de fechas**: Desde/Hasta

### Ordenamiento

Clic en encabezados de columnas para ordenar:
- ID
- Fecha de creación
- Estado
- Inspector

---

## 📎 Adjuntos y Comunicaciones

### Subir Adjuntos

1. Abrir querella
2. Ir a pestaña "Adjuntos"
3. Clic en "Subir Archivo"
4. Seleccionar archivo
5. Agregar descripción (opcional)
6. Subir

**Formatos soportados**: PDF, JPG, PNG, DOC, DOCX, XLS, XLSX

### Agregar Comunicaciones

1. Abrir querella
2. Ir a pestaña "Comunicaciones"
3. Clic en "Nueva Comunicación"
4. Seleccionar tipo:
   - Llamada telefónica
   - Email
   - Visita personal
   - Oficio
5. Ingresar destinatario y asunto
6. Escribir mensaje
7. Guardar

---

## ⚙️ Administración (Solo DIRECTOR)

### Gestionar Usuarios

1. Ir a "Administración" → "Usuarios"
2. Ver lista de usuarios
3. Opciones:
   - **Crear usuario**: Nuevo inspector/auxiliar
   - **Editar**: Modificar datos
   - **Activar/Desactivar**: Cambiar estado

### Gestionar Catálogos

**Temas**:
1. Ir a "Administración" → "Temas"
2. Agregar/Editar/Eliminar temas de querellas

**Comunas**:
1. Ir a "Administración" → "Comunas"
2. Agregar/Editar/Eliminar comunas

**Estados**:
- Son fijos del sistema
- No se pueden modificar desde la interfaz

---

## 🆘 Solución de Problemas

### Backend no inicia

**Error**: "Port 8081 already in use"
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8081
kill -9 <PID>
```

### Frontend no inicia

**Error**: "Port 3000 already in use"
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>
```

### No puede conectar al backend

1. Verificar que backend esté corriendo: http://localhost:8081/actuator/health
2. Verificar CORS en `application.properties`
3. Verificar `.env.local` en Frontend:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8081
   ```

### Error de autenticación

1. Limpiar localStorage del navegador
2. Verificar que el usuario exista en la base de datos
3. Verificar que la contraseña sea correcta

---

## 📚 Documentación Adicional

- **[README.md](README.md)**: Documentación técnica
- **[RESUMEN-DESPLIEGUE-PRODUCCION.md](RESUMEN-DESPLIEGUE-PRODUCCION.md)**: Guía de despliegue
- **[LISTO-PARA-PRODUCCION.md](LISTO-PARA-PRODUCCION.md)**: Estado final del proyecto
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html (Documentación de API)

---

## 💡 Consejos

1. **Usa Round-Robin**: Para distribuir equitativamente la carga entre inspectores
2. **Filtros combinados**: Puedes combinar múltiples filtros para búsquedas precisas
3. **Export a Excel**: Todos los reportes se pueden exportar
4. **Paginación**: Los listados muestran 10 registros por página por defecto
5. **Historial completo**: Cada cambio de estado se registra con fecha y usuario

---

## 📞 Soporte Técnico

Para problemas técnicos:
1. Revisar logs del backend: `querellas-backend/logs/`
2. Revisar consola del navegador (F12)
3. Verificar documentación en este directorio

---

**Versión**: 1.0.0
**Última actualización**: 26 de Diciembre de 2025
