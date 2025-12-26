-- ==========================================
-- ROLLBACK: Tabla configuracion_sistema
-- Elimina la tabla configuracion_sistema
-- ==========================================

-- Eliminar tabla configuracion_sistema (con CASCADE para eliminar dependencias)
DROP TABLE IF EXISTS configuracion_sistema CASCADE;

-- Nota: Este rollback eliminará todos los datos de configuración del sistema
-- incluyendo el tracking del round-robin
