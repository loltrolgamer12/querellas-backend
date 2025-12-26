# 📋 PLAN COMPLETO PARA 100% COBERTURA DE TESTS
## Sistema de Querellas - Alcaldía de Neiva

---

## 🎯 OBJETIVO

Alcanzar **100% de cobertura** de tests automatizados en el backend del sistema de querellas.

**Estado Actual:** 85-90% (90+ tests)
**Meta:** 100% (~230+ tests totales)
**Tests Faltantes:** ~140 tests
**Tiempo Estimado Total:** 6-8 horas

---

## 📊 ESTRATEGIA DE IMPLEMENTACIÓN

### Principios del Plan

1. **Prioridad por Criticidad:** Testear primero lo más crítico para el negocio
2. **Eficiencia:** Batch similar tests (todos los services, luego controllers, etc.)
3. **Validación Continua:** Verificar cobertura después de cada fase
4. **Documentación:** Actualizar reporte de cobertura en cada fase

---

## 🏗️ FASES DEL PLAN

---

## **FASE 1: SERVICIOS RESTANTES**
**Prioridad:** 🔴 ALTA
**Duración Estimada:** 2-3 horas
**Tests a Crear:** ~40-50 tests

### 1.1 NotificacionService (CRÍTICO)
**Archivo:** `NotificacionServiceTest.java`
**Tests Estimados:** 8-10 tests

**Métodos a Testear:**
- ✅ `crear(NotificacionCreateDTO)` - 2 tests
  - Creación exitosa
  - Validación de usuario destinatario existe

- ✅ `listarPorUsuario(Long usuarioId)` - 2 tests
  - Listar todas las notificaciones
  - Filtrar por leídas/no leídas

- ✅ `marcarComoLeida(Long id)` - 2 tests
  - Marcar exitosamente
  - Excepción si no existe

- ✅ `marcarTodasComoLeidas(Long usuarioId)` - 1 test
  - Marcar múltiples notificaciones

- ✅ `contarNoLeidas(Long usuarioId)` - 1 test
  - Contar notificaciones pendientes

**Complejidad:** ⭐⭐ (Media)

---

### 1.2 ComunicacionService
**Archivo:** `ComunicacionServiceTest.java`
**Tests Estimados:** 10-12 tests

**Métodos a Testear:**
- ✅ `crear(Long querellaId, ComunicacionCreateDTO)` - 3 tests
  - Creación exitosa
  - Validación querella existe
  - Validación usuario remitente existe

- ✅ `listarPorQuerella(Long querellaId)` - 2 tests
  - Listar todas
  - Ordenar por fecha DESC

- ✅ `obtenerPorId(Long id)` - 2 tests
  - Obtención exitosa
  - Excepción si no existe

- ✅ `actualizar(Long id, ComunicacionUpdateDTO)` - 2 tests
  - Actualización exitosa
  - Excepción si no existe

- ✅ `eliminar(Long id)` - 1 test
  - Eliminación exitosa

**Complejidad:** ⭐⭐ (Media)

---

### 1.3 AdjuntoService (CRÍTICO)
**Archivo:** `AdjuntoServiceTest.java`
**Tests Estimados:** 12-15 tests

**Métodos a Testear:**
- ✅ `subirAdjunto(Long querellaId, MultipartFile)` - 4 tests
  - Subida exitosa
  - Validación querella existe
  - Validación archivo no vacío
  - Validación tipo de archivo permitido

- ✅ `descargarAdjunto(Long id)` - 3 tests
  - Descarga exitosa
  - Excepción si no existe
  - Excepción si archivo no existe en disco

- ✅ `listarPorQuerella(Long querellaId)` - 2 tests
  - Listar todos los adjuntos
  - Ordenar por fecha subida

- ✅ `eliminar(Long id)` - 3 tests
  - Eliminación exitosa (archivo + BD)
  - Excepción si no existe
  - Manejo si archivo ya fue eliminado del disco

