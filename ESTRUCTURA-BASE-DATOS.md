# 🗄️ Estructura de la Base de Datos - Sistema de Querellas

## 📊 Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ESTRUCTURA DE TABLAS                              │
│                  (Ordenadas por nivel de dependencia)                 │
└─────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════
 NIVEL 1: TABLAS INDEPENDIENTES (Catálogos Base)
═══════════════════════════════════════════════════════════════════════

┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   comuna     │    │     tema     │    │   estado     │
├──────────────┤    ├──────────────┤    ├──────────────┤
│ id PK        │    │ id PK        │    │ id PK        │
│ nombre       │    │ nombre       │    │ modulo       │
│ creado_en    │    │ creado_en    │    │ nombre       │
│ ...          │    │ ...          │    │ creado_en    │
└──────────────┘    └──────────────┘    └──────────────┘

┌──────────────┐    ┌──────────────┐
│corregimiento │    │ inspeccion   │
├──────────────┤    ├──────────────┤
│ id PK        │    │ id PK        │  [DEPRECATED]
│ nombre       │    │ nombre       │  Legacy
│ creado_en    │    │ creado_en    │
│ ...          │    │ ...          │
└──────────────┘    └──────────────┘

═══════════════════════════════════════════════════════════════════════
 NIVEL 2: TABLAS CON RELACIONES SIMPLES
═══════════════════════════════════════════════════════════════════════

┌──────────────────────┐
│       barrio         │
├──────────────────────┤
│ id PK                │
│ nombre               │
│ comuna_id FK ────────┼──────┐
│ creado_en            │      │
│ ...                  │      │
└──────────────────────┘      │
                              │
                    ┌─────────▼──────┐
                    │     comuna      │
                    └─────────────────┘

┌──────────────────────────────────┐
│      estado_transicion           │
├──────────────────────────────────┤
│ id PK                            │
│ modulo                           │
│ desde_estado_id FK ──────────────┼─┐
│ hacia_estado_id FK ──────────────┼─┼──┐
└──────────────────────────────────┘ │  │
                                     │  │
                         ┌───────────▼──▼────┐
                         │      estado       │
                         └───────────────────┘

┌──────────────────────────────────┐
│      historial_estado            │
├──────────────────────────────────┤
│ id PK                            │
│ modulo                           │
│ caso_id                          │
│ estado_id FK ────────────────────┼──┐
│ motivo                           │  │
│ usuario_id                       │  │
│ creado_en                        │  │
└──────────────────────────────────┘  │
                                      │
                          ┌───────────▼──────┐
                          │     estado       │
                          └──────────────────┘

┌──────────────────────────────────┐
│          usuarios                │
├──────────────────────────────────┤
│ id PK                            │
│ nombre                           │
│ email (UNIQUE)                   │
│ password (BCrypt)                │
│ rol                              │
│ estado                           │
│ corregimiento_id FK ─────────────┼─┐
│ creado_en                        │ │
│ ...                              │ │
└──────────────────────────────────┘ │
                                     │
                   ┌─────────────────▼────┐
                   │   corregimiento      │
                   └──────────────────────┘

═══════════════════════════════════════════════════════════════════════
 NIVEL 3: TABLAS PRINCIPALES (Querellas y Despachos)
═══════════════════════════════════════════════════════════════════════

