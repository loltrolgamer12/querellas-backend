# ✅ CHECKLIST DE DESPLIEGUE - Sistema de Querellas

## Pre-Despliegue (Desarrollo)

### Verificación del Sistema

- [ ] Backend compila correctamente
  ```bash
  cd querellas-backend
  ./mvnw clean package -DskipTests
  ```

- [ ] Frontend compila correctamente
  ```bash
  cd Frontend
  npm install
  npm run build
  ```

- [ ] Backend inicia sin errores
  ```bash
  cd querellas-backend
  ./mvnw spring-boot:run
  # Verificar: http://localhost:8081/actuator/health
  ```

- [ ] Frontend inicia sin errores
  ```bash
  cd Frontend
  npm run dev
  # Verificar: http://localhost:3000
  ```

- [ ] Login funciona con usuarios de prueba
  - [ ] director@querellas.com / password123
  - [ ] auxiliar@querellas.com / password123
  - [ ] inspector1@querellas.com / password123

- [ ] Swagger UI accesible
  - URL: http://localhost:8081/swagger-ui/index.html

---

## Base de Datos

### Optimización

- [ ] Script de índices ejecutado
  ```bash
  psql -h vps-be502614.vps.ovh.ca -U postgres -d querillas -f indices-produccion.sql
  ```

- [ ] Verificar índices creados
  ```sql
  SELECT tablename, indexname FROM pg_indexes WHERE schemaname = 'public';
  -- Debe mostrar ~28 índices
  ```

- [ ] Verificar tablas principales existen
  ```sql
  \dt
  -- Debe listar: usuarios, querellas, despachos_comisorios, estado, tema, comuna, etc.
  ```

### Usuario Administrador

- [ ] Usuario administrador creado en producción
  ```sql
  SELECT * FROM usuarios WHERE rol = 'DIRECTOR';
  ```

- [ ] Contraseña del administrador es segura (NO password123)

- [ ] Usuarios de prueba eliminados o desactivados (producción)
  ```sql
  -- Solo en producción
  DELETE FROM usuarios WHERE email LIKE '%@querellas.com';
  ```

---

## Configuración Backend

### application.properties

- [ ] Base de datos configurada correctamente
  ```properties
  spring.datasource.url=jdbc:postgresql://...
  spring.datasource.username=postgres
  spring.datasource.password=***
  ```

- [ ] HikariCP configurado
  ```properties
  spring.datasource.hikari.maximum-pool-size=20
  spring.datasource.hikari.minimum-idle=5
  ```

- [ ] JWT secret es seguro (64+ caracteres)
  ```properties
  app.jwt.secret=SECRETO_LARGO_Y_ALEATORIO
  ```

- [ ] CORS configurado para producción
  ```properties
  # Desarrollo
  app.cors.allowed-origins=http://localhost:3000

  # Producción
  app.cors.allowed-origins=https://tudominio.com
  ```

- [ ] Logging configurado apropiadamente
  ```properties
  logging.level.root=INFO
  logging.level.org.hibernate.SQL=WARN
  ```

- [ ] Perfil correcto activado
  ```properties
  # Desarrollo: sin configuración adicional (usa "dev")
  # Producción: agregar
  spring.profiles.active=prod
  ```

---

## Configuración Frontend

### Variables de Entorno

- [ ] Archivo .env.local configurado
  ```env
  NEXT_PUBLIC_API_URL=http://localhost:8081
  ```

- [ ] Para producción: .env.production creado
  ```env
  NEXT_PUBLIC_API_URL=https://api.tudominio.com
  ```

- [ ] Frontend puede conectarse al backend
  - Verificar en consola del navegador (F12)
  - No debe haber errores de CORS

---

## Seguridad

### Producción

- [ ] JWT secret cambiado (no usar el de desarrollo)

- [ ] Contraseñas de base de datos seguras