**Complejidad:** ⭐⭐⭐ (Alta - manejo de archivos)

---

### 1.4 ExcelExportService
**Archivo:** `ExcelExportServiceTest.java`
**Tests Estimados:** 8-10 tests

**Métodos a Testear:**
- ✅ `generarReporteQuerellas(LocalDate, LocalDate)` - 4 tests
  - Generación exitosa con datos
  - Generación con lista vacía
  - Verificar estructura del Excel (10 columnas)
  - Verificar formato de fechas

- ✅ `aplicarEstilos(Workbook)` - 2 tests
  - Headers con fondo gris
  - Bordes en celdas

- ✅ `escribirFila(Row, QuerellaReporteDTO)` - 2 tests
  - Datos correctamente mapeados
  - Manejo de valores nulos

**Complejidad:** ⭐⭐⭐ (Alta - Apache POI)

---

### 1.5 DespachoExcelService
**Archivo:** `DespachoExcelServiceTest.java`
**Tests Estimados:** 6-8 tests

**Métodos a Testear:**
- ✅ `generarReporteExcel(LocalDate, LocalDate)` - 3 tests
  - Generación exitosa
  - Verificar 10 columnas formato FOR-GGOJ-81
  - Formato de fechas correcto

- ✅ `escribirFila(Row, DespachoComitorioReporteDTO)` - 2 tests
  - Mapeo correcto de datos
  - Manejo de valores nulos

**Complejidad:** ⭐⭐⭐ (Alta - Apache POI)

---

### ✅ Checklist Fase 1

```bash
[ ] NotificacionServiceTest.java creado
[ ] ComunicacionServiceTest.java creado
[ ] AdjuntoServiceTest.java creado
[ ] ExcelExportServiceTest.java creado
[ ] DespachoExcelServiceTest.java creado
[ ] Todos los tests pasan (mvn test)
[ ] Commit: "Agregar tests completos de servicios restantes"
```

---

## **FASE 2: REPOSITORIES CRÍTICOS**
**Prioridad:** 🟡 MEDIA-ALTA
**Duración Estimada:** 2-3 horas
**Tests a Crear:** ~40-45 tests

### 2.1 QuerellaRepository (CRÍTICO)
**Archivo:** `QuerellaRepositoryTest.java`
**Tests Estimados:** 10-12 tests

**Queries Complejas a Testear:**
- ✅ `buscarPosiblesDuplicados(...)` - 3 tests
  - Encontrar por dirección similar
  - Encontrar por tema y comuna
  - Ventana de tiempo correcta

- ✅ Queries de filtrado en `listarBandeja` - 4 tests
  - Filtro por texto (radicado, dirección)
  - Filtro por estado actual
  - Filtro por inspector
  - Filtro por fecha

- ✅ Joins con otras tablas - 2 tests
  - LEFT JOIN con Tema
  - LEFT JOIN con Comuna

**Complejidad:** ⭐⭐⭐⭐ (Muy Alta - SQL complejo)

---

### 2.2 DespachoComitorioRepository
**Archivo:** `DespachoComitorioRepositoryTest.java`
**Tests Estimados:** 8-10 tests

**Métodos a Testear:**
- ✅ `findByNumeroDespacho(String)` - 2 tests
- ✅ `findByInspectorAsignadoId(Long)` - 2 tests
- ✅ `findByEntidadProcedenteContainingIgnoreCase(String)` - 2 tests
- ✅ `findByFechaRecibidoBetween(...)` - 2 tests
- ✅ `findPendientes()` - 1 test
- ✅ `findDevueltos()` - 1 test
- ✅ `existsByNumeroDespacho(String)` - 1 test

**Complejidad:** ⭐⭐ (Media)

---

### 2.3 HistorialEstadoRepository
**Archivo:** `HistorialEstadoRepositoryTest.java`
**Tests Estimados:** 6-8 tests

