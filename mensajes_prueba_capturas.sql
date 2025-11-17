-- Script para insertar mensajes de prueba que servirán para las capturas del TP4
-- Ejecutar este script antes de tomar las capturas de pantalla
-- Uso: mysql -u moderacion_user -p moderacion_db < mensajes_prueba_capturas.sql

USE moderacion_db;

-- Insertar mensajes con diferentes problemas para demostrar las validaciones

-- 1. Mensaje con MÚLTIPLES problemas (para captura de validación automática)
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('HOLA!!! MI CORREO ES test@ejemplo.com Y MI TELÉFONO ES +54-351-123-4567!!!! NECESITO AYUDA URGENTE!!!! spam spam spam spam spam',
 'PENDIENTE', 
 NOW(), 
 1, 
 NULL);

-- 2. Mensaje con exceso de mayúsculas
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('ESTE MENSAJE ESTÁ COMPLETAMENTE EN MAYÚSCULAS Y DEBERÍA SER DETECTADO POR EL SISTEMA',
 'PENDIENTE',
 NOW() - INTERVAL 1 MINUTE,
 2,
 NULL);

-- 3. Mensaje con palabra prohibida
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Hola amigos, alguien sabe donde puedo conseguir cosas prohibidas en el juego',
 'PENDIENTE',
 NOW() - INTERVAL 2 MINUTE,
 1,
 NULL);

-- 4. Mensaje con email
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Hola, si quieres contactarme mi email es usuario123@gmail.com',
 'PENDIENTE',
 NOW() - INTERVAL 3 MINUTE,
 3,
 NULL);

-- 5. Mensaje con teléfono
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Llámame al 351-456-7890 para coordinar',
 'PENDIENTE',
 NOW() - INTERVAL 4 MINUTE,
 2,
 NULL);

-- 6. Mensaje con exceso de puntuación
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('¡¡¡¡¡Hola!!!! ¿¿¿Alguien me ayuda??? ¡¡¡Urgente!!!',
 'PENDIENTE',
 NOW() - INTERVAL 5 MINUTE,
 1,
 NULL);

-- 7. Mensaje con repeticiones excesivas
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('aaaaaaahhhhhh ayudaaaaa porfaaaaa necesito ayudaaaa',
 'PENDIENTE',
 NOW() - INTERVAL 6 MINUTE,
 3,
 NULL);

-- 8. Mensaje ya BLOQUEADO (para mostrar color rojo)
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Este mensaje fue bloqueado previamente',
 'BLOQUEADO',
 NOW() - INTERVAL 10 MINUTE,
 2,
 'Contiene palabra prohibida: spam (Razón: Publicidad no autorizada);Exceso de mayúsculas (>50% del texto);Uso excesivo de signos de puntuación');

-- 9. Mensaje SOSPECHOSO (para mostrar color amarillo)
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Este mensaje requiere revisión manual',
 'SOSPECHOSO',
 NOW() - INTERVAL 15 MINUTE,
 1,
 'Mensaje con peso sospechoso alto (12.50 > 10.0)');

-- 10. Mensaje APROBADO (para mostrar color verde)
INSERT INTO mensajes (contenido, estado, fecha_creacion, fecha_moderacion, usuario_id, razones_bloqueo) VALUES 
('Este es un mensaje perfectamente válido y fue aprobado',
 'APROBADO',
 NOW() - INTERVAL 20 MINUTE,
 NOW() - INTERVAL 18 MINUTE,
 3,
 '');

-- 11. Otro mensaje APROBADO
INSERT INTO mensajes (contenido, estado, fecha_creacion, fecha_moderacion, usuario_id, razones_bloqueo) VALUES 
('Hola equipo, ¿cómo están? Espero que bien',
 'APROBADO',
 NOW() - INTERVAL 25 MINUTE,
 NOW() - INTERVAL 22 MINUTE,
 2,
 '');

-- 12. Mensaje normal pendiente (sin problemas obvios)
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('Hola a todos, ¿alguien sabe cómo completar la misión del castillo?',
 'PENDIENTE',
 NOW() - INTERVAL 7 MINUTE,
 1,
 NULL);

-- 13. Otro mensaje bloqueado
INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) VALUES 
('MENSAJE SPAM SPAM SPAM BLOQUEADO!!!!',
 'BLOQUEADO',
 NOW() - INTERVAL 30 MINUTE,
 3,
 'Exceso de mayúsculas (>50% del texto);Texto con repeticiones excesivas;Uso excesivo de signos de puntuación');

-- Verificar los mensajes insertados
SELECT id, 
       LEFT(contenido, 50) as contenido_preview, 
       estado, 
       fecha_creacion,
       usuario_id,
       LEFT(razones_bloqueo, 50) as razones_preview
FROM mensajes 
ORDER BY fecha_creacion DESC 
LIMIT 15;

-- Consulta para ver distribución de estados
SELECT estado, COUNT(*) as cantidad 
FROM mensajes 
GROUP BY estado 
ORDER BY cantidad DESC;

COMMIT;

