-- ========================================
-- ACTUALIZACIÓN DE ESTADOS PARA QUERELLAS Y DESPACHOS COMISORIOS
-- ========================================

-- ELIMINAR ESTADOS ANTIGUOS DE QUERELLAS (si existen)
DELETE FROM estado_transicion WHERE modulo = 'QUERELLA';
DELETE FROM historial_estado WHERE modulo = 'QUERELLA';
DELETE FROM estado WHERE modulo = 'QUERELLA';

-- NUEVOS ESTADOS PARA QUERELLAS
INSERT INTO estado (modulo, nombre, creado_en) VALUES
('QUERELLA', 'APERTURA', now()),
('QUERELLA', 'NOTIFICACION', now()),
('QUERELLA', 'AUDIENCIA_PUBLICA', now()),
('QUERELLA', 'DECISION', now()),
('QUERELLA', 'RECURSO', now()),
('QUERELLA', 'INADMISIBLE', now());

-- TRANSICIONES PERMITIDAS PARA QUERELLAS
-- De APERTURA puede ir a NOTIFICACION o INADMISIBLE
INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION');

INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'APERTURA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'INADMISIBLE');

-- De NOTIFICACION puede ir a AUDIENCIA_PUBLICA
INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'NOTIFICACION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA');

-- De AUDIENCIA_PUBLICA puede ir a DECISION
INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'AUDIENCIA_PUBLICA'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION');

-- De DECISION puede ir a RECURSO o finalizar
INSERT INTO estado_transicion (modulo, desde_id, hacia_id)
SELECT 'QUERELLA',
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'DECISION'),
       (SELECT id FROM estado WHERE modulo = 'QUERELLA' AND nombre = 'RECURSO');

-- NOTA: Los estados de DESPACHO COMISORIO serán agregados cuando se especifiquen
-- Por favor contactar al administrador para definir los estados de despachos comisorios
