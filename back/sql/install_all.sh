#!/bin/bash

# =====================================================
# SCRIPT DE INSTALACIÓN AUTOMATIZADA
# Sistema de Querellas - Alcaldía de Neiva
# =====================================================

set -e  # Salir en caso de error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "INSTALACIÓN AUTOMÁTICA - BASE DE DATOS"
echo "Sistema de Querellas - Alcaldía de Neiva"
echo "========================================"
echo ""

# =====================================================
# CONFIGURACIÓN
# =====================================================

# Solicitar configuración
read -p "Host de PostgreSQL [localhost]: " PG_HOST
PG_HOST=${PG_HOST:-localhost}

read -p "Puerto de PostgreSQL [5432]: " PG_PORT
PG_PORT=${PG_PORT:-5432}

read -p "Usuario administrador [postgres]: " PG_ADMIN_USER
PG_ADMIN_USER=${PG_ADMIN_USER:-postgres}

read -sp "Contraseña de $PG_ADMIN_USER: " PG_ADMIN_PASS
echo ""

read -p "Nombre de la base de datos [querillas_db]: " DB_NAME
DB_NAME=${DB_NAME:-querillas_db}

echo ""
read -p "¿Crear usuario de aplicación? (s/n) [s]: " CREATE_APP_USER
CREATE_APP_USER=${CREATE_APP_USER:-s}

if [[ "$CREATE_APP_USER" == "s" ]]; then
    read -p "Nombre de usuario de aplicación [querillas_app]: " APP_USER
    APP_USER=${APP_USER:-querillas_app}

    read -sp "Contraseña para $APP_USER: " APP_PASS
    echo ""
fi

echo ""
echo -e "${YELLOW}Configuración:${NC}"
echo "  Host: $PG_HOST"
echo "  Puerto: $PG_PORT"
echo "  Base de datos: $DB_NAME"
echo "  Usuario admin: $PG_ADMIN_USER"
if [[ "$CREATE_APP_USER" == "s" ]]; then
    echo "  Usuario aplicación: $APP_USER"
fi
echo ""

read -p "¿Continuar con la instalación? (s/n): " CONFIRM
if [[ "$CONFIRM" != "s" ]]; then
    echo "Instalación cancelada."
    exit 0
fi

echo ""

# Exportar contraseña para psql
export PGPASSWORD="$PG_ADMIN_PASS"

# =====================================================
# PASO 1: VERIFICAR CONEXIÓN
# =====================================================

echo -e "${YELLOW}[1/7] Verificando conexión a PostgreSQL...${NC}"
if psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "\q" 2>/dev/null; then
    echo -e "${GREEN}✓ Conexión exitosa${NC}"
else
    echo -e "${RED}✗ Error de conexión a PostgreSQL${NC}"
    echo "Verifique host, puerto, usuario y contraseña"
    exit 1
fi
echo ""

# =====================================================
# PASO 2: CREAR BASE DE DATOS
# =====================================================

echo -e "${YELLOW}[2/7] Creando base de datos $DB_NAME...${NC}"

