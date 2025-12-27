# 🚀 Inicio Rápido - Despliegue en Windows

Guía ultra-rápida para desplegar en Fly.io desde Windows.

## ⚡ Pasos (5 minutos)

### 1️⃣ Instalar Fly CLI

Abre **PowerShell como Administrador** y ejecuta:

```powershell
iwr https://fly.io/install.ps1 -useb | iex
```

**Cierra y vuelve a abrir PowerShell** después de la instalación.

### 2️⃣ Autenticarse

```powershell
flyctl auth login
```

Se abrirá el navegador. Inicia sesión o crea una cuenta en Fly.io.

### 3️⃣ Ejecutar Script de Despliegue

Navega al proyecto y ejecuta:

```powershell
cd c:\Users\Usuario\OneDrive\Escritorio\torrente
.\deploy-flyio.ps1
```

Sigue las instrucciones del menú interactivo:
1. Selecciona opción **1** (Despliegue completo)
2. Confirma la creación de la base de datos (si no existe)
3. Espera a que se despliegue (5-10 minutos)

### 4️⃣ Configurar Secrets

Durante el despliegue, necesitarás configurar estos secrets:

```powershell
# Obtener URL de la base de datos
flyctl postgres db list -a querellas-db

# Configurar backend
flyctl secrets set `
  DATABASE_URL="postgres://postgres:PASSWORD@querellas-db.internal:5432/querillas" `
  JWT_SECRET="TU_SECRET_SUPER_SEGURO_MINIMO_32_CARACTERES_AQUI" `
  CORS_ALLOWED_ORIGINS="https://querellas-frontend.fly.dev" `
  -a querellas-backend

# Configurar frontend
flyctl secrets set `
  NEXT_PUBLIC_API_URL="https://querellas-backend.fly.dev" `
  -a querellas-frontend
```

**Generar JWT Secret seguro:**
```powershell
# Generar string aleatorio
[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
```

### 5️⃣ Inicializar Base de Datos

```powershell
# Conectarse a PostgreSQL
flyctl postgres connect -a querellas-db -d querillas
```

Dentro de psql, ejecutar:

```sql
\i database/schema.sql
\i database/datos_iniciales.sql
\i database/indices-produccion.sql

-- Crear usuario admin
INSERT INTO usuario (nombre, email, password, rol, zona, estado, creado_en, actualizado_en)
VALUES (
  'Administrador Sistema',
  'admin@querellas.gov.co',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkW',
  'DIRECTOR',
  NULL,
  'ACTIVO',
  NOW(),
  NOW()
);

\q
```

### 6️⃣ Verificar

```powershell
# Ver estado
.\deploy-flyio.ps1
# Seleccionar opción 6 (Verificar estado)

# Probar frontend
start https://querellas-frontend.fly.dev

# Probar backend
curl https://querellas-backend.fly.dev/actuator/health
```

## ✅ Login Inicial

- **URL**: https://querellas-frontend.fly.dev
- **Email**: `admin@querellas.gov.co`
- **Password**: `Admin2025!` (cambiar inmediatamente)

## 🆘 Si algo falla

```powershell
# Ver logs del backend
flyctl logs -a querellas-backend

# Ver logs del frontend
flyctl logs -a querellas-frontend

# Ver logs de la BD
flyctl logs -a querellas-db
```

## 📚 Más Ayuda

- [DESPLIEGUE-FLYIO.md](DESPLIEGUE-FLYIO.md) - Guía completa
- [INSTALAR-FLYCTL-WINDOWS.md](INSTALAR-FLYCTL-WINDOWS.md) - Problemas de instalación
- Fly.io Docs: https://fly.io/docs/

---

**Tiempo total estimado**: 20-30 minutos
