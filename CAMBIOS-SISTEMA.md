# Cambios Implementados en el Sistema de Querellas

## Resumen de Cambios

Este documento describe todos los cambios implementados en el sistema siguiendo los nuevos requerimientos.

## 1. 🏛️ Cambio de Nomenclatura: Inspectores → Inspector de Convivencia y Paz

### Archivos modificados:
- `src/main/java/com/neiva/querillas/domain/model/RolUsuario.java`

### Cambios realizados:
- Se agregó el nombre legible "Inspector de Convivencia y Paz" al enum `RolUsuario.INSPECTOR`
- Se agregó el método `getNombreLegible()` para obtener el nombre completo del rol
- El valor del enum sigue siendo `INSPECTOR` para mantener compatibilidad con la base de datos

### Uso:
```java
RolUsuario rol = RolUsuario.INSPECTOR;
String nombreCompleto = rol.getNombreLegible(); // "Inspector de Convivencia y Paz"
```

---

## 2. 📝 Actualización de Estados de Querellas

### Estados Nuevos:
Los estados de querellas ahora son:
1. **APERTURA** - Estado inicial cuando se recibe una querella
2. **NOTIFICACION** - Cuando se notifica a las partes
3. **AUDIENCIA_PUBLICA** - Cuando se realiza la audiencia
4. **DECISION** - Cuando se toma una decisión
5. **RECURSO** - Cuando se presenta un recurso
6. **INADMISIBLE** - Cuando la querella no es admitida

### Transiciones permitidas:
- APERTURA → NOTIFICACION
- APERTURA → INADMISIBLE
- NOTIFICACION → AUDIENCIA_PUBLICA
- AUDIENCIA_PUBLICA → DECISION
- DECISION → RECURSO

### Script SQL:
Ejecutar: `src/main/resources/db/estados-actualizados.sql`

---

## 3. 🏘️ Eliminación del Sistema de Inspecciones

### Cambios realizados:
- Los usuarios (inspectores) ya no se asignan a "inspecciones"
- Ahora los usuarios se asignan a **corregimientos**
- Las querellas se distribuyen por corregimiento en lugar de por inspección

### Entidades modificadas:
- `Usuario.java` - El campo `inspeccion_id` se cambió a `corregimiento_id`
- `Querella.java` - El campo `inspeccion_id` se marcó como `@Deprecated`

### Nueva entidad:
- `Corregimiento.java` - Representa los corregimientos de Neiva

### Migración:
- El campo `inspeccion_id` en Usuario y Querella debe migrarse a `corregimiento_id`
- La tabla `inspeccion` permanece por compatibilidad pero ya no se usa activamente

---

## 4. 🗺️ Barrios y Comunas de Neiva

### Nuevas entidades:
- **Barrio** - Catálogo de barrios asociados a comunas
- **Corregimiento** - Catálogo de corregimientos de Neiva

### Comunas de Neiva (10):
1. Comuna 1 - Norte
2. Comuna 2 - Oriente
3. Comuna 3 - Centro
4. Comuna 4 - Occidental
5. Comuna 5 - Suroriental
6. Comuna 6 - Noroccidental
7. Comuna 7 - Suroccidental
8. Comuna 8 - Nororiental
9. Comuna 9 - Oriental
10. Comuna 10 - Sur

### Corregimientos de Neiva (11):
1. Caguán
2. Chapinero
3. Fortalecillas
4. Guacirco
5. Órganos
6. Río Loro
7. San Antonio de Anaconia
8. San Luis
9. Santa Helena del Opón
10. Vegalarga
11. El Venado

### Barrios:
Se incluyen aproximadamente 70 barrios distribuidos entre las 10 comunas.

### Script SQL:
Ejecutar: `src/main/resources/db/catalogos-neiva.sql`

### Endpoints API:
```
GET    /api/catalogos/barrios
GET    /api/catalogos/barrios/comuna/{comunaId}
POST   /api/catalogos/barrios
PUT    /api/catalogos/barrios/{id}
DELETE /api/catalogos/barrios/{id}

GET    /api/catalogos/corregimientos
POST   /api/catalogos/corregimientos
PUT    /api/catalogos/corregimientos/{id}
DELETE /api/catalogos/corregimientos/{id}
```