**Métodos a Testear:**
- ✅ `findByModuloAndCasoIdOrderByCreadoEnDesc(...)` - 3 tests
  - Encontrar historial completo
  - Ordenamiento correcto
  - Filtro por módulo (QUERELLA vs DESPACHO)

- ✅ `findUltimoEstadoNombre(...)` - 3 tests
  - Encontrar último estado
  - Retornar vacío si no hay historial
  - Manejo de múltiples estados

**Complejidad:** ⭐⭐⭐ (Alta)

---

### 2.4 Repositories Simples
**Archivos:**
- `TemaRepositoryTest.java` (3-4 tests)
- `ComunaRepositoryTest.java` (3-4 tests)
- `EstadoRepositoryTest.java` (4-5 tests)
- `EstadoTransicionRepositoryTest.java` (4-5 tests)
- `AdjuntoRepositoryTest.java` (3-4 tests)
- `ComunicacionRepositoryTest.java` (3-4 tests)
- `NotificacionRepositoryTest.java` (4-5 tests)

**Total:** ~24-30 tests

**Métodos Comunes a Testear:**
- ✅ `findById()` / `findAll()`
- ✅ `save()` / `delete()`
- ✅ Queries personalizadas (ej: `findByModuloAndNombre`)
- ✅ `existeTransicion()` para EstadoTransicionRepository

**Complejidad:** ⭐ (Baja - CRUD simple)

---

### ✅ Checklist Fase 2

```bash
[ ] QuerellaRepositoryTest.java creado
[ ] DespachoComitorioRepositoryTest.java creado
[ ] HistorialEstadoRepositoryTest.java creado
[ ] TemaRepositoryTest.java creado
[ ] ComunaRepositoryTest.java creado
[ ] EstadoRepositoryTest.java creado
[ ] EstadoTransicionRepositoryTest.java creado
[ ] AdjuntoRepositoryTest.java creado
[ ] ComunicacionRepositoryTest.java creado
[ ] NotificacionRepositoryTest.java creado
[ ] Todos los tests con @DataJpaTest pasan
[ ] Commit: "Agregar tests completos de repositorios"
```

---

## **FASE 3: CONTROLLERS REST**
**Prioridad:** 🟡 MEDIA
**Duración Estimada:** 3-4 horas
**Tests a Crear:** ~60-70 tests

### Estrategia para Controllers

**Framework:** @WebMvcTest + MockMvc
**Patrón:** Testear cada endpoint con:
1. ✅ Request válido → 200/201 OK
2. ✅ Request inválido → 400 Bad Request
3. ✅ Recurso no existe → 404 Not Found
4. ✅ Sin autorización → 401/403 Forbidden
5. ✅ Validación de @PreAuthorize roles

---

### 3.1 QuerellaController (CRÍTICO)
**Archivo:** `QuerellaControllerTest.java`
**Tests Estimados:** 12-15 tests

**Endpoints (7 total):**
- `POST /api/querellas` - 3 tests (crear OK, validación, sin permiso)
- `GET /api/querellas/{id}` - 3 tests
- `GET /api/querellas` - 2 tests (paginación, filtros)
- `PUT /api/querellas/{id}/asignar-inspector` - 2 tests
- `PUT /api/querellas/{id}/cambiar-estado` - 2 tests
- `GET /api/querellas/{id}/historial-estados` - 1 test
- `GET /api/querellas/{id}/duplicados` - 1 test

**Complejidad:** ⭐⭐⭐ (Alta)

---

### 3.2 DespachoComitorioController
**Archivo:** `DespachoComitorioControllerTest.java`
**Tests Estimados:** 15-18 tests

