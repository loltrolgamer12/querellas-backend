# Pruebas de Endpoints de Exportación a Excel

Este documento contiene ejemplos para probar los endpoints de generación de reportes en Excel.

## 📋 Requisitos Previos

1. **Backend corriendo**: `http://localhost:8081`
2. **Base de datos** PostgreSQL configurada con datos de prueba
3. **Token JWT** válido (obtenido del endpoint `/auth/login`)

## 🔐 Paso 1: Obtener Token de Autenticación

### Login como Director
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "director@querellas.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "usuario": {
    "id": 1,
    "nombre": "Director",
    "email": "director@querellas.com",
    "rol": "DIRECTOR"
  }
}
```

**Guardar el token** para usarlo en las siguientes peticiones.

---

## 📊 Paso 2: Probar Endpoints de Excel

### 1. Reporte Trimestral de Querellas (Excel)

Este endpoint genera un archivo Excel con todas las querellas en un rango de fechas.

#### Ejemplo 1: Reporte completo (todos los inspectores)

```bash
curl -X GET \
  'http://localhost:8081/api/reportes/querellas-trimestral/excel?desde=2025-01-01&hasta=2025-03-31' \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -o reporte_querellas.xlsx
```

#### Ejemplo 2: Reporte filtrado por inspector

```bash
curl -X GET \
  'http://localhost:8081/api/reportes/querellas-trimestral/excel?desde=2025-01-01&hasta=2025-03-31&inspectorId=1' \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -o reporte_querellas_inspector_1.xlsx
```

#### Ejemplo 3: Reporte del mes actual

```bash
# Obtener primer y último día del mes actual
PRIMER_DIA=$(date +%Y-%m-01)
ULTIMO_DIA=$(date +%Y-%m-%d)

curl -X GET \
  "http://localhost:8081/api/reportes/querellas-trimestral/excel?desde=${PRIMER_DIA}&hasta=${ULTIMO_DIA}" \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -o reporte_mes_actual.xlsx
```

**Columnas del Excel generado:**
1. Fecha Radicado
2. Inspector Asignado
3. Zona
4. Radicado Interno
5. ID Local
6. Tema
7. Descripción
8. Género Querellante
9. Género Querellado
10. Normas Aplicables
11. Barrio
12. Comuna
13. Estado
14. Materialización Medida
15. Observaciones
16. Tiene Fallo
17. Tiene Apelación
18. Archivado

---

### 2. Reporte de Despachos Comisorios (Excel)

Este endpoint genera un archivo Excel con los despachos comisorios según el formato oficial FOR-GGOJ-81.

#### Ejemplo 1: Reporte trimestral

```bash
curl -X GET \
  'http://localhost:8081/api/despachos-comisorios/reporte/excel?desde=2025-01-01&hasta=2025-03-31' \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -o reporte_despachos.xlsx
```

#### Ejemplo 2: Reporte mensual

```bash
curl -X GET \
  'http://localhost:8081/api/despachos-comisorios/reporte/excel?desde=2025-12-01&hasta=2025-12-31' \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -o reporte_despachos_diciembre.xlsx
```

**Columnas del Excel generado (Formato FOR-GGOJ-81):**
1. ITEM
2. FECHA DE RECIBIDO
3. RADICADO DEL PROCESO
4. N° DESPACHO COMISORIO
5. ENTIDAD PROCEDENTE
6. ASUNTO
7. DEMANDANTE Y/O APODERADO
8. DEMANDADO Y/O APODERADO
9. INSPECCIÓN DE POLICÍA O CORREGIMIENTO ASIGNADO
10. FECHA DE DEVOLUCIÓN AL JUZGANDO COMITENTE

---

## 🧪 Paso 3: Verificar con Postman / Insomnia

### Configuración en Postman

1. **Crear nueva petición GET**
2. **URL**: `http://localhost:8081/api/reportes/querellas-trimestral/excel`
3. **Params**:
   - `desde`: `2025-01-01`
   - `hasta`: `2025-03-31`
   - `inspectorId`: `1` (opcional)
4. **Headers**:
   - `Authorization`: `Bearer {tu_token}`
5. **Send and Download**: Guardar el archivo .xlsx

---

## 🔍 Validación de Resultados

### Verificar que el archivo Excel se descargó correctamente

```bash
# Verificar tamaño del archivo
ls -lh reporte_querellas.xlsx

# Verificar que es un archivo Excel válido
file reporte_querellas.xlsx
# Salida esperada: Microsoft Excel 2007+
```

### Abrir el archivo Excel

El archivo descargado debería:
- ✅ Abrirse en Excel, LibreOffice Calc, o Google Sheets
- ✅ Tener encabezados en negrita con fondo gris
- ✅ Tener bordes en todas las celdas
- ✅ Mostrar datos formateados correctamente
- ✅ Tener fechas en formato dd/MM/yyyy HH:mm
- ✅ Mostrar "Sí"/"No" para valores booleanos

