package com.moderacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Servicio que implementa las validaciones automáticas con algoritmos
 * para moderar contenido de mensajes
 * Utiliza tablas de la base de datos para palabras prohibidas y con peso
 */
public class ServicioValidacion {
    
    // Patrones de detección
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );
    
    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
        "\\b(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b"
    );
    
    private MensajeDAO mensajeDAO;
    private PalabraProhibidaDAO palabraProhibidaDAO;
    private PalabraPesoDAO palabraPesoDAO;
    
    public ServicioValidacion(MensajeDAO mensajeDAO) {
        this.mensajeDAO = mensajeDAO;
        this.palabraProhibidaDAO = new PalabraProhibidaDAO();
        this.palabraPesoDAO = new PalabraPesoDAO();
    }
    
    /**
     * Valida un mensaje usando múltiples algoritmos
     * @param mensaje El mensaje a validar
     * @return Lista de razones de bloqueo (vacía si el mensaje es válido)
     */
    public List<String> validarMensaje(Mensaje mensaje) {
        List<String> razones = new ArrayList<>();
        
        // 1. Validar palabras prohibidas (desde base de datos)
        if (contienePalabrasProhibidas(mensaje.getContenido())) {
            PalabraProhibida pp = palabraProhibidaDAO.obtenerPrimeraPalabraProhibida(mensaje.getContenido());
            if (pp != null) {
                razones.add("Contiene palabra prohibida: " + pp.getPalabra() + " (Razón: " + pp.getRazon() + ")");
            }
        }
        
        // 2. Validar exceso de mayúsculas
        if (tieneExcesoMayusculas(mensaje.getContenido())) {
            razones.add("Exceso de mayúsculas (>50% del texto)");
        }
        
        // 3. Validar correos electrónicos
        if (contieneCorreoElectronico(mensaje.getContenido())) {
            razones.add("Contiene dirección de correo electrónico");
        }
        
        // 4. Validar números de teléfono
        if (contieneTelefono(mensaje.getContenido())) {
            razones.add("Contiene número de teléfono");
        }
        
        // 5. Validar exceso de puntuación
        if (tieneExcesoPuntuacion(mensaje.getContenido())) {
            razones.add("Uso excesivo de signos de puntuación");
        }
        
        // 6. Validar peso del mensaje (usando base de datos)
        double pesoCalculado = calcularPeso(mensaje.getContenido());
        if (pesoCalculado > 10.0) {
            razones.add(String.format("Mensaje con peso sospechoso alto (%.2f > 10.0)", pesoCalculado));
        }
        
        // 7. Validar repeticiones
        if (tieneRepeticionesExcesivas(mensaje.getContenido())) {
            razones.add("Texto con repeticiones excesivas");
        }
        
        // 8. Validar velocidad de envío
        if (envioDemasiadoRapido(mensaje)) {
            razones.add("Enviado demasiado rápido (posible spam)");
        }
        
        return razones;
    }
    
    /**
     * Verifica si el contenido contiene palabras prohibidas (desde base de datos)
     */
    private boolean contienePalabrasProhibidas(String contenido) {
        return palabraProhibidaDAO.contienePalabraProhibida(contenido);
    }
    
    /**
     * Verifica si el mensaje tiene exceso de mayúsculas (>50%)
     */
    private boolean tieneExcesoMayusculas(String contenido) {
        if (contenido.isEmpty()) return false;
        
        long mayusculas = contenido.chars().filter(Character::isUpperCase).count();
        double porcentaje = (double) mayusculas / contenido.length();
        
        return porcentaje > 0.5;
    }
    
    /**
     * Verifica si el contenido contiene direcciones de correo electrónico
     */
    private boolean contieneCorreoElectronico(String contenido) {
        return EMAIL_PATTERN.matcher(contenido).find();
    }
    
    /**
     * Verifica si el contenido contiene números de teléfono
     */
    private boolean contieneTelefono(String contenido) {
        return TELEFONO_PATTERN.matcher(contenido).find();
    }
    
    /**
     * Verifica si hay exceso de signos de puntuación
     */
    private boolean tieneExcesoPuntuacion(String contenido) {
        if (contenido.isEmpty()) return false;
        
        long puntuacion = contenido.chars()
            .filter(c -> "!?¡¿.,;:".indexOf(c) >= 0)
            .count();
        
        return puntuacion > contenido.length() * 0.2;
    }
    
    /**
     * Calcula el peso de un mensaje basado en palabras sospechosas (desde base de datos)
     * Algoritmo: Obtiene pesos de la base de datos y suma el total con factor de contexto
     */
    private double calcularPeso(String contenido) {
        String contenidoLower = contenido.toLowerCase();
        double pesoTotal = 0.0;
        
        String[] palabras = contenidoLower.split("\\s+");
        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i].replaceAll("[^a-z]", "");
            if (!palabra.isEmpty()) {
                // Obtener peso de la base de datos
                double pesoPalabra = palabraPesoDAO.obtenerPesoOPorDefecto(palabra, 1.0);
                pesoTotal += pesoPalabra;
                
                // Factor de contexto: multiplicar por el peso de las siguientes 2 palabras
                double pesoContexto = 1.0;
                for (int j = 1; j <= 2 && i + j < palabras.length; j++) {
                    String palabraContexto = palabras[i + j].replaceAll("[^a-z]", "");
                    if (!palabraContexto.isEmpty()) {
                        double pesoPalabraContexto = palabraPesoDAO.obtenerPesoOPorDefecto(palabraContexto, 1.0);
                        pesoContexto *= pesoPalabraContexto / (j + 1);
                    }
                }
                
                pesoTotal += pesoPalabra * pesoContexto;
            }
        }
        
        // Factor de contexto: contar palabras totales
        if (palabras.length > 50) {
            pesoTotal *= 1.2; // Penalizar mensajes muy largos
        }
        
        return pesoTotal;
    }
    
    /**
     * Verifica si hay repeticiones excesivas de caracteres o palabras
     */
    private boolean tieneRepeticionesExcesivas(String contenido) {
        // Detectar caracteres repetidos consecutivamente (ej: "¡¡¡¡" o "aaaaaa")
        if (contenido.matches(".*(.)\\1{4,}.*")) {
            return true;
        }
        
        // Detectar palabras repetidas múltiples veces
        String[] palabras = contenido.toLowerCase().split("\\s+");
        Map<String, Integer> conteo = new HashMap<>();
        
        for (String palabra : palabras) {
            if (palabra.length() > 3) { // Ignorar palabras muy cortas
                conteo.put(palabra, conteo.getOrDefault(palabra, 0) + 1);
            }
        }
        
        // Si alguna palabra aparece más de 3 veces, es sospechoso
        for (int count : conteo.values()) {
            if (count > 3) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el mensaje fue enviado demasiado rápido (indicador de spam)
     */
    private boolean envioDemasiadoRapido(Mensaje mensaje) {
        // Simular verificación de velocidad
        // En implementación real, consultaría mensajes anteriores del usuario
        // y calcularía la diferencia de tiempo
        
        List<Mensaje> mensajesUsuario = mensajeDAO.obtenerUltimosMensajes(mensaje.getUsuarioId(), 5);
        
        if (mensajesUsuario.size() < 2) {
            return false; // No hay suficientes mensajes para comparar
        }
        
        // Verificar si el usuario envió más de 5 mensajes en los últimos 2 minutos
        long totalMensajes = mensajesUsuario.stream()
            .filter(m -> esReciente(m.getFechaCreacion(), 2)) // últimos 2 minutos
            .count();
        
        return totalMensajes > 5;
    }
    
    private boolean esReciente(java.time.LocalDateTime fecha, int minutos) {
        return java.time.Duration.between(fecha, java.time.LocalDateTime.now()).toMinutes() < minutos;
    }
    
    /**
     * Determina el estado del mensaje basado en las validaciones
     */
    public EstadoMensaje determinarEstado(List<String> razones) {
        if (razones.isEmpty()) {
            return EstadoMensaje.PENDIENTE; // Necesita revisión manual
        } else if (razones.size() >= 3) {
            return EstadoMensaje.BLOQUEADO; // Múltiples razones, bloqueado automáticamente
        } else if (razones.size() >= 1) {
            return EstadoMensaje.SOSPECHOSO; // Sospechoso, requiere revisión
        }
        return EstadoMensaje.PENDIENTE;
    }
}