**Endpoints (12 total):**
- `POST /api/despachos-comisorios` - 3 tests
- `GET /api/despachos-comisorios` - 2 tests
- `GET /api/despachos-comisorios/{id}` - 2 tests
- `PUT /api/despachos-comisorios/{id}` - 2 tests
- `DELETE /api/despachos-comisorios/{id}` - 2 tests
- `GET /api/despachos-comisorios/pendientes` - 1 test
- `GET /api/despachos-comisorios/devueltos` - 1 test
- `GET /api/despachos-comisorios/inspector/{id}` - 1 test
- `PUT /api/despachos-comisorios/{id}/asignar-inspector` - 1 test
- `PUT /api/despachos-comisorios/{id}/marcar-devuelto` - 1 test
- `GET /api/despachos-comisorios/reporte` - 1 test
- `GET /api/despachos-comisorios/reporte/excel` - 1 test

**Complejidad:** ⭐⭐⭐ (Alta)

---

### 3.3 UsuarioController
**Archivo:** `UsuarioControllerTest.java`
**Tests Estimados:** 10-12 tests

**Endpoints (7 total):**
- `POST /api/usuarios` - 3 tests
- `GET /api/usuarios` - 2 tests
- `GET /api/usuarios/{id}` - 2 tests
- `PUT /api/usuarios/{id}` - 2 tests
- `PUT /api/usuarios/{id}/cambiar-estado` - 1 test
- `DELETE /api/usuarios/{id}` - 1 test
- `GET /api/usuarios/inspectores` - 2 tests

**Complejidad:** ⭐⭐⭐ (Alta)

---

### 3.4 AuthController
**Archivo:** `AuthControllerTest.java`
**Tests Estimados:** 6-8 tests

**Endpoints (3 total):**
- `POST /api/auth/register` - 3 tests
- `POST /api/auth/login` - 3 tests (OK, credenciales inválidas, usuario bloqueado)
- `POST /api/auth/refresh` - 2 tests

**Complejidad:** ⭐⭐⭐⭐ (Muy Alta - JWT, Security)

---

### 3.5 Controllers Menores
**Archivos:**
- `AdjuntoControllerTest.java` (6-8 tests) - Upload/download archivos
- `ComunicacionControllerTest.java` (8-10 tests) - CRUD
- `NotificacionControllerTest.java` (6-8 tests) - Listar/marcar
- `CatalogoControllerTest.java` (12-15 tests) - 8 endpoints (temas + comunas)
- `ReporteControllerTest.java` (6-8 tests) - Reportes Excel
- `PingControllerTest.java` (1-2 tests) - Health check

**Total:** ~39-51 tests

**Complejidad:** ⭐⭐ (Media)

---

### ✅ Checklist Fase 3

```bash
[ ] QuerellaControllerTest.java creado
[ ] DespachoComitorioControllerTest.java creado
[ ] UsuarioControllerTest.java creado
[ ] AuthControllerTest.java creado
[ ] AdjuntoControllerTest.java creado
[ ] ComunicacionControllerTest.java creado
[ ] NotificacionControllerTest.java creado
[ ] CatalogoControllerTest.java creado
[ ] ReporteControllerTest.java creado
[ ] PingControllerTest.java creado
[ ] Todos los tests con @WebMvcTest pasan
[ ] Commit: "Agregar tests completos de controllers REST"
```

---

## **FASE 4: VERIFICACIÓN Y OPTIMIZACIÓN**
**Prioridad:** 🟢 MEDIA
**Duración Estimada:** 1 hora

### 4.1 Ejecutar Suite Completa
```bash
mvn clean test
```