---

## 🔒 Permisos Requeridos

### Usuarios con acceso a exportación Excel:
- ✅ **DIRECTOR**: Acceso completo
- ✅ **AUXILIAR**: Acceso completo
- ❌ **INSPECTOR**: Sin acceso a exportación

### Usuarios de desarrollo (modo dev):
```javascript
// Director
{
  "username": "director@querellas.com",
  "password": "password123"
}

// Auxiliar
{
  "username": "auxiliar@querellas.com",
  "password": "password123"
}
```

---

## 🐛 Troubleshooting

### Error: "401 Unauthorized"
**Causa**: Token inválido o expirado
**Solución**: Obtener nuevo token con `/auth/login`

### Error: "403 Forbidden"
**Causa**: Usuario sin permisos (INSPECTOR)
**Solución**: Usar cuenta DIRECTOR o AUXILIAR

### Error: "500 Internal Server Error"
**Causa**: Error en el servidor (posiblemente sin datos en el rango)
**Solución**: Verificar logs del backend y que existan datos en las fechas solicitadas

### Error: Archivo corrupto o vacío
**Causa**: Error en la generación del Excel
**Solución**:
1. Verificar que Apache POI está en el classpath
2. Revisar logs del backend
3. Verificar que hay datos en el rango de fechas

---

## 📈 Ejemplos de Respuestas Exitosas

### Headers de respuesta

```http
HTTP/1.1 200 OK
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="reporte_trimestral_2025-01-01_2025-03-31.xlsx"
Content-Length: 45678
```

### Nombre de archivos generados

- Querellas: `reporte_trimestral_YYYY-MM-DD_YYYY-MM-DD.xlsx`
- Despachos: `reporte_despachos_YYYY-MM-DD_YYYY-MM-DD.xlsx`

---

## 🚀 Script Automatizado de Pruebas

```bash
#!/bin/bash

# Script para probar endpoints de Excel
BASE_URL="http://localhost:8081"

# 1. Login
echo "=== Obteniendo token ==="
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"director@querellas.com","password":"password123"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "Token obtenido: ${TOKEN:0:20}..."

# 2. Descargar reporte de querellas
echo -e "\n=== Descargando reporte de querellas ==="
curl -X GET \
  "${BASE_URL}/api/reportes/querellas-trimestral/excel?desde=2025-01-01&hasta=2025-03-31" \
  -H "Authorization: Bearer ${TOKEN}" \
  -o reporte_querellas.xlsx

if [ -f reporte_querellas.xlsx ]; then
  echo "✅ Reporte de querellas descargado: $(ls -lh reporte_querellas.xlsx | awk '{print $5}')"
else
  echo "❌ Error al descargar reporte de querellas"
fi

# 3. Descargar reporte de despachos
echo -e "\n=== Descargando reporte de despachos ==="
curl -X GET \
  "${BASE_URL}/api/despachos-comisorios/reporte/excel?desde=2025-01-01&hasta=2025-03-31" \
  -H "Authorization: Bearer ${TOKEN}" \
  -o reporte_despachos.xlsx

if [ -f reporte_despachos.xlsx ]; then
  echo "✅ Reporte de despachos descargado: $(ls -lh reporte_despachos.xlsx | awk '{print $5}')"
else
  echo "❌ Error al descargar reporte de despachos"
fi

echo -e "\n=== Pruebas completadas ==="
```

**Guardar como**: `test_excel_endpoints.sh`
**Ejecutar**: `chmod +x test_excel_endpoints.sh && ./test_excel_endpoints.sh`

---

## 📝 Notas Adicionales

1. **Rango de fechas**: El formato debe ser `YYYY-MM-DD` (ISO 8601)
2. **Zona horaria**: El backend usa UTC
3. **Tamaño de archivo**: Depende de la cantidad de registros
4. **Timeout**: Para reportes muy grandes, aumentar timeout del cliente
5. **Memoria**: Reportes de +10,000 registros pueden requerir más memoria JVM

---

## ✅ Checklist de Pruebas

- [ ] Backend corriendo en puerto 8081
- [ ] Base de datos PostgreSQL conectada
- [ ] Token JWT obtenido correctamente
- [ ] Endpoint de querellas Excel probado
- [ ] Endpoint de despachos Excel probado
- [ ] Archivos Excel se abren correctamente
- [ ] Formato de Excel es correcto (bordes, estilos, datos)
- [ ] Filtro por inspector funciona (querellas)
- [ ] Filtro por fechas funciona en ambos endpoints
- [ ] Permisos verificados (Director/Auxiliar sí, Inspector no)

---

**Última actualización**: Diciembre 2025
**Versión del sistema**: 1.0.0
