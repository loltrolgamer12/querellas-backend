-- ========================================
-- CATÁLOGOS PARA LA CIUDAD DE NEIVA
-- ========================================

-- COMUNAS DE NEIVA
INSERT INTO comuna (nombre, creado_en, actualizado_en) VALUES
('Comuna 1 - Norte', now(), now()),
('Comuna 2 - Oriente', now(), now()),
('Comuna 3 - Centro', now(), now()),
('Comuna 4 - Occidental', now(), now()),
('Comuna 5 - Suroriental', now(), now()),
('Comuna 6 - Noroccidental', now(), now()),
('Comuna 7 - Suroccidental', now(), now()),
('Comuna 8 - Nororiental', now(), now()),
('Comuna 9 - Oriental', now(), now()),
('Comuna 10 - Sur', now(), now())
ON CONFLICT DO NOTHING;

-- CORREGIMIENTOS DE NEIVA
INSERT INTO corregimiento (nombre, creado_en, actualizado_en) VALUES
('Caguán', now(), now()),
('Chapinero', now(), now()),
('Fortalecillas', now(), now()),
('Guacirco', now(), now()),
('Órganos', now(), now()),
('Río Loro', now(), now()),
('San Antonio de Anaconia', now(), now()),
('San Luis', now(), now()),
('Santa Helena del Opón', now(), now()),
('Vegalarga', now(), now()),
('El Venado', now(), now())
ON CONFLICT DO NOTHING;

-- BARRIOS POR COMUNA

-- Comuna 1 - Norte
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Altico', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Calixto Leiva', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Las Palmas', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Los Olivos', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Provivienda', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Santa Lucía', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte'
UNION ALL SELECT 'Siete de Agosto', id, now(), now() FROM comuna WHERE nombre = 'Comuna 1 - Norte';

-- Comuna 2 - Oriente
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Alvernia', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'Álvaro Mejía', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'Cándido', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'El Cedral', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'Guacirco', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'Jardín', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'La Floresta', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'Primavera', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente'
UNION ALL SELECT 'San Fernando', id, now(), now() FROM comuna WHERE nombre = 'Comuna 2 - Oriente';

-- Comuna 3 - Centro
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Centro', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'La Gaitana', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'La Libertad', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'Mipueblo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'Modelo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'Quinta Oriental', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro'
UNION ALL SELECT 'San Luís', id, now(), now() FROM comuna WHERE nombre = 'Comuna 3 - Centro';

-- Comuna 4 - Occidental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Altico del Río', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental'
UNION ALL SELECT 'Campo Núñez', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental'
UNION ALL SELECT 'Granjas', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental'
UNION ALL SELECT 'La Magdalena', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental'
UNION ALL SELECT 'Los Pinos', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental'
UNION ALL SELECT 'Sevilla', id, now(), now() FROM comuna WHERE nombre = 'Comuna 4 - Occidental';

-- Comuna 5 - Suroriental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Antonia Santos', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'El Bosque', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'El Limonar', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'La Vega', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'Luis Ignacio Andrade', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'Panorama', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental'
UNION ALL SELECT 'Santa Isabel', id, now(), now() FROM comuna WHERE nombre = 'Comuna 5 - Suroriental';

-- Comuna 6 - Noroccidental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Comuneros', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'El Progreso', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'Inocencio Chincué', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'La Torcoroma', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'Las Brisas', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'Los Cambulos', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental'
UNION ALL SELECT 'San Jorge', id, now(), now() FROM comuna WHERE nombre = 'Comuna 6 - Noroccidental';

-- Comuna 7 - Suroccidental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Buganviles', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'El Pedregal', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'La Jagua', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'Los Alpes', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'Los Mártires', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'San Diego', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental'
UNION ALL SELECT 'San Francisco', id, now(), now() FROM comuna WHERE nombre = 'Comuna 7 - Suroccidental';

-- Comuna 8 - Nororiental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'El Lago', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'El Triunfo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'Ipanema', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'La Colina', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'Los Libertadores', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'Maracaibo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental'
UNION ALL SELECT 'Villa Carolina', id, now(), now() FROM comuna WHERE nombre = 'Comuna 8 - Nororiental';

-- Comuna 9 - Oriental
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Arrayanes', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'El Caguán', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'La Orquídea', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'Las Ceibas', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'Palermo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'Quirinal', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental'
UNION ALL SELECT 'San Antonio', id, now(), now() FROM comuna WHERE nombre = 'Comuna 9 - Oriental';

-- Comuna 10 - Sur
INSERT INTO barrio (nombre, comuna_id, creado_en, actualizado_en)
SELECT 'Cálamo', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'Chapinero', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'El Caguán Sur', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'Guaduales', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'La Plata', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'Mampuesto', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur'
UNION ALL SELECT 'Río de Oro', id, now(), now() FROM comuna WHERE nombre = 'Comuna 10 - Sur';