### 4.2 Generar Reporte JaCoCo
```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

### 4.3 Verificar Métricas
- ✅ **Cobertura de Líneas:** > 95%
- ✅ **Cobertura de Ramas:** > 90%
- ✅ **Cobertura de Métodos:** > 95%

### 4.4 Identificar Gaps
- Revisar clases con < 90% cobertura
- Crear tests adicionales si es necesario

### 4.5 Optimizar Tests Lentos
- Identificar tests que tardan > 5 segundos
- Optimizar setup/teardown
- Usar @MockBean solo cuando sea necesario

### ✅ Checklist Fase 4

```bash
[ ] Suite completa ejecutada sin errores
[ ] Reporte JaCoCo generado
[ ] Cobertura >= 95% en todos los módulos
[ ] Tests optimizados (< 3 min total)
[ ] TEST_COVERAGE_REPORT.md actualizado
[ ] Commit: "Verificar 100% cobertura y optimizar suite"
```

---

## 📈 PROGRESO ESTIMADO

| Fase | Tests | Duración | Estado |
|------|-------|----------|--------|
| **Actual** | 90 | - | ✅ Completo |
| **Fase 1: Servicios** | +45 | 2-3h | ⏳ Pendiente |
| **Fase 2: Repositories** | +43 | 2-3h | ⏳ Pendiente |
| **Fase 3: Controllers** | +65 | 3-4h | ⏳ Pendiente |
| **Fase 4: Verificación** | - | 1h | ⏳ Pendiente |
| **TOTAL** | ~243 tests | 8-11h | - |

---

## 🎯 HITOS Y COMMITS

### Commit 1: Servicios Restantes
```bash
git commit -m "Agregar tests completos de servicios restantes

- NotificacionServiceTest: 10 tests
- ComunicacionServiceTest: 12 tests
- AdjuntoServiceTest: 15 tests
- ExcelExportServiceTest: 10 tests
- DespachoExcelServiceTest: 8 tests

Total: 55 tests nuevos
Cobertura servicios: 100%"
```

### Commit 2: Repositories
```bash
git commit -m "Agregar tests completos de repositorios

- QuerellaRepositoryTest: 12 tests (queries complejas)
- DespachoComitorioRepositoryTest: 10 tests
- HistorialEstadoRepositoryTest: 8 tests
- 7 repositories adicionales: 24 tests

Total: 54 tests nuevos
Cobertura repositories: 100%"
```

### Commit 3: Controllers
```bash
git commit -m "Agregar tests completos de controllers REST

- QuerellaControllerTest: 15 tests
- DespachoComitorioControllerTest: 18 tests
- UsuarioControllerTest: 12 tests
- AuthControllerTest: 8 tests
- 6 controllers adicionales: 47 tests

Total: 100 tests nuevos con @WebMvcTest
Cobertura controllers: 100%"
```

### Commit 4: Verificación Final
```bash
git commit -m "Alcanzar 100% cobertura de tests

- Suite completa: 243 tests
- Cobertura de líneas: 98%
- Cobertura de ramas: 95%
- Reporte JaCoCo actualizado
- TEST_COVERAGE_REPORT.md actualizado

✅ Objetivo de 100% cobertura alcanzado"
```

---

## 📋 RECURSOS NECESARIOS

### Dependencias (Ya Instaladas)
- ✅ JUnit 5
- ✅ Mockito
- ✅ Spring Boot Test
- ✅ AssertJ
- ✅ JaCoCo
- ✅ Testcontainers (PostgreSQL)

### Archivos de Configuración
- ✅ `pom.xml` con JaCoCo configurado
- ⚠️ `application-test.properties` (crear si no existe)
- ⚠️ `TestSecurityConfig.java` (crear si no existe)

---

## 🚀 EJECUCIÓN DEL PLAN

### Opción 1: Secuencial (Recomendado)
Ejecutar fase por fase, verificando que cada una pase antes de continuar.

**Ventajas:**
- ✅ Mayor control
- ✅ Fácil identificar problemas
- ✅ Commits incrementales

**Comando por Fase:**
```bash
# Fase 1
mvn test -Dtest="*ServiceTest"

# Fase 2
mvn test -Dtest="*RepositoryTest"

# Fase 3
mvn test -Dtest="*ControllerTest"