# Verificar si ya existe
DB_EXISTS=$(psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'")

if [[ "$DB_EXISTS" == "1" ]]; then
    echo -e "${YELLOW}⚠ La base de datos $DB_NAME ya existe${NC}"
    read -p "¿Eliminar y recrear? (s/n): " DROP_DB
    if [[ "$DROP_DB" == "s" ]]; then
        echo "Eliminando base de datos existente..."
        psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "DROP DATABASE $DB_NAME;"
        echo "Creando nueva base de datos..."
        psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "CREATE DATABASE $DB_NAME WITH ENCODING='UTF8';"
        echo -e "${GREEN}✓ Base de datos recreada${NC}"
    else
        echo "Usando base de datos existente"
    fi
else
    psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "CREATE DATABASE $DB_NAME WITH ENCODING='UTF8';"
    echo -e "${GREEN}✓ Base de datos creada${NC}"
fi
echo ""

# =====================================================
# PASO 3: CREAR USUARIO DE APLICACIÓN (OPCIONAL)
# =====================================================

if [[ "$CREATE_APP_USER" == "s" ]]; then
    echo -e "${YELLOW}[3/7] Creando usuario de aplicación $APP_USER...${NC}"

    USER_EXISTS=$(psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$APP_USER'")

    if [[ "$USER_EXISTS" == "1" ]]; then
        echo -e "${YELLOW}⚠ El usuario $APP_USER ya existe${NC}"
        read -p "¿Cambiar contraseña? (s/n): " CHANGE_PASS
        if [[ "$CHANGE_PASS" == "s" ]]; then
            psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "ALTER USER $APP_USER WITH PASSWORD '$APP_PASS';"
            echo -e "${GREEN}✓ Contraseña actualizada${NC}"
        fi
    else
        psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "CREATE USER $APP_USER WITH PASSWORD '$APP_PASS';"
        echo -e "${GREEN}✓ Usuario creado${NC}"
    fi

    psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $APP_USER;"
    echo -e "${GREEN}✓ Permisos otorgados${NC}"
else
    echo -e "${YELLOW}[3/7] Omitiendo creación de usuario de aplicación${NC}"
fi
echo ""

# =====================================================
# PASO 4: EJECUTAR SCHEMA.SQL
# =====================================================

echo -e "${YELLOW}[4/7] Ejecutando schema.sql...${NC}"
if psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d "$DB_NAME" -f schema.sql > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Esquema creado exitosamente${NC}"
else
    echo -e "${RED}✗ Error al crear esquema${NC}"
    exit 1
fi
echo ""

# =====================================================
# PASO 5: EJECUTAR DATA.SQL
# =====================================================

echo -e "${YELLOW}[5/7] Ejecutando data.sql...${NC}"
if psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d "$DB_NAME" -f data.sql > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Datos iniciales cargados${NC}"
else
    echo -e "${RED}✗ Error al cargar datos${NC}"
    exit 1
fi
echo ""

# =====================================================
# PASO 6: CONFIGURAR PERMISOS (SI SE CREÓ USUARIO APP)
# =====================================================

if [[ "$CREATE_APP_USER" == "s" ]]; then
    echo -e "${YELLOW}[6/7] Configurando permisos para $APP_USER...${NC}"

    psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d "$DB_NAME" <<EOF > /dev/null 2>&1
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $APP_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $APP_USER;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO $APP_USER;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO $APP_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO $APP_USER;
EOF

    echo -e "${GREEN}✓ Permisos configurados${NC}"
else
    echo -e "${YELLOW}[6/7] Omitiendo configuración de permisos${NC}"
fi
echo ""

# =====================================================
# PASO 7: VERIFICAR INSTALACIÓN
# =====================================================

echo -e "${YELLOW}[7/7] Verificando instalación...${NC}"
if psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN_USER" -d "$DB_NAME" -f verify_installation.sql > /tmp/verify_output.log 2>&1; then

    # Buscar mensaje de éxito
    if grep -q "INSTALACIÓN EXITOSA" /tmp/verify_output.log; then
        echo -e "${GREEN}✓ Verificación exitosa${NC}"
    else
        echo -e "${YELLOW}⚠ Verificación completada con advertencias${NC}"
        echo "Ver detalles en /tmp/verify_output.log"
    fi
else
    echo -e "${RED}✗ Error en verificación${NC}"
    exit 1
fi
echo ""

# =====================================================
# RESUMEN FINAL
# =====================================================

echo "========================================"
echo -e "${GREEN}INSTALACIÓN COMPLETADA EXITOSAMENTE${NC}"
echo "========================================"
echo ""
echo "Resumen de instalación:"
echo "  Base de datos: $DB_NAME"
echo "  Host: $PG_HOST:$PG_PORT"
if [[ "$CREATE_APP_USER" == "s" ]]; then
    echo "  Usuario aplicación: $APP_USER"
fi
echo ""
echo "Credenciales de acceso al sistema:"
echo "  Email: directora@inspecciones.neiva.gov.co"
echo "  Contraseña: demo123"
echo ""
echo -e "${RED}⚠ IMPORTANTE: Cambiar contraseñas en producción${NC}"
echo ""
echo "Configuración para application.properties:"
echo "  spring.datasource.url=jdbc:postgresql://$PG_HOST:$PG_PORT/$DB_NAME"
if [[ "$CREATE_APP_USER" == "s" ]]; then
    echo "  spring.datasource.username=$APP_USER"
    echo "  spring.datasource.password=<contraseña configurada>"
fi
echo ""
echo "Para más información, consulte README_DATABASE.md"
echo ""

# Limpiar contraseña
unset PGPASSWORD

exit 0