┌────────────────────────────────────────┐
│            querella                    │
├────────────────────────────────────────┤
│ id PK                                  │
│ radicado_interno                       │
│ direccion                              │
│ descripcion                            │
│ naturaleza (OFICIO/PERSONA/ANONIMA)    │
│                                        │
│ tema_id FK ─────────────────────────── ┼──┐
│ comuna_id FK ───────────────────────── ┼──┼──┐
│ barrio_id FK ───────────────────────── ┼──┼──┼──┐
│ corregimiento_id FK ────────────────── ┼──┼──┼──┼──┐
│ inspeccion_id FK [DEPRECATED] ──────── ┼──┼──┼──┼──┼──┐
│                                        │  │  │  │  │  │
│ querellante_nombre                     │  │  │  │  │  │
│ querellante_contacto                   │  │  │  │  │  │
│ genero_querellante                     │  │  │  │  │  │
│ genero_querellado                      │  │  │  │  │  │
│ tiene_fallo                            │  │  │  │  │  │
│ tiene_apelacion                        │  │  │  │  │  │
│ creado_en                              │  │  │  │  │  │
│ ...                                    │  │  │  │  │  │
└────────────────────────────────────────┘  │  │  │  │  │
                                            │  │  │  │  │
                     ┌──────────────────────┘  │  │  │  │
                     │     tema                │  │  │  │
                     └─────────────────────────┘  │  │  │
                                                  │  │  │
                     ┌────────────────────────────┘  │  │
                     │     comuna                    │  │
                     └───────────────────────────────┘  │
                                                        │
                     ┌──────────────────────────────────┘
                     │     barrio
                     └─────────────────┐
                                       │
                     ┌─────────────────▼──────┐
                     │   corregimiento        │
                     └────────────────────────┘

                     ┌────────────────────────┐
                     │   inspeccion [LEGACY]  │
                     └────────────────────────┘


┌────────────────────────────────────────┐
│        despacho_comisorio              │
├────────────────────────────────────────┤
│ id PK                                  │
│ fecha_recibido                         │
│ radicado_proceso                       │
│ numero_despacho                        │
│ entidad_procedente                     │
│ asunto                                 │
│ demandante_apoderado                   │
│ fecha_devolucion                       │
│                                        │
│ corregimiento_id FK ────────────────── ┼──┐
│                                        │  │
│ creado_en                              │  │
│ ...                                    │  │
└────────────────────────────────────────┘  │
                                            │
                      ┌─────────────────────▼────┐
                      │   corregimiento          │
                      └──────────────────────────┘

═══════════════════════════════════════════════════════════════════════
 NIVEL 4: TABLAS DE RELACIÓN Y AUDITORÍA
═══════════════════════════════════════════════════════════════════════

┌────────────────────────────────┐
│       notificaciones           │
├────────────────────────────────┤
│ id PK                          │
│ titulo                         │
│ mensaje                        │
│ tipo                           │
│ leida                          │
│                                │
│ querella_id FK ────────────────┼──┐
│ usuario_id FK ─────────────────┼──┼──┐
│                                │  │  │
│ creado_en                      │  │  │
└────────────────────────────────┘  │  │
                                    │  │
                  ┌─────────────────┘  │
                  │    querella        │
                  └────────────────────┘
                                       │
                  ┌────────────────────▼──┐
                  │     usuarios          │
                  └───────────────────────┘


┌────────────────────────────────┐
│          adjuntos              │
├────────────────────────────────┤
│ id PK                          │
│ nombre_archivo                 │
│ tipo_archivo (MIME)            │
│ tamano_bytes                   │
│ ruta_storage                   │
│ descripcion                    │
│                                │
│ querella_id FK ────────────────┼──┐
│ cargado_por FK ────────────────┼──┼──┐
│                                │  │  │
│ creado_en                      │  │  │
└────────────────────────────────┘  │  │
                                    │  │
                  ┌─────────────────┘  │
                  │    querella        │
                  └────────────────────┘
                                       │
                  ┌────────────────────▼──┐
                  │     usuarios          │
                  └───────────────────────┘


┌────────────────────────────────┐
│       comunicaciones           │
├────────────────────────────────┤
│ id PK                          │
│ tipo (OFICIO/NOTIFICACION/...) │
│ numero_radicado                │
│ asunto                         │
│ contenido                      │
│ fecha_envio                    │
│ destinatario                   │
│ estado (BORRADOR/ENVIADO/...)  │
│                                │
│ querella_id FK ────────────────┼──┐
│ creado_por FK ─────────────────┼──┼──┐
│                                │  │  │
│ creado_en                      │  │  │
└────────────────────────────────┘  │  │
                                    │  │
                  ┌─────────────────┘  │
                  │    querella        │
                  └────────────────────┘
                                       │
                  ┌────────────────────▼──┐
                  │     usuarios          │
                  └───────────────────────┘