# Fase 4
mvn clean test jacoco:report
```

### Opción 2: Paralelo (Avanzado)
Crear múltiples archivos de test simultáneamente.

**Ventajas:**
- ✅ Más rápido
- ✅ Mejor para equipos

**Desventajas:**
- ⚠️ Riesgo de conflictos
- ⚠️ Difícil debugging

---

## ⚡ OPTIMIZACIONES

### Tests Más Rápidos
1. **Usar H2 in-memory** para @DataJpaTest (no PostgreSQL)
2. **@MockBean solo cuando necesario** (preferir @Mock)
3. **Reutilizar fixtures** con @BeforeEach
4. **Tests paralelos** con Maven Surefire:
   ```xml
   <parallel>classes</parallel>
   <threadCount>4</threadCount>
   ```

### Mejor Mantenibilidad
1. **Test Builders** para DTOs complejos
2. **Test Data Factories** para entidades
3. **Custom Matchers** para assertions complejas
4. **@Nested tests** para agrupar por escenario

---

## 🎓 PATRONES Y MEJORES PRÁCTICAS

### Patrón AAA (Arrange-Act-Assert)
```java
@Test
void metodo_DebeHacerX() {
    // Arrange - Preparar datos
    var dto = new DTO();
    when(repo.findById(1L)).thenReturn(Optional.of(entity));

    // Act - Ejecutar método
    var result = service.metodo(dto);

    // Assert - Verificar resultado
    assertThat(result).isNotNull();
    verify(repo).save(any());
}
```

### Nombres Descriptivos
- ✅ `crear_DebeCrearUsuarioExitosamente()`
- ✅ `crear_DebeLanzarExcepcionSiEmailYaExiste()`
- ❌ `testCrear()` (poco descriptivo)

### Un Concepto por Test
- ✅ Test separado para cada caso
- ❌ Múltiples asserts no relacionados en un test

---

## 📊 MÉTRICAS DE ÉXITO

### Cobertura Objetivo
- ✅ **Líneas:** ≥ 95%
- ✅ **Ramas:** ≥ 90%
- ✅ **Métodos:** ≥ 95%
- ✅ **Clases:** 100%

### Calidad de Tests
- ✅ Tiempo total < 5 minutos
- ✅ 0 tests flakey (intermitentes)
- ✅ 0 tests ignorados (@Disabled)
- ✅ Coverage uniforme (no solo "happy path")

### Documentación
- ✅ Cada test con @DisplayName descriptivo
- ✅ TEST_COVERAGE_REPORT.md actualizado
- ✅ README.md con instrucciones de testing

---

## ✅ CRITERIOS DE ACEPTACIÓN

El plan se considera completado cuando:

1. ✅ **243+ tests** implementados y pasando
2. ✅ **Cobertura ≥ 95%** en JaCoCo
3. ✅ **0 tests fallando** en `mvn clean test`
4. ✅ **Todos los servicios** 100% cubiertos
5. ✅ **Todos los controllers** 100% cubiertos
6. ✅ **Todos los repositories** 100% cubiertos
7. ✅ **Reporte actualizado** en TEST_COVERAGE_REPORT.md
8. ✅ **4 commits** realizados (uno por fase)
9. ✅ **Push al remoto** exitoso

---

## 🔄 PRÓXIMOS PASOS DESPUÉS DEL PLAN

1. **Integración Continua (CI)**
   - Configurar GitHub Actions / GitLab CI
   - Ejecutar tests en cada push
   - Bloquear merge si tests fallan

2. **Coverage Gates**
   - Configurar JaCoCo para fallar si < 95%
   - Reporte automático en PRs

3. **Tests E2E** (Opcional)
   - Testcontainers con PostgreSQL real
   - Tests de flujos completos

4. **Performance Tests** (Opcional)
   - JMeter para carga
   - Identificar bottlenecks

---

## 📞 RESUMEN EJECUTIVO

**Situación Actual:**
- 90 tests implementados
- 85-90% cobertura
- Servicios críticos 100% cubiertos

**Plan:**
- 4 fases secuenciales
- 143 tests adicionales
- 8-11 horas de trabajo
- 4 commits incrementales

**Resultado Esperado:**
- 243 tests totales
- 95%+ cobertura
- Sistema 100% testeado
- Listo para producción

---

**Preparado por:** Claude Agent SDK
**Fecha:** 2025-12-18
**Versión:** 1.0
