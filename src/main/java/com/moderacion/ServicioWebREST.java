package com.moderacion;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio web REST minimalista para moderación de mensajes
 * Similar a Flask de Python, usa Javalin para endpoints REST
 */
public class ServicioWebREST {
    
    private final Javalin app;
    private final MensajeDAO mensajeDAO;
    private final ServicioValidacion servicioValidacion;
    private final Gson gson;
    private static final int PUERTO = 7000;
    
    public ServicioWebREST() {
        this.mensajeDAO = new MensajeDAO();
        this.servicioValidacion = new ServicioValidacion(mensajeDAO);
        this.gson = new Gson();
        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        });
        
        configurarRutas();
    }
    
    /**
     * Configura las rutas del API REST
     */
    private void configurarRutas() {
        // Ruta de bienvenida
        app.get("/", ctx -> {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("servicio", "Sistema de Moderación Escalonada");
            respuesta.put("version", "1.0");
            respuesta.put("endpoints", new String[]{
                "POST /api/mensajes - Enviar mensaje para moderación",
                "GET /api/mensajes/{id} - Consultar estado de mensaje",
                "GET /api/estadisticas - Estadísticas del sistema"
            });
            ctx.contentType("application/json").result(gson.toJson(respuesta));
        });
        
        // POST /api/mensajes - Enviar mensaje para moderación
        app.post("/api/mensajes", this::crearYValidarMensaje);
        
        // GET /api/mensajes/{id} - Consultar mensaje por ID
        app.get("/api/mensajes/{id}", this::consultarMensaje);
        
        // GET /api/estadisticas - Estadísticas del sistema
        app.get("/api/estadisticas", this::obtenerEstadisticas);
    }
    
    /**
     * POST /api/mensajes
     * Crea un mensaje y lo valida automáticamente
     */
    private void crearYValidarMensaje(Context ctx) {
        try {
            // Leer el JSON del body
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String contenido = (String) body.get("contenido");
            Integer usuarioId = body.containsKey("usuario_id") ? 
                               ((Double) body.get("usuario_id")).intValue() : 1;
            
            if (contenido == null || contenido.trim().isEmpty()) {
                ctx.status(400).contentType("application/json")
                   .result(gson.toJson(crearRespuestaError("El contenido del mensaje es requerido")));
                return;
            }
            
            // Crear mensaje temporal para validación
            Mensaje mensaje = new Mensaje(
                0, // ID temporal
                contenido,
                EstadoMensaje.NUEVO,
                LocalDateTime.now(),
                usuarioId
            );
            
            // Ejecutar validaciones
            List<String> razones = servicioValidacion.validarMensaje(mensaje);
            EstadoMensaje estado = servicioValidacion.determinarEstado(razones);
            mensaje.setEstado(estado);
            mensaje.setRazonesBloqueo(razones);
            
            // Guardar en base de datos
            int mensajeId = mensajeDAO.insertarMensaje(mensaje);
            mensaje.setId(mensajeId);
            
            // Crear respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje_id", mensajeId);
            respuesta.put("contenido", contenido);
            respuesta.put("estado", estado.name());
            respuesta.put("estado_descripcion", estado.getDescripcion());
            respuesta.put("razones_bloqueo", razones);
            respuesta.put("total_razones", razones.size());
            respuesta.put("fecha_creacion", mensaje.getFechaCreacion().toString());
            
            // Agregar mensaje descriptivo según el estado
            if (estado == EstadoMensaje.BLOQUEADO) {
                respuesta.put("mensaje", "⛔ Mensaje BLOQUEADO por el sistema de validación automática");
            } else if (estado == EstadoMensaje.SOSPECHOSO) {
                respuesta.put("mensaje", "⚠️  Mensaje SOSPECHOSO - Requiere revisión manual");
            } else {
                respuesta.put("mensaje", "✅ Mensaje validado - Sin problemas detectados");
            }
            
            ctx.status(201).contentType("application/json").result(gson.toJson(respuesta));
            
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).contentType("application/json")
               .result(gson.toJson(crearRespuestaError("Error interno del servidor: " + e.getMessage())));
        }
    }
    
    /**
     * GET /api/mensajes/{id}
     * Consulta el estado de un mensaje por su ID
     */
    private void consultarMensaje(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            
            // Buscar mensaje en BD (necesitamos un método en DAO)
            Mensaje mensaje = mensajeDAO.obtenerMensajePorId(id);
            
            if (mensaje == null) {
                ctx.status(404).contentType("application/json")
                   .result(gson.toJson(crearRespuestaError("Mensaje no encontrado")));
                return;
            }
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje_id", mensaje.getId());
            respuesta.put("contenido", mensaje.getContenido());
            respuesta.put("estado", mensaje.getEstado().name());
            respuesta.put("estado_descripcion", mensaje.getEstado().getDescripcion());
            respuesta.put("razones_bloqueo", mensaje.getRazonesBloqueo());
            respuesta.put("fecha_creacion", mensaje.getFechaCreacion().toString());
            respuesta.put("usuario_id", mensaje.getUsuarioId());
            
            ctx.contentType("application/json").result(gson.toJson(respuesta));
            
        } catch (NumberFormatException e) {
            ctx.status(400).contentType("application/json")
               .result(gson.toJson(crearRespuestaError("ID de mensaje inválido")));
        } catch (Exception e) {
            System.err.println("Error al consultar mensaje: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).contentType("application/json")
               .result(gson.toJson(crearRespuestaError("Error interno del servidor")));
        }
    }
    
    /**
     * GET /api/estadisticas
     * Obtiene estadísticas del sistema de moderación
     */
    private void obtenerEstadisticas(Context ctx) {
        try {
            Map<String, Object> stats = mensajeDAO.obtenerEstadisticas();
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("estadisticas", stats);
            
            ctx.contentType("application/json").result(gson.toJson(respuesta));
            
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).contentType("application/json")
               .result(gson.toJson(crearRespuestaError("Error interno del servidor")));
        }
    }
    
    /**
     * Crea una respuesta de error estándar
     */
    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", mensaje);
        return error;
    }
    
    /**
     * Inicia el servidor web
     */
    public void iniciar() {
        app.start(PUERTO);
        System.out.println("🚀 Servicio web REST iniciado en http://localhost:" + PUERTO);
        System.out.println("📝 Endpoints disponibles:");
        System.out.println("   - POST http://localhost:" + PUERTO + "/api/mensajes");
        System.out.println("   - GET  http://localhost:" + PUERTO + "/api/mensajes/{id}");
        System.out.println("   - GET  http://localhost:" + PUERTO + "/api/estadisticas");
    }
    
    /**
     * Detiene el servidor web
     */
    public void detener() {
        app.stop();
        System.out.println("🛑 Servicio web REST detenido");
    }
    
    /**
     * Obtiene el puerto en el que está corriendo el servidor
     */
    public int getPuerto() {
        return PUERTO;
    }
}

