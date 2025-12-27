# ================================================================
# Script de Despliegue en Fly.io para Windows PowerShell
# Sistema Completo de Gestión de Querellas
# ================================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        🚀 DESPLIEGUE EN FLY.IO - WINDOWS POWERSHELL         ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ================================================================
# Funciones auxiliares
# ================================================================

function Check-Flyctl {
    try {
        $version = flyctl version 2>$null
        Write-Host "✅ Fly CLI instalado: $version" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "❌ Fly CLI no está instalado" -ForegroundColor Red
        Write-Host ""
        Write-Host "Instalar con PowerShell (como Administrador):" -ForegroundColor Yellow
        Write-Host "  iwr https://fly.io/install.ps1 -useb | iex" -ForegroundColor White
        Write-Host ""
        Write-Host "O ver: INSTALAR-FLYCTL-WINDOWS.md" -ForegroundColor Yellow
        return $false
    }
}

function Check-Auth {
    try {
        flyctl auth whoami 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Autenticado en Fly.io" -ForegroundColor Green
            return $true
        }
    }
    catch {}

    Write-Host "❌ No estás autenticado en Fly.io" -ForegroundColor Red
    Write-Host "Ejecuta: flyctl auth login" -ForegroundColor Yellow
    return $false
}

# ================================================================
# Verificaciones iniciales
# ================================================================

Write-Host "=== Verificando requisitos ===" -ForegroundColor Yellow
Write-Host ""

if (-not (Check-Flyctl)) {
    exit 1
}

if (-not (Check-Auth)) {
    Write-Host ""
    Write-Host "¿Quieres autenticarte ahora? (s/n): " -NoNewline -ForegroundColor Cyan
    $auth = Read-Host
    if ($auth -eq "s" -or $auth -eq "S") {
        flyctl auth login
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Error en autenticación" -ForegroundColor Red
            exit 1
        }
    }
    else {
        exit 1
    }
}

# ================================================================
# Menú de opciones
# ================================================================

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              OPCIONES DE DESPLIEGUE                          ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Despliegue completo (BD + Backend + Frontend)" -ForegroundColor White
Write-Host "2. Solo Backend" -ForegroundColor White
Write-Host "3. Solo Frontend" -ForegroundColor White
Write-Host "4. Solo crear Base de Datos" -ForegroundColor White
Write-Host "5. Ver logs" -ForegroundColor White
Write-Host "6. Verificar estado" -ForegroundColor White
Write-Host "7. Salir" -ForegroundColor White
Write-Host ""
$option = Read-Host "Selecciona una opción (1-7)"

