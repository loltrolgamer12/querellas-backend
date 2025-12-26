#!/bin/bash

# ================================================================
# SCRIPT DE INICIO RÁPIDO - Sistema de Querellas
# ================================================================

echo "========================================"
echo "Sistema de Querellas - Inicio Rápido"
echo "========================================"

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar Java
echo -e "\n${YELLOW}Verificando Java...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java no encontrado. Instalar Java 17+${NC}"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}❌ Java 17+ requerido. Versión actual: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java $(java -version 2>&1 | head -n 1)${NC}"

# Verificar Node.js
echo -e "\n${YELLOW}Verificando Node.js...${NC}"
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js no encontrado. Instalar Node.js 18+${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Node.js $(node --version)${NC}"

# Verificar npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm no encontrado${NC}"
    exit 1
fi
echo -e "${GREEN}✓ npm $(npm --version)${NC}"

# Verificar PostgreSQL
echo -e "\n${YELLOW}Verificando conexión a PostgreSQL...${NC}"
if command -v psql &> /dev/null; then
    echo -e "${GREEN}✓ psql instalado${NC}"
else
    echo -e "${YELLOW}⚠️  psql no encontrado (opcional)${NC}"
fi

echo -e "\n========================================"
echo -e "${YELLOW}Opción de inicio:${NC}"
echo "1) Iniciar Backend (Puerto 8081)"
echo "2) Iniciar Frontend (Puerto 3000)"
echo "3) Iniciar ambos (Backend + Frontend)"
echo "4) Compilar Backend (crear JAR)"
echo "5) Salir"
echo "========================================"
read -p "Seleccione una opción [1-5]: " opcion

case $opcion in
    1)
        echo -e "\n${YELLOW}Iniciando Backend...${NC}"
        cd back
        if [ ! -f "target/querillas-0.0.1-SNAPSHOT.jar" ]; then
            echo -e "${YELLOW}JAR no encontrado. Compilando primero...${NC}"
            ./mvnw clean package -DskipTests
        fi
        echo -e "${GREEN}✓ Backend iniciado en http://localhost:8081${NC}"
        echo -e "${GREEN}✓ Swagger UI: http://localhost:8081/swagger-ui/index.html${NC}"
        java -jar target/querillas-0.0.1-SNAPSHOT.jar
        ;;
    2)
        echo -e "\n${YELLOW}Iniciando Frontend...${NC}"
        cd front
        if [ ! -d "node_modules" ]; then
            echo -e "${YELLOW}Instalando dependencias...${NC}"
            npm install
        fi
        echo -e "${GREEN}✓ Frontend iniciado en http://localhost:3000${NC}"
        npm run dev
        ;;
    3)
        echo -e "\n${YELLOW}Iniciando Backend + Frontend...${NC}"

        # Backend en background
        cd back
        if [ ! -f "target/querillas-0.0.1-SNAPSHOT.jar" ]; then
            echo -e "${YELLOW}Compilando Backend...${NC}"
            ./mvnw clean package -DskipTests
        fi
        echo -e "${GREEN}✓ Iniciando Backend en background...${NC}"
        java -jar target/querillas-0.0.1-SNAPSHOT.jar > ../backend.log 2>&1 &
        BACKEND_PID=$!

        # Esperar 10 segundos para que el backend inicie
        echo -e "${YELLOW}Esperando a que Backend inicie (10s)...${NC}"
        sleep 10

        # Frontend en foreground
        cd ../front
        if [ ! -d "node_modules" ]; then
            echo -e "${YELLOW}Instalando dependencias de Frontend...${NC}"
            npm install
        fi
        echo -e "${GREEN}✓ Backend: http://localhost:8081${NC}"
        echo -e "${GREEN}✓ Frontend: http://localhost:3000${NC}"
        echo -e "${GREEN}✓ Swagger: http://localhost:8081/swagger-ui/index.html${NC}"
        echo -e "\n${YELLOW}Presiona Ctrl+C para detener ambos servicios${NC}\n"

        # Trap para matar backend al salir
        trap "echo -e '\n${YELLOW}Deteniendo servicios...${NC}'; kill $BACKEND_PID 2>/dev/null; exit" INT TERM

        npm run dev
        ;;
    4)
        echo -e "\n${YELLOW}Compilando Backend...${NC}"
        cd back
        ./mvnw clean package -DskipTests
        echo -e "${GREEN}✓ JAR creado en: target/querillas-0.0.1-SNAPSHOT.jar${NC}"
        ;;
    5)
        echo -e "${GREEN}Saliendo...${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}Opción inválida${NC}"
        exit 1
        ;;
esac
