# 🚀 Guía de Despliegue en Fly.io

Guía completa para desplegar el Sistema Completo de Gestión de Querellas en Fly.io.

## 📋 Requisitos Previos

1. **Cuenta en Fly.io**: https://fly.io/app/sign-up
2. **Fly CLI instalado**: https://fly.io/docs/hands-on/install-flyctl/
3. **Git** instalado
4. **Docker** instalado (opcional, Fly.io puede construir remotamente)

### Instalar Fly CLI

```bash
# Windows (PowerShell)
iwr https://fly.io/install.ps1 -useb | iex

# macOS/Linux
curl -L https://fly.io/install.sh | sh

# Verificar instalación
flyctl version
```

### Login en Fly.io

```bash
flyctl auth login
```

---

## 🗄️ Paso 1: Crear y Configurar Base de Datos PostgreSQL

### 1.1 Crear PostgreSQL en Fly.io

```bash
# Crear app de PostgreSQL
flyctl postgres create \
  --name querellas-db \
  --region mia \
  --initial-cluster-size 1 \
  --vm-size shared-cpu-1x \
  --volume-size 10

# Guardar las credenciales que se muestran
```

**IMPORTANTE**: Guarda las credenciales que se muestran (usuario, password, host, etc.)

### 1.2 Conectarse a PostgreSQL

```bash
# Conectarse a la base de datos
flyctl postgres connect -a querellas-db
```

### 1.3 Inicializar Base de Datos

Una vez conectado al PostgreSQL:

```sql
-- Crear base de datos
CREATE DATABASE querillas;

-- Salir de psql
\q
```

Ahora ejecutar los scripts de inicialización:

```bash
# Conectarse a la base de datos querillas
flyctl postgres connect -a querellas-db -d querillas

-- Dentro de psql, ejecutar:
\i database/schema.sql
\i database/datos_iniciales.sql
\i database/indices-produccion.sql

-- Crear usuario administrador
INSERT INTO usuario (nombre, email, password, rol, zona, estado, creado_en, actualizado_en)
VALUES (
  'Administrador Sistema',
  'admin@querellas.gov.co',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW', -- Admin2025!
  'DIRECTOR',
  NULL,
  'ACTIVO',
  NOW(),
  NOW()
);

\q
```

### 1.4 Obtener URL de Conexión

```bash
# Obtener connection string
flyctl postgres db list -a querellas-db

# El formato será algo como:
# postgres://postgres:PASSWORD@querellas-db.internal:5432/querillas
```

---

## 🔧 Paso 2: Desplegar Backend (Spring Boot)

### 2.1 Navegar al directorio del backend

```bash
cd back
```

### 2.2 Crear app en Fly.io

```bash
# Inicializar app (usará fly.toml existente)
flyctl launch --no-deploy

# O crear manualmente
flyctl apps create querellas-backend --org personal
```

### 2.3 Configurar Secrets (Variables de Entorno)

```bash
# Configurar URL de base de datos
flyctl secrets set \
  DATABASE_URL="postgres://postgres:PASSWORD@querellas-db.internal:5432/querillas" \
  -a querellas-backend

# Configurar JWT secret (generar uno nuevo y seguro)
flyctl secrets set \
  JWT_SECRET="TU_SECRET_SUPER_SEGURO_AQUI_MIN_32_CARACTERES" \
  -a querellas-backend

# Configurar CORS (URL del frontend - se configurará después)
flyctl secrets set \
  CORS_ALLOWED_ORIGINS="https://querellas-frontend.fly.dev" \
  -a querellas-backend
```

### 2.4 Crear archivo de configuración de producción

Editar `src/main/resources/application-prod.properties`:

```properties
# PostgreSQL desde variable de entorno
spring.datasource.url=${DATABASE_URL}

# JWT desde variable de entorno
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-minutes=120

# CORS desde variable de entorno
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Actuator para health checks
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### 2.5 Desplegar Backend

```bash
# Desplegar
flyctl deploy -a querellas-backend