switch ($option) {
    "1" {
        Write-Host ""
        Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║              DESPLIEGUE COMPLETO                             ║" -ForegroundColor Cyan
        Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

        # Verificar BD
        Write-Host ""
        Write-Host "📊 Paso 1/3: Verificando Base de Datos..." -ForegroundColor Yellow

        flyctl status -a querellas-db 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Base de datos ya existe" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  Base de datos no existe" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "¿Quieres crearla ahora? (s/n): " -NoNewline -ForegroundColor Cyan
            $createDB = Read-Host

            if ($createDB -eq "s" -or $createDB -eq "S") {
                Write-Host "Creando base de datos..." -ForegroundColor Yellow
                flyctl postgres create --name querellas-db --region mia --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 10

                if ($LASTEXITCODE -ne 0) {
                    Write-Host "❌ Error creando base de datos" -ForegroundColor Red
                    exit 1
                }

                Write-Host "✅ Base de datos creada" -ForegroundColor Green
                Write-Host ""
                Write-Host "⚠️  IMPORTANTE: Guarda las credenciales mostradas arriba" -ForegroundColor Yellow
                Write-Host ""
                Write-Host "Presiona Enter para continuar..." -ForegroundColor Cyan
                Read-Host
            }
            else {
                Write-Host "❌ No se puede continuar sin base de datos" -ForegroundColor Red
                exit 1
            }
        }

        # Backend
        Write-Host ""
        Write-Host "🔧 Paso 2/3: Desplegando Backend..." -ForegroundColor Yellow

        # Verificar secrets
        Write-Host "Verificando secretos del backend..." -ForegroundColor White
        $secrets = flyctl secrets list -a querellas-backend 2>&1

        if ($secrets -match "DATABASE_URL" -and $secrets -match "JWT_SECRET") {
            Write-Host "✅ Secretos ya configurados" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  Necesitas configurar los secretos primero" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Ver: DESPLIEGUE-FLYIO.md - Paso 2.3" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Ejemplo:" -ForegroundColor White
            Write-Host '  flyctl secrets set DATABASE_URL="postgres://..." -a querellas-backend' -ForegroundColor Gray
            Write-Host '  flyctl secrets set JWT_SECRET="..." -a querellas-backend' -ForegroundColor Gray
            Write-Host ""
            Write-Host "¿Continuar de todas formas? (s/n): " -NoNewline -ForegroundColor Cyan
            $continue = Read-Host

            if ($continue -ne "s" -and $continue -ne "S") {
                exit 1
            }
        }

        Set-Location back
        flyctl deploy -a querellas-backend

        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Error desplegando backend" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        Write-Host "✅ Backend desplegado" -ForegroundColor Green
        Set-Location ..

        # Frontend
        Write-Host ""
        Write-Host "🎨 Paso 3/3: Desplegando Frontend..." -ForegroundColor Yellow

        Set-Location front
        flyctl deploy -a querellas-frontend

        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Error desplegando frontend" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        Write-Host "✅ Frontend desplegado" -ForegroundColor Green
        Set-Location ..

        Write-Host ""
        Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║              ✅ DESPLIEGUE COMPLETO EXITOSO                  ║" -ForegroundColor Green
        Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
        Write-Host ""
        Write-Host "URLs del sistema:" -ForegroundColor Yellow
        Write-Host "  Frontend: https://querellas-frontend.fly.dev" -ForegroundColor White
        Write-Host "  Backend:  https://querellas-backend.fly.dev" -ForegroundColor White
        Write-Host "  Swagger:  https://querellas-backend.fly.dev/swagger-ui" -ForegroundColor White
    }

    "2" {
        Write-Host ""
        Write-Host "🔧 Desplegando Backend..." -ForegroundColor Yellow
        Set-Location back
        flyctl deploy -a querellas-backend
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Backend desplegado" -ForegroundColor Green
        }
        else {
            Write-Host "❌ Error desplegando backend" -ForegroundColor Red
        }
        Set-Location ..
    }

    "3" {
        Write-Host ""
        Write-Host "🎨 Desplegando Frontend..." -ForegroundColor Yellow
        Set-Location front
        flyctl deploy -a querellas-frontend
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Frontend desplegado" -ForegroundColor Green
        }
        else {
            Write-Host "❌ Error desplegando frontend" -ForegroundColor Red
        }
        Set-Location ..
    }

    "4" {
        Write-Host ""
        Write-Host "📊 Creando Base de Datos..." -ForegroundColor Yellow
        flyctl postgres create --name querellas-db --region mia --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 10

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Base de datos creada" -ForegroundColor Green
            Write-Host "⚠️  IMPORTANTE: Guarda las credenciales mostradas arriba" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Próximos pasos:" -ForegroundColor Yellow
            Write-Host "1. Ejecutar scripts SQL de inicialización" -ForegroundColor White
            Write-Host "2. Configurar DATABASE_URL en backend" -ForegroundColor White
            Write-Host ""
            Write-Host "Ver: DESPLIEGUE-FLYIO.md para más detalles" -ForegroundColor Yellow
        }
    }

    "5" {
        Write-Host ""
        Write-Host "=== Ver Logs ===" -ForegroundColor Yellow
        Write-Host "1. Backend" -ForegroundColor White
        Write-Host "2. Frontend" -ForegroundColor White
        Write-Host "3. Base de Datos" -ForegroundColor White
        Write-Host ""
        $logOption = Read-Host "Selecciona (1-3)"

        switch ($logOption) {
            "1" { flyctl logs -a querellas-backend }
            "2" { flyctl logs -a querellas-frontend }
            "3" { flyctl logs -a querellas-db }
            default { Write-Host "Opción inválida" -ForegroundColor Red }
        }
    }

    "6" {
        Write-Host ""
        Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║              ESTADO DEL SISTEMA                              ║" -ForegroundColor Cyan
        Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

        Write-Host ""
        Write-Host "📊 Base de Datos:" -ForegroundColor Yellow
        flyctl status -a querellas-db 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            flyctl status -a querellas-db
        }
        else {
            Write-Host "❌ No desplegada" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "🔧 Backend:" -ForegroundColor Yellow
        flyctl status -a querellas-backend 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            flyctl status -a querellas-backend
        }
        else {
            Write-Host "❌ No desplegado" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "🎨 Frontend:" -ForegroundColor Yellow
        flyctl status -a querellas-frontend 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            flyctl status -a querellas-frontend
        }
        else {
            Write-Host "❌ No desplegado" -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "=== URLs ===" -ForegroundColor Yellow
        Write-Host "Backend:  https://querellas-backend.fly.dev" -ForegroundColor White
        Write-Host "Swagger:  https://querellas-backend.fly.dev/swagger-ui" -ForegroundColor White
        Write-Host "Frontend: https://querellas-frontend.fly.dev" -ForegroundColor White
    }

    "7" {
        Write-Host "Saliendo..." -ForegroundColor Gray
        exit 0
    }

    default {
        Write-Host "❌ Opción inválida" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "✅ Proceso completado" -ForegroundColor Green
Write-Host ""