```

---

## 📋 Resumen de Tablas

| # | Tabla | Registros Esperados | Descripción |
|---|-------|---------------------|-------------|
| 1 | `comuna` | 10 | Comunas de Neiva |
| 2 | `corregimiento` | 11 | Corregimientos de Neiva |
| 3 | `tema` | Variable | Temas/motivos de querellas |
| 4 | `estado` | 6+ | Estados de flujo (QUERELLA, DESPACHO) |
| 5 | `inspeccion` | Variable | [LEGACY] Sistema antiguo |
| 6 | `barrio` | 70+ | Barrios de Neiva por comuna |
| 7 | `estado_transicion` | 5+ | Transiciones válidas entre estados |
| 8 | `historial_estado` | Miles | Auditoría de cambios de estado |
| 9 | `usuarios` | Variable | Inspectores, directores, auxiliares |
| 10 | `querella` | Miles | Casos/querellas del sistema |
| 11 | `despacho_comisorio` | Variable | Despachos de juzgados |
| 12 | `notificaciones` | Miles | Notificaciones a usuarios |
| 13 | `adjuntos` | Variable | Archivos de querellas |
| 14 | `comunicaciones` | Variable | Oficios, citaciones, etc. |

---

## 🔑 Foreign Keys y Relaciones

### Relaciones One-to-Many (1:N)

| Tabla Padre | Tabla Hija | Descripción |
|-------------|------------|-------------|
| `comuna` | `barrio` | Una comuna tiene muchos barrios |
| `corregimiento` | `usuarios` | Un corregimiento tiene muchos inspectores |
| `corregimiento` | `querella` | Un corregimiento maneja muchas querellas |
| `corregimiento` | `despacho_comisorio` | Un corregimiento tramita muchos despachos |
| `querella` | `notificaciones` | Una querella genera muchas notificaciones |
| `querella` | `adjuntos` | Una querella tiene muchos adjuntos |
| `querella` | `comunicaciones` | Una querella genera muchas comunicaciones |
| `usuarios` | `notificaciones` | Un usuario recibe muchas notificaciones |
| `usuarios` | `adjuntos` | Un usuario carga muchos adjuntos |
| `usuarios` | `comunicaciones` | Un usuario crea muchas comunicaciones |
| `estado` | `estado_transicion` | Un estado participa en muchas transiciones |
| `estado` | `historial_estado` | Un estado aparece en muchos historiales |

---

## 🔍 Índices Principales

### Índices para Optimización de Consultas

```sql
-- Búsquedas por ubicación
CREATE INDEX idx_querella_comuna ON querella(comuna_id);
CREATE INDEX idx_querella_barrio ON querella(barrio_id);
CREATE INDEX idx_querella_corregimiento ON querella(corregimiento_id);
CREATE INDEX idx_barrio_comuna ON barrio(comuna_id);

-- Búsquedas por usuario
CREATE INDEX idx_usuario_corregimiento ON usuarios(corregimiento_id);
CREATE INDEX idx_usuario_rol ON usuarios(rol);
CREATE INDEX idx_usuario_estado ON usuarios(estado);
CREATE INDEX idx_usuario_email ON usuarios(email);

-- Búsquedas temporales
CREATE INDEX idx_querella_creado_en ON querella(creado_en DESC);
CREATE INDEX idx_despacho_fecha_recibido ON despacho_comisorio(fecha_recibido DESC);
CREATE INDEX idx_historial_creado_en ON historial_estado(creado_en DESC);

-- Búsquedas por estado
CREATE INDEX idx_historial_modulo_caso ON historial_estado(modulo, caso_id);
CREATE INDEX idx_notificacion_leida ON notificaciones(leida);
CREATE INDEX idx_comunicacion_estado ON comunicaciones(estado);

