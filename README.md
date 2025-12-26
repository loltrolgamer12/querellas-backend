# Sistema Completo de Gestión de Querellas

Sistema integral de gestión de querellas y despachos comisorios para entidades gubernamentales.

## 📋 Descripción

Sistema completo que permite gestionar querellas ciudadanas, asignar inspectores de forma automática mediante algoritmo Round-Robin, gestionar despachos comisorios, y generar reportes estadísticos con exportación a Excel.

## 🏗️ Estructura del Proyecto

```
sistema-completo-gestion-querellas/
├── back/                           # Backend - API REST (Spring Boot)
│   ├── src/                        # Código fuente
│   ├── pom.xml                     # Dependencias Maven
│   └── README.md                   # Documentación del backend
│
├── front/                          # Frontend - Interfaz Web (Next.js)
│   ├── app/                        # Páginas (App Router)
│   ├── components/                 # Componentes React
│   ├── lib/                        # Utilidades y API client
│   ├── package.json                # Dependencias npm
│   └── README.md                   # Documentación del frontend
│
├── database/                       # Scripts de Base de Datos
│   ├── schema.sql                  # Esquema completo de la BD
│   ├── datos_iniciales.sql         # Datos iniciales (catálogos)
│   ├── indices-produccion.sql      # Índices para optimización
│   ├── install_all.sh              # Script de instalación completo
│   └── README_DATABASE.md          # Documentación de la BD
│
├── README.md                       # Este archivo
├── INSTRUCCIONES-USO.md           # Manual de usuario
├── RESUMEN-DESPLIEGUE-PRODUCCION.md # Guía de despliegue
└── iniciar-sistema.sh             # Script de inicio rápido
```

## 🚀 Tecnologías

### Backend
- **Java 17+**
- **Spring Boot 3.5.6**
- **Spring Security** (JWT)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 15.14**
- **Maven**
- **Swagger/OpenAPI**

### Frontend
- **Node.js 18+**
- **Next.js 14** (React 18)
- **TypeScript**
- **Tailwind CSS**
- **API REST Client**

### Base de Datos
- **PostgreSQL 15.14**
- 12 tablas relacionadas
- 28 índices optimizados
- Auditoría completa
- Soporte para 10,000+ registros

## ⚡ Inicio Rápido

### Requisitos Previos
- Java 17 o superior
- Node.js 18 o superior
- PostgreSQL 15 o superior
- Maven 3.8+

### 1. Configurar Base de Datos

```bash
# Conectarse a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE querillas;

# Ejecutar scripts de instalación
cd database
./install_all.sh
```

O manualmente:
```bash
psql -U postgres -d querillas -f database/schema.sql
psql -U postgres -d querillas -f database/datos_iniciales.sql
psql -U postgres -d querillas -f database/indices-produccion.sql
```

### 2. Configurar Backend

```bash
cd back

# Editar application.properties con tus credenciales de BD
nano src/main/resources/application.properties

# Compilar
./mvnw clean package -DskipTests

# Ejecutar
./mvnw spring-boot:run
```

Backend corriendo en: **http://localhost:8081**

### 3. Configurar Frontend

```bash
cd front

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env.local
# Editar .env.local si es necesario

# Modo desarrollo
npm run dev

# Modo producción
npm run build
npm start
```

Frontend corriendo en: **http://localhost:3000**

### 4. Acceso al Sistema

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html

## 🔐 Usuarios de Desarrollo

Solo en modo desarrollo (`@Profile("dev")`):

| Email | Contraseña | Rol |
|-------|------------|-----|
| director@querellas.com | password123 | DIRECTOR |
| auxiliar@querellas.com | password123 | AUXILIAR |
| inspector1@querellas.com | password123 | INSPECTOR |

⚠️ **En producción, crear usuario administrador manualmente en la base de datos.**

## 🎯 Funcionalidades Principales

### Gestión de Querellas
- ✅ CRUD completo de querellas
- ✅ Asignación manual de inspectores
- ✅ **Asignación automática Round-Robin**
- ✅ Filtros avanzados (texto, tema, comuna, estado, inspector, fechas)
- ✅ Paginación y ordenamiento
- ✅ Detección de posibles duplicados
- ✅ Historial completo de cambios

### Gestión de Despachos Comisorios
- ✅ CRUD de despachos
- ✅ Asignación de inspectores
- ✅ Control de despachos pendientes/devueltos
- ✅ Reportes específicos

### Sistema de Adjuntos
- ✅ Upload de archivos
- ✅ Download de archivos
- ✅ Gestión por querella
- ✅ Soporte múltiples formatos (PDF, imágenes, documentos)

