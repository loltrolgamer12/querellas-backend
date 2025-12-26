-- Actualizar contraseñas de usuarios de prueba
-- Password: password123
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

DELETE FROM usuarios WHERE id IN (1, 2, 3);

INSERT INTO usuarios (id, nombre, email, telefono, password, rol, estado, creado_en, actualizado_en) VALUES
(1, 'Juan Director', 'director@querellas.com', '3001234567', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DIRECTOR', 'ACTIVO', now(), now()),
(2, 'María Auxiliar', 'auxiliar@querellas.com', '3001234568', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AUXILIAR', 'ACTIVO', now(), now()),
(3, 'Pedro Inspector', 'inspector1@querellas.com', '3001234569', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'INSPECTOR', 'ACTIVO', now(), now());