# Verificar logs
flyctl logs -a querellas-backend

# Verificar estado
flyctl status -a querellas-backend
```

### 2.6 Verificar Backend

```bash
# Obtener URL del backend
flyctl info -a querellas-backend

# Probar endpoint
curl https://querellas-backend.fly.dev/api/ping
```

---

## 🎨 Paso 3: Desplegar Frontend (Next.js)

### 3.1 Navegar al directorio del frontend

```bash
cd ../front
```

### 3.2 Crear app en Fly.io

```bash
# Inicializar app
flyctl launch --no-deploy

# O crear manualmente
flyctl apps create querellas-frontend --org personal
```

### 3.3 Configurar Variables de Entorno

```bash
# Configurar URL del backend
flyctl secrets set \
  NEXT_PUBLIC_API_URL="https://querellas-backend.fly.dev" \
  -a querellas-frontend
```

### 3.4 Desplegar Frontend

```bash
# Desplegar
flyctl deploy -a querellas-frontend

# Verificar logs
flyctl logs -a querellas-frontend

# Verificar estado
flyctl status -a querellas-frontend
```

### 3.5 Verificar Frontend

```bash
# Obtener URL
flyctl info -a querellas-frontend

# Abrir en navegador
flyctl open -a querellas-frontend
```

---

## 🔄 Paso 4: Actualizar CORS en Backend

Una vez que el frontend esté desplegado, actualizar CORS:

```bash
cd ../back

# Actualizar CORS con la URL real del frontend
flyctl secrets set \
  CORS_ALLOWED_ORIGINS="https://querellas-frontend.fly.dev" \
  -a querellas-backend

# Redesplegar backend para aplicar cambios
flyctl deploy -a querellas-backend
```

---

## ✅ Paso 5: Verificar Despliegue Completo

### 5.1 URLs del Sistema

```bash
# Backend
echo "Backend: https://querellas-backend.fly.dev"
echo "Swagger: https://querellas-backend.fly.dev/swagger-ui/index.html"
echo "Health: https://querellas-backend.fly.dev/actuator/health"

# Frontend
echo "Frontend: https://querellas-frontend.fly.dev"
```

### 5.2 Prueba End-to-End

1. **Abrir Frontend**: https://querellas-frontend.fly.dev
2. **Login con usuario admin**:
   - Email: `admin@querellas.gov.co`
   - Password: `Admin2025!`
3. **Cambiar contraseña** inmediatamente
4. **Crear una querella de prueba**
5. **Generar reporte Excel**

---

## 📊 Monitoreo y Logs

### Ver logs en tiempo real

```bash
# Backend
flyctl logs -a querellas-backend

# Frontend
flyctl logs -a querellas-frontend

# PostgreSQL
flyctl logs -a querellas-db
```

### Monitorear métricas

```bash
# Dashboard de Fly.io
flyctl dashboard

# Métricas del backend
flyctl metrics -a querellas-backend

# Métricas del frontend
flyctl metrics -a querellas-frontend
```

### SSH a las máquinas

```bash
# Conectarse al backend
flyctl ssh console -a querellas-backend

# Conectarse al frontend
flyctl ssh console -a querellas-frontend
```

---

## 🔧 Comandos Útiles

### Escalar aplicaciones

```bash
# Escalar backend
flyctl scale vm shared-cpu-2x --memory 2048 -a querellas-backend

# Escalar frontend
flyctl scale vm shared-cpu-1x --memory 1024 -a querellas-frontend

# Escalar número de instancias
flyctl scale count 2 -a querellas-backend
```

### Gestión de secretos

```bash
# Listar secretos
flyctl secrets list -a querellas-backend

# Eliminar secreto
flyctl secrets unset SECRET_NAME -a querellas-backend

# Ver valores (no muestra los valores reales)
flyctl secrets list -a querellas-backend
```

### Backup de base de datos

```bash
# Crear backup
flyctl postgres backup create -a querellas-db

