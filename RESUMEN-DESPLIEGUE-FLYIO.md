# ⚡ Resumen Ejecutivo - Despliegue Fly.io

Guía rápida para desplegar el sistema en Fly.io en menos de 30 minutos.

## 🎯 Comandos Rápidos

### Opción 1: Script Automatizado (Recomendado)

```bash
# Hacer ejecutable
chmod +x deploy-flyio.sh

# Ejecutar script interactivo
./deploy-flyio.sh
```

### Opción 2: Comandos Manuales

```bash
# 1. Crear PostgreSQL
flyctl postgres create --name querellas-db --region mia

# 2. Desplegar Backend
cd back && flyctl deploy -a querellas-backend

# 3. Desplegar Frontend
cd ../front && flyctl deploy -a querellas-frontend
```

## 📋 Checklist Pre-Despliegue

- [ ] Cuenta en Fly.io creada
- [ ] Fly CLI instalado (`flyctl version`)
- [ ] Autenticado en Fly.io (`flyctl auth login`)
- [ ] Repositorio clonado localmente
- [ ] Docker instalado (opcional)

## 🔑 Secrets a Configurar

### Backend
```bash
flyctl secrets set DATABASE_URL="postgres://..." -a querellas-backend
flyctl secrets set JWT_SECRET="..." -a querellas-backend
flyctl secrets set CORS_ALLOWED_ORIGINS="https://querellas-frontend.fly.dev" -a querellas-backend
```

### Frontend
```bash
flyctl secrets set NEXT_PUBLIC_API_URL="https://querellas-backend.fly.dev" -a querellas-frontend
```

## 🗄️ Inicializar Base de Datos

```bash
# Conectarse
flyctl postgres connect -a querellas-db -d querillas

# Ejecutar scripts (dentro de psql)
\i database/schema.sql
\i database/datos_iniciales.sql
\i database/indices-produccion.sql
```

## ✅ Verificación Post-Despliegue

```bash
# Backend health
curl https://querellas-backend.fly.dev/actuator/health

# Frontend
curl https://querellas-frontend.fly.dev

# Login
curl -X POST https://querellas-backend.fly.dev/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@querellas.gov.co","password":"Admin2025!"}'
```

## 🌐 URLs del Sistema

- **Frontend**: https://querellas-frontend.fly.dev
- **Backend API**: https://querellas-backend.fly.dev
- **Swagger UI**: https://querellas-backend.fly.dev/swagger-ui/index.html
- **Health Check**: https://querellas-backend.fly.dev/actuator/health

## 💰 Costos Mensuales

- PostgreSQL (10GB): ~$5/mes
- Backend (1GB RAM): ~$5/mes
- Frontend (512MB RAM): ~$3/mes
- **TOTAL: ~$13/mes**

## 📚 Documentación Completa

Ver [DESPLIEGUE-FLYIO.md](DESPLIEGUE-FLYIO.md) para instrucciones detalladas.

## 🆘 Problemas Comunes

### Build falla
```bash
flyctl logs -a querellas-backend
```

### Backend no conecta a BD
```bash
flyctl secrets list -a querellas-backend
flyctl postgres db list -a querellas-db
```

### Frontend no carga
```bash
flyctl logs -a querellas-frontend
flyctl secrets list -a querellas-frontend
```

## 🔄 Actualizar Aplicación

```bash
# Backend
cd back && flyctl deploy -a querellas-backend

# Frontend
cd front && flyctl deploy -a querellas-frontend
```

---

**Tiempo estimado**: 20-30 minutos
**Última actualización**: Diciembre 2025
