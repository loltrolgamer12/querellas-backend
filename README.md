
**Sistema de Gestión de Querellas – Backend**

Backend desarrollado con **Spring Boot** y **PostgreSQL** para la gestión de querellas y despachos comisorios de las Inspecciones de Policía de Neiva.\
Incluye autenticación con JWT, bandeja de casos, cambio de estado con flujo controlado, reportes y endpoints para dashboard.

-----
**1. Requisitos previos**

Antes de ejecutar el proyecto necesitas tener instalado:

- **Java JDK 17** (o la versión indicada en el pom.xml en maven.compiler.release)
- **Maven 3.9+**
- **PostgreSQL** (local o en contenedor)
- Un cliente REST (Insomnia / Postman) para probar los endpoints


- **TRAE AI / VS Code** para abrir el proyecto
- Insomnia con la colección querellas-insomnia.json 
-----
**2. Clonar el proyecto**

git clone https://github.com/TU-USUARIO/querellas-backend.git

cd querellas-backend

Cambia TU-USUARIO por tu usuario real de GitHub.

-----
**3. Configuración de base de datos**

1. Crear una base de datos en PostgreSQL (nombre sugerido):

CREATE DATABASE querillas\_db;

2. Restaurar el **dump/.sql** o ejecutar los scripts SQL que se entregaron con el proyecto\
   (estructura de tablas, catálogos, usuarios, etc.).
2. Configurar las credenciales en src/main/resources/application.properties\
   Ejemplo usando application.properties:

spring.application.name=querillas

server.port=8081

spring.datasource.url=jdbc:postgresql://vps-be502614.vps.ovh.ca:5432/querillas

spring.datasource.username=postgres

spring.datasource.password=kibf6f1tniqayblk

spring.jpa.hibernate.ddl-auto=validate

spring.jpa.show-sql=false

spring.jpa.properties.hibernate.format\_sql=true

spring.jackson.serialization.WRITE\_DATES\_AS\_TIMESTAMPS=false

spring.jpa.properties.hibernate.jdbc.time\_zone=UTC

spring.jpa.properties.hibernate.default\_schema=public

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

logging.level.org.springdoc=DEBUG

logging.level.org.springframework.web=DEBUG

app.jwt.secret=OFZTWEep4zgsMGWysImr+p9znKW/TXpawG+aROI0/oFLJzTUSV1Owo9oC9VmpeFK

app.jwt.expiration-minutes=120

logging.level.org.springframework.security=INFO

springdoc.swagger-ui.path=/swagger-ui

app.cors.allowed-origins=http://localhost:5173,http://localhost:3000


·  spring.application.name → Nombre lógico de la aplicación dentro del ecosistema Spring (solo informativo).

·  server.port → Puerto donde corre el backend. Ahora mismo http://localhost:8081.

·  spring.datasource.url → URL de conexión JDBC a PostgreSQL (host, puerto, base de datos).

·  spring.datasource.username → Usuario de la BD.

·  spring.datasource.password → Contraseña del usuario.

·  ddl-auto=validate → Spring **no crea ni modifica tablas**, solo valida que el modelo JPA coincida con la BD.\
(Si hay diferencias, lanza error al arrancar).

·  show-sql=false → No imprime los SQL en consola (útil para producción).

·  format\_sql=true → Si se imprimen SQL, que salgan bonitos / identados.

·  WRITE\_DATES\_AS\_TIMESTAMPS=false → Jackson serializa fechas como texto ISO (2025-11-25T10:30:00Z) y no como números.

·  hibernate.jdbc.time\_zone=UTC → Hibernate maneja las fechas en UTC hacia la BD.

·  hibernate.default\_schema=public → Usa el esquema public de PostgreSQL por defecto.

·  database-platform=PostgreSQLDialect → Dialecto específico para PostgreSQL (tipos de datos, funciones, etc)


Ajusta url, username y password según tu instalación de PostgreSQL. 

-----
**4. Compilar y ejecutar con Maven**

Desde la carpeta raíz del proyecto:

\# Compilar

mvn clean install

\# Ejecutar la aplicación

mvn spring-boot:run

Si todo está correcto, la aplicación quedará corriendo en:

http://localhost:8081

-----
**5. Autenticación (login)**

El proyecto expone un endpoint de login que devuelve un **JWT**.

**5.1. Endpoint de login**

POST /auth/login

Body de ejemplo:

{

`  `"username": "directora",

`  `"password": "demo123"

}

Otros usuarios de ejemplo (según configuración de seguridad):

- directora / demo123
- auxiliar / demo123
- inspector / demo123

La respuesta será algo como:

{

`  `"token": "eyJhbGciOiJIUzI1NiJ9....",

`  `"type": "Bearer"

}

Con ese token debes enviar el header en el resto de peticiones protegidas:

Authorization: Bearer TU\_TOKEN\_AQUI

-----
**6. Endpoints principales (resumen rápido)**

Todos los endpoints (salvo /auth/login) requieren el header Authorization: Bearer ....

**Catálogos**

- GET /api/catalogos/naturalezas
- GET /api/catalogos/inspecciones
- GET /api/catalogos/comunas
- GET /api/catalogos/temas
- GET /api/catalogos/estados?modulo=QUERELLA

**Core de querellas**

- GET /api/querellas\
  Bandeja de casos con filtros y paginación.
- GET /api/querellas/{id}\
  Detalle del caso.
- GET /api/querellas/{id}/historial-estados\
  Historial de cambios de estado.
- PUT /api/querellas/{id}/estado\
  Cambiar estado (valida flujo permitido).
- PUT /api/querellas/{id}/inspeccion\
  Asignar / reasignar inspección.
- POST /api/querellas\
  Crear una nueva querella.
- GET /api/querellas/{id}/posibles-duplicados\
  Lista de casos similares (detección de posibles duplicados).

**Reportes**

- GET /api/reportes/querellas-trimestral?desde=YYYY-MM-DD&hasta=YYYY-MM-DD\
  Reporte detallado para informes trimestrales.
- GET /api/reportes/dashboard-querellas?desde=YYYY-MM-DD&hasta=YYYY-MM-DD\
  Datos agregados para dashboard: total, por estado, por inspección, por naturaleza.
-----
**7. Uso de la colección de Insomnia (opcional)**

1. Abrir Insomnia → **Application → Import → From File**.
1. Seleccionar docs/insomnia/querellas-insomnia.json.
1. En **Base Environment** configurar:

{

`  `"base\_url": "http://localhost:8081",

`  `"token": ""

}

4. Ejecutar primero Auth / Login, copiar el token de la respuesta y pegarlo en token.
4. Probar el resto de endpoints (bandeja, detalles, cambio de estado, reportes, etc.).