- [ ] HTTPS configurado (Let's Encrypt + Nginx)
  ```bash
  sudo certbot --nginx -d tudominio.com -d api.tudominio.com
  ```

- [ ] Firewall configurado
  ```bash
  # Solo puertos necesarios abiertos
  # 80 (HTTP), 443 (HTTPS), 5432 (PostgreSQL solo desde localhost)
  sudo ufw status
  ```

- [ ] CORS con dominios específicos (no usar *)

- [ ] Usuarios de prueba eliminados

- [ ] Credenciales NO están en el código (usar variables de entorno)

---

## Infraestructura

### Servidor

- [ ] Java 17+ instalado
  ```bash
  java -version
  ```

- [ ] Node.js 18+ instalado
  ```bash
  node --version
  ```

- [ ] PostgreSQL 15+ corriendo
  ```bash
  psql --version
  ```

- [ ] Nginx instalado (para reverse proxy)
  ```bash
  nginx -v
  ```

### Servicios Systemd (Opcional)

- [ ] Servicio backend creado
  ```bash
  sudo systemctl status querellas-backend
  ```

- [ ] Servicio frontend creado
  ```bash
  sudo systemctl status querellas-frontend
  ```

- [ ] Servicios configurados para auto-inicio
  ```bash
  sudo systemctl enable querellas-backend
  sudo systemctl enable querellas-frontend
  ```

### Nginx

- [ ] Configuración de reverse proxy creada
  - /etc/nginx/sites-available/querellas

- [ ] Configuración activada
  ```bash
  sudo ln -s /etc/nginx/sites-available/querellas /etc/nginx/sites-enabled/
  sudo nginx -t
  sudo systemctl reload nginx
  ```

- [ ] SSL/TLS configurado correctamente
  ```bash
  sudo certbot certificates
  ```

---

## Backup y Recuperación

### Backup

- [ ] Script de backup automático configurado
  ```bash
  # Cron job para backup diario
  crontab -e
  # 0 2 * * * pg_dump ... > backup_$(date +\%Y\%m\%d).sql
  ```

- [ ] Backup manual funciona
  ```bash
  pg_dump -h HOST -U postgres querillas > backup_test.sql
  ```

- [ ] Restauración probada
  ```bash
  psql -h HOST -U postgres -d querillas < backup_test.sql
  ```

- [ ] Backups se guardan fuera del servidor

---

## Monitoreo

### Logs

- [ ] Logs de backend accesibles
  ```bash
  tail -f querellas-backend/logs/application.log
  ```

- [ ] Logs de Nginx accesibles
  ```bash
  tail -f /var/log/nginx/access.log
  tail -f /var/log/nginx/error.log
  ```

- [ ] Log rotation configurado
  ```bash
  cat /etc/logrotate.d/querellas
  ```

### Salud del Sistema

- [ ] Endpoint de health check funciona
  ```bash
  curl http://localhost:8081/actuator/health
  # Debe retornar: {"status":"UP"}
  ```

- [ ] Métricas disponibles (opcional)
  ```bash
  curl http://localhost:8081/actuator/metrics
  ```

---

## Pruebas Post-Despliegue

### Funcionalidad Básica

- [ ] Login funciona
  - Probar con usuario administrador

- [ ] Crear querella funciona
  - Crear una querella de prueba

- [ ] Listar querellas funciona
  - Ver lista de querellas
  - Aplicar filtros

- [ ] Asignar inspector funciona
  - Asignación manual
  - Asignación automática (Round-Robin)

- [ ] Cambiar estado funciona
  - Cambiar estado de querella de prueba

- [ ] Subir adjuntos funciona
  - Subir archivo de prueba
  - Descargar archivo

- [ ] Crear despacho funciona
  - Crear despacho comisorio de prueba

- [ ] Reportes funcionan
  - Ver dashboard
  - Generar reporte trimestral
  - Exportar a Excel

### Rendimiento

- [ ] Listado de querellas carga en < 1 segundo
  - Con 100+ querellas

- [ ] Búsqueda por texto funciona rápidamente
  - Con índices aplicados

- [ ] Dashboard carga en < 2 segundos

### Seguridad

- [ ] No se puede acceder sin autenticación
  ```bash
  curl http://localhost:8081/api/querellas
  # Debe retornar 401 o 403
  ```

- [ ] CORS funciona correctamente
  - Frontend puede hacer peticiones al backend
  - Otros dominios no pueden

- [ ] Tokens JWT expiran correctamente
  - Esperar 2 horas
  - Verificar que pide login nuevamente

---

## Documentación

### Para Usuarios

- [ ] INSTRUCCIONES-USO.md revisado
- [ ] Usuarios finales tienen acceso a documentación
- [ ] Capacitación realizada (opcional)

### Para Administradores

- [ ] README.md actualizado con información de producción
- [ ] RESUMEN-DESPLIEGUE-PRODUCCION.md revisado
- [ ] Credenciales de acceso documentadas (de forma segura)

---

## Rollback Plan

### En caso de problemas

- [ ] Backup de base de datos anterior al despliegue
- [ ] Versión anterior del código disponible
- [ ] Procedimiento de rollback documentado

```bash
# Restaurar base de datos
psql -h HOST -U postgres -d querillas < backup_anterior.sql

# Volver a versión anterior
git checkout VERSION_ANTERIOR
./mvnw clean package
# Reiniciar servicio
```

---

## ✅ LISTO PARA PRODUCCIÓN

Una vez completados todos los checkmarks:

### Desarrollo ✅
- [ ] Todos los checks de "Pre-Despliegue" completados
- [ ] Todos los checks de "Base de Datos" completados
- [ ] Todos los checks de "Configuración" completados

### Producción ✅
- [ ] Todos los checks de "Seguridad" completados
- [ ] Todos los checks de "Infraestructura" completados
- [ ] Todos los checks de "Backup y Recuperación" completados
- [ ] Todos los checks de "Monitoreo" completados
- [ ] Todos los checks de "Pruebas Post-Despliegue" completados
- [ ] Todos los checks de "Documentación" completados
- [ ] Rollback plan documentado

---

## 🚀 Comando de Despliegue Final

```bash
# 1. Compilar backend
cd querellas-backend
./mvnw clean package -DskipTests

# 2. Compilar frontend
cd ../Frontend
npm run build

# 3. Iniciar backend
java -jar querellas-backend/target/querillas-0.0.1-SNAPSHOT.jar &

# 4. Iniciar frontend
cd Frontend
npm start &

# 5. Verificar
curl http://localhost:8081/actuator/health
curl http://localhost:3000

# 6. Abrir navegador
# http://localhost:3000
```

---

**Fecha de Checklist**: 26 de Diciembre de 2025
**Versión**: 1.0.0
**Estado**: ✅ LISTO PARA USAR
