-- Base de datos para el Sistema de Moderación
CREATE DATABASE IF NOT EXISTS moderacion_db;
USE moderacion_db;

-- Tabla de usuarios
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de mensajes
CREATE TABLE mensajes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenido TEXT NOT NULL,
    estado ENUM('NUEVO', 'PENDIENTE', 'EN_MODERACION', 'BLOQUEADO', 'APROBADO', 'RECHAZADO', 'SOSPECHOSO') 
           DEFAULT 'NUEVO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_moderacion TIMESTAMP NULL,
    usuario_id INT,
    razones_bloqueo TEXT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_creacion (fecha_creacion),
    INDEX idx_usuario_id (usuario_id)
);

-- Tabla de moderación (historial)
CREATE TABLE moderacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mensaje_id INT NOT NULL,
    accion ENUM('APROBAR', 'RECHAZAR') NOT NULL,
    fecha_accion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    razon TEXT NULL,
    FOREIGN KEY (mensaje_id) REFERENCES mensajes(id)
);

-- Tabla de palabras prohibidas
CREATE TABLE palabras_prohibidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    palabra VARCHAR(100) NOT NULL UNIQUE,
    razon VARCHAR(255) NOT NULL,
    severidad INT DEFAULT 1 CHECK (severidad BETWEEN 1 AND 5),
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_palabra (palabra)
);

-- Tabla de palabras con peso
CREATE TABLE palabras_peso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    palabra VARCHAR(100) NOT NULL UNIQUE,
    peso DECIMAL(10,2) NOT NULL DEFAULT 1.0,
    contexto VARCHAR(255) NULL,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_palabra (palabra)
);

-- Datos de ejemplo
INSERT INTO usuarios (nombre, email) VALUES 
('Juan Pérez', 'juan@example.com'),
('María González', 'maria@example.com'),
('Pedro García', 'pedro@example.com');

INSERT INTO mensajes (contenido, estado, usuario_id) VALUES 
('Este es un mensaje de prueba que necesita moderación. Contiene contenido que debe ser revisado por un administrador antes de ser publicado.', 'PENDIENTE', 1),
('Otro mensaje que está esperando revisión. Este es un contenido importante que requiere aprobación.', 'PENDIENTE', 2),
('Un tercer mensaje pendiente de aprobación. Este mensaje contiene información relevante para el sistema.', 'PENDIENTE', 1),
('Mensaje adicional para pruebas del sistema de moderación. Este texto es más largo para ver cómo se comporta la interfaz con contenido extenso.', 'PENDIENTE', 2);

-- Mensajes sospechosos o bloqueados de ejemplo
INSERT INTO mensajes (contenido, estado, usuario_id, razones_bloqueo) VALUES 
('CLICK AQUÍ PARA GANAR UN MILLÓN $$$ GRAFIS DESCARGAR AHORA', 'BLOQUEADO', 3, 'Exceso de mayúsculas;Contiene palabras prohibidas'),
('Email: spam@example.com Tel: +1234567890 OFERTA', 'SOSPECHOSO', 1, 'Contiene dirección de correo electrónico;Contiene número de teléfono'),
('¡¡¡¡¡¡URGENTE!!!!! CLICK!!!!! CLICK!!!!! CLICK!!!!!', 'BLOQUEADO', 2, 'Uso excesivo de signos de puntuación;Texto con repeticiones excesivas');

-- Insertar palabras prohibidas
-- Incluye protección para menores: palabras ofensivas codificadas con * para mantener decoro académico
INSERT INTO palabras_prohibidas (palabra, razon, severidad) VALUES
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
-- Palabras sensibles para menores (ofensivas y groserías)
('p*t*', 'Lenguaje ofensivo y grosero', 5),
('m*l*m*l*t*', 'Contenido vulgar e inapropiado', 5),
('c*b*', 'Terminología vulgar', 5),
('h*v*r*', 'Terminología vulgar', 4),
('p*nd*j*', 'Palabra ofensiva', 4),
('m*r*d*', 'Lenguaje soez', 5),
('d*m*n*', 'Lenguaje ofensivo', 5),
-- Acoso y violencia
('m*t*r', 'Referencia a violencia', 5),
('d*r', 'Referencia a violencia física', 5),
('c*ll*r', 'Referencia a violencia', 4),
-- Contenido inapropiado sexual
('s*x*', 'Referencia sexual inapropiada para menores', 5),
('n*p*l*', 'Contenido sexual explícito', 5),
('p*', 'Referencia sexual inapropiada', 4),
-- Riesgos para menores
('d*r*g*s', 'Referencia a sustancias prohibidas', 5),
('l*c*r', 'Referencia a alcohol', 4),
('f*m*r', 'Referencia a tabaco', 4),
('s*c*d*', 'Contenido suicida', 5),
-- Acoso cibernético
('b*ll*ng', 'Acoso escolar', 5),
('s*t*c*d*', 'Acoso cibernético', 5),
-- Autolesión
('c*rt*rs*', 'Referencia a autolesión', 5),
('m*rt*r', 'Referencia a autolesión', 5);

-- Insertar palabras con peso para el algoritmo de moderación
INSERT INTO palabras_peso (palabra, peso, contexto) VALUES
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

