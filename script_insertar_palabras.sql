-- Script para insertar o actualizar palabras en la base de datos
-- Útil para agregar nuevas palabras sin recrear la BD

-- Insertar nuevas palabras prohibidas (ignora duplicados)
-- INCLUYE PROTECCIÓN PARA MENORES - Palabras codificadas para mantener decoro académico
INSERT IGNORE INTO palabras_prohibidas (palabra, razon, severidad) VALUES
-- Comerciales y spam
('spam', 'Contenido no solicitado', 5),
('publicidad', 'Publicidad no autorizada', 4),
('click', 'Intentos de clic forzado', 4),
('descarga', 'Links de descarga sospechosos', 3),
('gratis', 'Ofertas sospechosas de gratuidad', 3),
('oferta', 'Ofertas comerciales', 2),
('premio', 'Promesas de premios', 3),
('ganaste', 'Engaños tipo premio', 4),
('urgente', 'Urgencia falsa', 2),
('importante', 'Urgencia falsa', 1),
('promocion', 'Promociones no autorizadas', 3),
('atencion', 'Urgencia sospechosa', 2),
('ganar', 'Promesas de ganancias', 3),
('dinero', 'Referencias a dinero', 2),
('nuevo', 'Publicidad', 1),
('descuento', 'Publicidad comercial', 2),
-- PROTECCIÓN MENORES: Lenguaje ofensivo
('p*t*', 'Lenguaje ofensivo y grosero', 5),
('m*l*m*l*t*', 'Contenido vulgar e inapropiado', 5),
('c*b*', 'Terminología vulgar', 5),
('h*v*r*', 'Terminología vulgar', 4),
('p*nd*j*', 'Palabra ofensiva', 4),
('m*r*d*', 'Lenguaje soez', 5),
('d*m*n*', 'Lenguaje ofensivo', 5),
-- PROTECCIÓN MENORES: Contenido sexual inapropiado
('s*x*', 'Referencia sexual inapropiada para menores', 5),
('n*p*l*', 'Contenido sexual explícito', 5),
('p*', 'Referencia sexual inapropiada', 4),
-- PROTECCIÓN MENORES: Riesgos (sustancias)
('d*r*g*s', 'Referencia a sustancias prohibidas', 5),
('l*c*r', 'Referencia a alcohol', 4),
('f*m*r', 'Referencia a tabaco', 4),
-- PROTECCIÓN MENORES: Autolesión
('s*c*d*', 'Contenido suicida', 5),
('c*rt*rs*', 'Referencia a autolesión', 5),
('m*rt*r', 'Referencia a autolesión', 5),
-- PROTECCIÓN MENORES: Acoso y violencia
('m*t*r', 'Referencia a violencia', 5),
('d*r', 'Referencia a violencia física', 5),
('c*ll*r', 'Referencia a violencia', 4),
('b*ll*ng', 'Acoso escolar', 5),
('s*t*c*d*', 'Acoso cibernético', 5);

-- Insertar nuevas palabras con peso (ignora duplicados)
INSERT IGNORE INTO palabras_peso (palabra, peso, contexto) VALUES
('urgente', 5.0, 'Indica urgencia falsa'),
('gratis', 3.0, 'Ofertas sospechosas'),
('oferta', 2.0, 'Publicidad'),
('click', 4.0, 'Intentos de interacción forzada'),
('descarga', 3.5, 'Links sospechosos'),
('gana', 3.0, 'Promesas de premios'),
('premio', 2.5, 'Engaños de premios'),
('importante', 1.5, 'Urgencia falsa'),
('spam', 5.0, 'Contenido no deseado'),
('promocion', 2.0, 'Publicidad'),
('atencion', 2.0, 'Urgencia sospechosa'),
('dinero', 1.5, 'Referencias financieras'),
('nuevo', 1.0, 'Publicidad'),
('descuento', 1.5, 'Publicidad comercial'),
('publicidad', 4.0, 'Publicidad no autorizada'),
('promesa', 3.0, 'Promesas falsas'),
('regalo', 2.5, 'Ofertas dudosas'),
('millon', 5.0, 'Promesas exageradas'),
('fortuna', 4.5, 'Promesas de riqueza');

-- Consultas útiles para verificar

-- Ver todas las palabras prohibidas ordenadas por severidad
SELECT palabra, razon, severidad FROM palabras_prohibidas ORDER BY severidad DESC, palabra;

-- Ver todas las palabras con peso ordenadas por peso
SELECT palabra, peso, contexto FROM palabras_peso ORDER BY peso DESC, palabra;

-- Contar palabras por severidad
SELECT severidad, COUNT(*) as total FROM palabras_prohibidas GROUP BY severidad ORDER BY severidad;

-- Ver palabras con peso superior a 3.0 (muy sospechosas)
SELECT palabra, peso, contexto FROM palabras_peso WHERE peso > 3.0 ORDER BY peso DESC;