---

## 5. 📋 Sistema de Despachos Comisorios

### Nueva entidad: `DespachoComisorio`

### Campos:
- `id` - Identificador único
- `fechaRecibido` - Fecha de recibido (obligatorio)
- `radicadoProceso` - Radicado del proceso (obligatorio)
- `numeroDespacho` - N° de despacho comisorio (obligatorio)
- `entidadProcedente` - Entidad procedente (obligatorio)
- `asunto` - Asunto del despacho (obligatorio)
- `demandanteApoderado` - Demandante y/o apoderado
- `corregimientoAsignado` - Corregimiento asignado (relación)
- `fechaDevolucion` - Fecha de devolución al juzgado
- `creadoPor` - Usuario que creó el registro
- `creadoEn` - Fecha de creación
- `actualizadoEn` - Fecha de última actualización

### Archivos creados:
- `DespachoComisorio.java` - Entidad
- `DespachoComisarioRepository.java` - Repositorio
- `DespachoComisarioService.java` - Servicio
- `DespachoComisarioController.java` - Controlador REST
- `DespachoComisarioCreateDTO.java` - DTO de creación
- `DespachoComisarioUpdateDTO.java` - DTO de actualización
- `DespachoComisarioResponse.java` - DTO de respuesta

### Endpoints API:
```
POST   /api/despachos-comisorios
GET    /api/despachos-comisorios
GET    /api/despachos-comisorios/{id}
PUT    /api/despachos-comisorios/{id}
DELETE /api/despachos-comisorios/{id}

GET    /api/despachos-comisorios/corregimiento/{corregimientoId}
GET    /api/despachos-comisorios/pendientes-devolucion
GET    /api/despachos-comisorios/buscar-por-entidad?entidad={nombre}
GET    /api/despachos-comisorios/rango-fechas?desde={fecha}&hasta={fecha}
PUT    /api/despachos-comisorios/{id}/devolucion?fechaDevolucion={fecha}
```

### Funcionalidades:
- ✅ Crear despachos comisorios
- ✅ Listar con paginación
- ✅ Filtrar por corregimiento
- ✅ Filtrar por entidad procedente
- ✅ Filtrar por rango de fechas
- ✅ Listar pendientes de devolución
- ✅ Registrar devolución
- ✅ Actualizar información
- ✅ Eliminar despachos

---

## 6. 📊 Reportes para Despachos Comisorios

### Reportes disponibles (a través de los endpoints):

1. **Pendientes de devolución**
   - `GET /api/despachos-comisorios/pendientes-devolucion`
   - Muestra todos los despachos que aún no han sido devueltos

2. **Por corregimiento**
   - `GET /api/despachos-comisorios/corregimiento/{id}`
   - Agrupa despachos por corregimiento asignado

3. **Por rango de fechas**
   - `GET /api/despachos-comisorios/rango-fechas?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`
   - Filtra despachos por fecha de recibido

4. **Por entidad procedente**
   - `GET /api/despachos-comisorios/buscar-por-entidad?entidad={nombre}`
   - Busca despachos por entidad que los envía

---

## 🚀 Instrucciones de Migración

### Paso 1: Actualizar la base de datos