# Listar backups
flyctl postgres backup list -a querellas-db
```

---

## 🔐 Seguridad en Producción

### Checklist de Seguridad

- [ ] Cambiar contraseña del usuario admin
- [ ] Generar JWT secret único y seguro (mínimo 32 caracteres)
- [ ] Configurar CORS correctamente
- [ ] Verificar que las variables de entorno sensibles están en secrets
- [ ] Habilitar HTTPS (automático en Fly.io)
- [ ] Configurar rate limiting (opcional)
- [ ] Revisar logs regularmente
- [ ] Configurar alertas de monitoreo
- [ ] Realizar backups periódicos de la BD

### Generar JWT Secret Seguro

```bash
# Generar secret aleatorio de 64 caracteres
openssl rand -base64 64 | tr -d '\n'

# O usando Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

---

## 💰 Costos Estimados (Fly.io)

### Plan Gratuito (Hobby)
- **Backend**: shared-cpu-1x, 1GB RAM → ~$5/mes
- **Frontend**: shared-cpu-1x, 512MB RAM → ~$3/mes
- **PostgreSQL**: shared-cpu-1x, 10GB → ~$5/mes
- **TOTAL**: ~$13/mes

### Plan Recomendado para Producción
- **Backend**: shared-cpu-2x, 2GB RAM → ~$10/mes
- **Frontend**: shared-cpu-1x, 1GB RAM → ~$5/mes
- **PostgreSQL**: dedicated-cpu-1x, 20GB → ~$15/mes
- **TOTAL**: ~$30/mes

*Precios aproximados, verificar en https://fly.io/pricing*

---

## 🔄 Actualizaciones y Redespliegues

### Actualizar Backend

```bash
cd back

# Pull últimos cambios
git pull origin main

# Redesplegar
flyctl deploy -a querellas-backend
```

### Actualizar Frontend

```bash
cd front

# Pull últimos cambios
git pull origin main

# Redesplegar
flyctl deploy -a querellas-frontend
```

### Rollback a versión anterior

```bash
# Listar releases
flyctl releases -a querellas-backend

# Rollback a release específico
flyctl releases rollback <release-id> -a querellas-backend
```

---

## 🆘 Troubleshooting

### Backend no inicia

```bash
# Ver logs detallados
flyctl logs -a querellas-backend

# Verificar secretos
flyctl secrets list -a querellas-backend

# Verificar configuración
flyctl config show -a querellas-backend

# Reiniciar
flyctl restart -a querellas-backend
```

### Frontend no conecta con Backend

1. Verificar `NEXT_PUBLIC_API_URL` está configurado
2. Verificar CORS en backend incluye URL del frontend
3. Verificar que backend esté corriendo (`flyctl status -a querellas-backend`)

### Error de conexión a BD

```bash
# Verificar que PostgreSQL esté corriendo
flyctl status -a querellas-db

# Verificar connection string
flyctl postgres db list -a querellas-db

# Verificar que DATABASE_URL en backend sea correcto
flyctl secrets list -a querellas-backend
```

### Build falla

```bash
# Ver logs de build
flyctl logs -a querellas-backend

# Build local para debug
flyctl deploy --local-only -a querellas-backend
```

---

## 📞 Soporte

- **Fly.io Docs**: https://fly.io/docs/
- **Fly.io Community**: https://community.fly.io/
- **Fly.io Status**: https://status.flyio.net/

---

## 🎉 Siguiente Pasos Post-Despliegue

1. **Configurar dominio personalizado** (opcional)
2. **Configurar alertas** de monitoreo
3. **Configurar backups** automáticos
4. **Crear usuarios** adicionales
5. **Importar datos** de producción (si aplica)
6. **Configurar CI/CD** para despliegues automáticos
7. **Documentar** procedimientos operativos

---

**Versión**: 1.0.0
**Última actualización**: Diciembre 2025
**Estado**: ✅ Listo para Producción