-- Búsquedas textuales
CREATE INDEX idx_querella_radicado ON querella(radicado_interno);
CREATE INDEX idx_despacho_entidad ON despacho_comisorio(entidad_procedente);
```

---

## ⚙️ Enums y Valores Permitidos

### RolUsuario
- `INSPECTOR` - Inspector de Convivencia y Paz
- `DIRECTOR` - Director/a
- `AUXILIAR` - Personal auxiliar

### EstadoUsuario
- `ACTIVO` - Usuario activo
- `BLOQUEADO` - Usuario bloqueado
- `NO_DISPONIBLE` - Usuario no disponible temporalmente

### Naturaleza (Querella)
- `OFICIO` - Querella de oficio
- `PERSONA` - Querella presentada por persona
- `ANONIMA` - Querella anónima

### TipoComunicacion
- `OFICIO` - Oficio oficial
- `NOTIFICACION` - Notificación a partes
- `CITACION` - Citación a audiencia
- `AUTO` - Auto judicial
- `RESOLUCION` - Resolución

### EstadoComunicacion
- `BORRADOR` - En elaboración
- `ENVIADO` - Enviado
- `RECIBIDO` - Recibido/confirmado

### TipoNotificacion
- `ASIGNACION` - Asignación de caso
- `CAMBIO_ESTADO` - Cambio de estado
- `COMENTARIO` - Nuevo comentario
- `SISTEMA` - Notificación del sistema
- `RECORDATORIO` - Recordatorio

### Estados de Querella
- `APERTURA` - Estado inicial
- `NOTIFICACION` - En notificación
- `AUDIENCIA_PUBLICA` - En audiencia
- `DECISION` - Decisión tomada
- `RECURSO` - En recurso
- `INADMISIBLE` - Inadmitida

---

## 🛡️ Constraints y Validaciones

### Unique Constraints
- `comuna.nombre` - UNIQUE
- `corregimiento.nombre` - UNIQUE
- `tema.nombre` - UNIQUE
- `inspeccion.nombre` - UNIQUE
- `usuarios.email` - UNIQUE
- `(estado.modulo, estado.nombre)` - UNIQUE
- `(estado_transicion.modulo, desde_estado_id, hacia_estado_id)` - UNIQUE

### Check Constraints
- `estado.modulo` IN ('QUERELLA', 'DESPACHO')
- `estado_transicion.modulo` IN ('QUERELLA', 'DESPACHO')
- `historial_estado.modulo` IN ('QUERELLA', 'DESPACHO')
- `usuarios.rol` IN ('INSPECTOR', 'DIRECTOR', 'AUXILIAR')
- `usuarios.estado` IN ('ACTIVO', 'BLOQUEADO', 'NO_DISPONIBLE')
- `querella.naturaleza` IN ('OFICIO', 'PERSONA', 'ANONIMA')
- `notificaciones.tipo` IN ('ASIGNACION', 'CAMBIO_ESTADO', 'COMENTARIO', 'SISTEMA', 'RECORDATORIO')
- `comunicaciones.tipo` IN ('OFICIO', 'NOTIFICACION', 'CITACION', 'AUTO', 'RESOLUCION')
- `comunicaciones.estado` IN ('BORRADOR', 'ENVIADO', 'RECIBIDO')

---

## 📈 Estadísticas de Uso Típico

### Volumen de Datos Esperado (Por Año)

| Tabla | Registros/Año | Crecimiento |
|-------|---------------|-------------|
| `querella` | 1,000 - 5,000 | Alto |
| `despacho_comisorio` | 500 - 2,000 | Medio |
| `comunicaciones` | 3,000 - 15,000 | Alto |
| `adjuntos` | 2,000 - 10,000 | Medio |
| `notificaciones` | 10,000 - 50,000 | Muy Alto |
| `historial_estado` | 5,000 - 25,000 | Alto |

---

## 🔐 Políticas de Seguridad

### Datos Sensibles
- `usuarios.password` - Hasheado con BCrypt (nunca en texto plano)
- `usuarios.email` - Protegido, solo acceso autorizado
- `querella.querellante_contacto` - Información personal protegida
- `adjuntos.ruta_storage` - Ruta de archivos protegida

### Auditoría
- Todas las tablas tienen `creado_en`
- Tablas críticas tienen `actualizado_en`
- `historial_estado` registra todos los cambios con usuario y motivo

---

**Versión:** 2.0.0
**Fecha:** 2025-12-10
**SGBD:** PostgreSQL 12+