### Comunicaciones
- ✅ Registro de comunicaciones por querella
- ✅ Tipos: Llamada, Email, Visita, Oficio
- ✅ Estados: Borrador, Enviada, Recibida
- ✅ Historial completo

### Reportes y Estadísticas
- ✅ Dashboard con métricas en tiempo real
- ✅ Reportes trimestrales
- ✅ Filtros por fecha e inspector
- ✅ **Exportación a Excel**
- ✅ Estadísticas por tema, comuna, estado

### Seguridad
- ✅ Autenticación JWT
- ✅ Control de acceso basado en roles (RBAC)
- ✅ Passwords con BCrypt
- ✅ CORS configurado
- ✅ Validación de datos

## 👥 Roles y Permisos

### DIRECTOR (Acceso Completo)
- Gestión completa de querellas
- Asignación de inspectores
- Gestión de usuarios
- Administración de catálogos
- Todos los reportes

### AUXILIAR
- Crear y gestionar querellas
- Gestionar despachos
- Ver reportes
- Exportar a Excel

### INSPECTOR
- Ver querellas asignadas
- Actualizar estados de sus querellas
- Agregar comunicaciones y adjuntos
- Ver sus despachos asignados

## 📊 Rendimiento

- ✅ Soporta 10,000+ querellas
- ✅ 50,000+ adjuntos
- ✅ 100,000+ registros de historial
- ✅ 100-200 usuarios concurrentes
- ✅ Listado de querellas: 50-200ms (con índices)
- ✅ Búsqueda de texto: 100-500ms
- ✅ Dashboard: 500ms-2s

## 📚 Documentación

- **[INSTRUCCIONES-USO.md](INSTRUCCIONES-USO.md)** - Manual completo de usuario
- **[RESUMEN-DESPLIEGUE-PRODUCCION.md](RESUMEN-DESPLIEGUE-PRODUCCION.md)** - Guía de despliegue
- **[CHECKLIST-DESPLIEGUE.md](CHECKLIST-DESPLIEGUE.md)** - Checklist pre-producción
- **[back/README.md](back/README.md)** - Documentación del backend
- **[front/README.md](front/README.md)** - Documentación del frontend
- **[database/README_DATABASE.md](database/README_DATABASE.md)** - Documentación de BD
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html

## 🔧 Configuración de Producción

### Backend

Editar `back/src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://HOST:5432/querillas
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}

# CORS
app.cors.allowed-origins=https://tudominio.com
```

### Frontend

Crear `front/.env.production`:

```env
NEXT_PUBLIC_API_URL=https://api.tudominio.com
```

### Base de Datos

1. Aplicar esquema: `psql ... -f database/schema.sql`
2. Cargar datos iniciales: `psql ... -f database/datos_iniciales.sql`
3. Aplicar índices: `psql ... -f database/indices-produccion.sql`
4. Crear usuario administrador manualmente

## 🚢 Despliegue

Ver [RESUMEN-DESPLIEGUE-PRODUCCION.md](RESUMEN-DESPLIEGUE-PRODUCCION.md) para instrucciones detalladas de despliegue en:
- VPS con Systemd
- Docker + Docker Compose
- Nginx como reverse proxy
- SSL con Let's Encrypt

## 🛠️ Desarrollo

### Backend

```bash
cd back
./mvnw spring-boot:run
```

### Frontend

```bash
cd front
npm run dev
```

### Ejecutar Tests

```bash
cd back
./mvnw test
```

## 📈 Arquitectura

```
Frontend (Next.js)
      ↓ HTTP/REST
Backend (Spring Boot)
      ↓ JDBC
PostgreSQL Database
```

### Capas del Backend

```
Controllers (REST API)
      ↓
Services (Lógica de negocio)
      ↓
Repositories (Acceso a datos)
      ↓
Entities (JPA/Hibernate)
```

## 🤝 Contribuir

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto es privado y está protegido por derechos de autor.

## 📞 Soporte

Para problemas o preguntas:
- Abrir un issue en GitHub
- Revisar documentación en `/docs`
- Consultar Swagger UI para API

## 🎉 Características Destacadas

### ⭐ Asignación Automática Round-Robin
Distribuye equitativamente las querellas entre inspectores activos, garantizando carga balanceada.

### ⭐ Auditoría Completa
Registro detallado de todos los cambios con timestamps, usuarios y motivos.

### ⭐ Optimización para Alto Volumen
Base de datos optimizada con 28 índices para manejar miles de registros eficientemente.

### ⭐ Exportación a Excel
Todos los reportes se pueden exportar a formato Excel para análisis externo.

### ⭐ Búsqueda Full-Text
Búsqueda rápida en descripciones y direcciones con soporte para español.

---

**Versión**: 1.0.0
**Última actualización**: Diciembre 2025
**Estado**: ✅ Producción Ready
