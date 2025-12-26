-- ================================================
-- SCRIPT DE DATOS DE DESARROLLO Y PRUEBAS
-- ================================================

-- Estados (solo tiene: id, modulo, nombre, creado_en)
INSERT INTO estado (id, modulo, nombre, creado_en) VALUES
(1, 'QUERELLA', 'RECIBIDA', now()),
(2, 'QUERELLA', 'ASIGNADA', now()),
(3, 'QUERELLA', 'EN_INVESTIGACION', now()),
(4, 'QUERELLA', 'RESUELTA', now()),
(5, 'QUERELLA', 'CERRADA', now()),
(6, 'DESPACHO', 'PENDIENTE', now()),
(7, 'DESPACHO', 'EN_PROCESO', now()),
(8, 'DESPACHO', 'FINALIZADO', now())
ON CONFLICT DO NOTHING;

-- Temas (solo tiene: id, nombre)
INSERT INTO tema (id, nombre) VALUES
(1, 'Ruido Excesivo'),
(2, 'Contaminación Ambiental'),
(3, 'Ocupación Espacio Público'),
(4, 'Problemas de Convivencia'),
(5, 'Animales Domésticos')
ON CONFLICT DO NOTHING;

-- Comunas (solo tiene: id, nombre)
INSERT INTO comuna (id, nombre) VALUES
(1, 'Comuna 1 - Centro'),
(2, 'Comuna 2 - Norte'),
(3, 'Comuna 3 - Sur')
ON CONFLICT DO NOTHING;

-- Usuarios (password: password123)
-- Campos: id, nombre, email, telefono, password, rol, estado, zona, creado_en, actualizado_en
-- Hash BCrypt para "password123" generado con rounds=10
INSERT INTO usuarios (id, nombre, email, telefono, password, rol, estado, creado_en, actualizado_en) VALUES
(1, 'Juan Director', 'director@querellas.com', '3001234567', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DIRECTOR', 'ACTIVO', now(), now()),
(2, 'María Auxiliar', 'auxiliar@querellas.com', '3001234568', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AUXILIAR', 'ACTIVO', now(), now()),
(3, 'Pedro Inspector', 'inspector1@querellas.com', '3001234569', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'INSPECTOR', 'ACTIVO', now(), now())
ON CONFLICT DO NOTHING;