```sql
-- 1. Crear las nuevas tablas (Hibernate lo hará automáticamente con ddl-auto=update)
-- O ejecutar manualmente si se usa ddl-auto=validate:

-- Tabla corregimiento
CREATE TABLE corregimiento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- Tabla barrio
CREATE TABLE barrio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    comuna_id BIGINT REFERENCES comuna(id),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- Tabla despacho_comisorio
CREATE TABLE despacho_comisorio (
    id BIGSERIAL PRIMARY KEY,
    fecha_recibido DATE NOT NULL,
    radicado_proceso VARCHAR(100) NOT NULL,
    numero_despacho VARCHAR(100) NOT NULL,
    entidad_procedente VARCHAR(300) NOT NULL,
    asunto VARCHAR(1000) NOT NULL,
    demandante_apoderado VARCHAR(500),
    corregimiento_id BIGINT REFERENCES corregimiento(id),
    fecha_devolucion DATE,
    creado_por BIGINT,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE
);

-- 2. Agregar columna corregimiento_id a usuarios
ALTER TABLE usuarios ADD COLUMN corregimiento_id BIGINT REFERENCES corregimiento(id);

-- 3. Agregar columnas barrio_id y corregimiento_id a querella
ALTER TABLE querella ADD COLUMN barrio_id BIGINT REFERENCES barrio(id);
ALTER TABLE querella ADD COLUMN corregimiento_id BIGINT REFERENCES corregimiento(id);

-- 4. Ejecutar los scripts de datos
\i src/main/resources/db/catalogos-neiva.sql
\i src/main/resources/db/estados-actualizados.sql
```

### Paso 2: Migrar datos existentes (si es necesario)

```sql
-- Si hay inspecciones que necesitan convertirse a corregimientos:
-- INSERT INTO corregimiento (nombre, creado_en)
-- SELECT nombre, now() FROM inspeccion WHERE ...;

-- Migrar asignaciones de usuarios:
-- UPDATE usuarios SET corregimiento_id = ... WHERE inspeccion_id = ...;
```

### Paso 3: Reiniciar la aplicación

```bash
mvn clean install
mvn spring-boot:run
```

---

## ⚠️ Pendiente: Estados de Despachos Comisorios

**NOTA IMPORTANTE:** Los estados para despachos comisorios no fueron especificados en los requerimientos.

Por favor, definir cuáles serán los estados y sus transiciones para completar la implementación:
- ¿Qué estados debe tener un despacho comisorio?
- ¿Cuáles son las transiciones permitidas?
- ¿Se necesita un historial de cambios de estado?

Una vez definidos, agregar al script `estados-actualizados.sql` e implementar el control de estados en `DespachoComisarioService.java`.

---

## 📋 Checklist de Cambios

- ✅ Cambiar nombre de inspectores a "Inspector de Convivencia y Paz"
- ✅ Actualizar estados de querellas: Apertura, Notificación, Audiencia Publica, Decision, Recurso, Inadmisible
- ⚠️  Cambio de estados de despachos comisorios (pendiente de definición)
- ✅ Eliminar sistema de inspecciones y redistribuir por corregimiento
- ✅ Agregar barrios y comunas de Neiva
- ✅ Implementar despachos comisorios con todos los campos requeridos
- ✅ Crear reportes para despachos comisorios

---

## 🔗 Archivos Modificados/Creados

### Entidades
- ✏️ `Usuario.java` - Modificado
- ✏️ `Querella.java` - Modificado
- ✏️ `RolUsuario.java` - Modificado
- ✨ `Corregimiento.java` - Nuevo
- ✨ `Barrio.java` - Nuevo
- ✨ `DespachoComisorio.java` - Nuevo

### Repositorios
- ✨ `CorregimientoRepository.java` - Nuevo
- ✨ `BarrioRepository.java` - Nuevo
- ✨ `DespachoComisarioRepository.java` - Nuevo

### Servicios
- ✏️ `CatalogoService.java` - Modificado
- ✨ `DespachoComisarioService.java` - Nuevo

### Controladores
- ✏️ `CatalogoController.java` - Modificado
- ✨ `DespachoComisarioController.java` - Nuevo

### DTOs
- ✏️ `CatalogoDTO.java` - Modificado
- ✨ `DespachoComisarioCreateDTO.java` - Nuevo
- ✨ `DespachoComisarioUpdateDTO.java` - Nuevo
- ✨ `DespachoComisarioResponse.java` - Nuevo

### Scripts SQL
- ✨ `catalogos-neiva.sql` - Nuevo
- ✨ `estados-actualizados.sql` - Nuevo

### Documentación
- ✨ `CAMBIOS-SISTEMA.md` - Este archivo

---

## 📞 Contacto y Soporte

Para cualquier duda o problema con la implementación, contactar al equipo de desarrollo.

---

**Fecha de implementación:** 2025-12-10
**Versión:** 1.0.0
