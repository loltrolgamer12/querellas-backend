-- Estados base para QUERELLA
INSERT INTO estado (id, modulo, nombre, creado_en, actualizado_en)
VALUES
  (1, 'QUERELLA', 'RECIBIDA', now(), now()),
  (2, 'QUERELLA', 'ASIGNADA', now(), now());

-- Catálogos mínimos
INSERT INTO tema(id, nombre, creado_en, actualizado_en) VALUES (1, 'Ruidos', now(), now());
INSERT INTO comuna(id, nombre, creado_en, actualizado_en) VALUES (1, 'Comuna 1', now(), now());
INSERT INTO inspeccion(id, nombre, creado_en, actualizado_en) VALUES (1, 'Inspección Tercera', now(), now());
