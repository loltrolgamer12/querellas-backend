#!/bin/bash

# ================================================================
# Script de Despliegue Rápido en Fly.io
# Sistema Completo de Gestión de Querellas
# ================================================================

set -e

echo "🚀 Iniciando despliegue en Fly.io..."

# ================================================================
# Funciones auxiliares
# ================================================================

check_flyctl() {
    if ! command -v flyctl &> /dev/null; then
        echo "❌ flyctl no está instalado"
        echo "Instalar desde: https://fly.io/docs/hands-on/install-flyctl/"
        exit 1
    fi
    echo "✅ flyctl instalado"
}

check_auth() {
    if ! flyctl auth whoami &> /dev/null; then
        echo "❌ No estás autenticado en Fly.io"
        echo "Ejecuta: flyctl auth login"
        exit 1
    fi
    echo "✅ Autenticado en Fly.io"
}

# ================================================================
# Verificaciones iniciales
# ================================================================

echo ""
echo "=== Verificando requisitos ==="
check_flyctl
check_auth

# ================================================================
# Menú de opciones
# ================================================================

echo ""
echo "=== Opciones de Despliegue ==="
echo "1. Despliegue completo (BD + Backend + Frontend)"
echo "2. Solo Backend"
echo "3. Solo Frontend"
echo "4. Solo crear Base de Datos"
echo "5. Ver logs"
echo "6. Verificar estado"
echo ""
read -p "Selecciona una opción (1-6): " option

case $option in
    1)
        echo ""
        echo "=== Despliegue Completo ==="

        # Base de datos
        echo ""
        echo "📊 Paso 1/3: Verificando Base de Datos..."
        if flyctl status -a querellas-db &> /dev/null; then
            echo "✅ Base de datos ya existe"
        else
            echo "⚠️  Base de datos no existe. Créala con:"
            echo "flyctl postgres create --name querellas-db --region mia --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 10"
            exit 1
        fi

        # Backend
        echo ""
        echo "🔧 Paso 2/3: Desplegando Backend..."
        cd back
        flyctl deploy -a querellas-backend
        echo "✅ Backend desplegado"
        cd ..

        # Frontend
        echo ""
        echo "🎨 Paso 3/3: Desplegando Frontend..."
        cd front
        flyctl deploy -a querellas-frontend
        echo "✅ Frontend desplegado"
        cd ..

        echo ""
        echo "✅ Despliegue completo exitoso!"
        ;;

    2)
        echo ""
        echo "🔧 Desplegando Backend..."
        cd back
        flyctl deploy -a querellas-backend
        echo "✅ Backend desplegado"
        ;;

    3)
        echo ""
        echo "🎨 Desplegando Frontend..."
        cd front
        flyctl deploy -a querellas-frontend
        echo "✅ Frontend desplegado"
        ;;

    4)
        echo ""
        echo "📊 Creando Base de Datos..."
        flyctl postgres create \
          --name querellas-db \
          --region mia \
          --initial-cluster-size 1 \
          --vm-size shared-cpu-1x \
          --volume-size 10
        echo ""
        echo "✅ Base de datos creada"
        echo "⚠️  IMPORTANTE: Guarda las credenciales mostradas arriba"
        echo ""
        echo "Próximos pasos:"
        echo "1. Ejecutar scripts SQL de inicialización (ver DESPLIEGUE-FLYIO.md)"
        echo "2. Configurar DATABASE_URL en backend"
        ;;

    5)
        echo ""
        echo "=== Ver Logs ==="
        echo "1. Backend"
        echo "2. Frontend"
        echo "3. Base de Datos"
        echo ""
        read -p "Selecciona (1-3): " log_option

        case $log_option in
            1) flyctl logs -a querellas-backend ;;
            2) flyctl logs -a querellas-frontend ;;
            3) flyctl logs -a querellas-db ;;
            *) echo "Opción inválida" ;;
        esac
        ;;

    6)
        echo ""
        echo "=== Estado del Sistema ==="

        echo ""
        echo "📊 Base de Datos:"
        flyctl status -a querellas-db || echo "❌ No desplegada"

        echo ""
        echo "🔧 Backend:"
        flyctl status -a querellas-backend || echo "❌ No desplegado"

        echo ""
        echo "🎨 Frontend:"
        flyctl status -a querellas-frontend || echo "❌ No desplegado"

        echo ""
        echo "=== URLs ==="
        echo "Backend:  https://querellas-backend.fly.dev"
        echo "Swagger:  https://querellas-backend.fly.dev/swagger-ui/index.html"
        echo "Frontend: https://querellas-frontend.fly.dev"
        ;;

    *)
        echo "❌ Opción inválida"
        exit 1
        ;;
esac

echo ""
echo "✅ Proceso completado"
