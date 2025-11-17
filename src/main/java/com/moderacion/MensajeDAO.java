package com.moderacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase de acceso a datos para mensajes
 */
public class MensajeDAO {
    // Configuración por defecto para Docker de mariadb
    private static final String URL = "jdbc:mariadb://localhost:3306/moderacion_db";
    private static final String USER = "moderacion_user";
    private static final String PASSWORD = "moderacion_pass";
    
    /**
     * Obtiene todos los mensajes pendientes de moderación
     * @return Lista de mensajes pendientes
     */
    public List<Mensaje> obtenerMensajesPendientes() {
        List<Mensaje> mensajes = new ArrayList<>();
        String sql = "SELECT id, contenido, estado, fecha_creacion, usuario_id, razones_bloqueo " +
                     "FROM mensajes WHERE estado IN ('PENDIENTE', 'SOSPECHOSO') ORDER BY fecha_creacion";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Mensaje msg = crearMensajeDesdeResultSet(rs);
                mensajes.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener mensajes pendientes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mensajes;
    }
    
    /**
     * Obtiene los últimos N mensajes de un usuario
     * @param usuarioId ID del usuario
     * @param cantidad Cantidad de mensajes a obtener
     * @return Lista de mensajes
     */
    public List<Mensaje> obtenerUltimosMensajes(int usuarioId, int cantidad) {
        List<Mensaje> mensajes = new ArrayList<>();
        String sql = "SELECT id, contenido, estado, fecha_creacion, usuario_id, razones_bloqueo " +
                     "FROM mensajes WHERE usuario_id = ? ORDER BY fecha_creacion DESC LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, cantidad);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mensaje msg = crearMensajeDesdeResultSet(rs);
                    mensajes.add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener últimos mensajes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mensajes;
    }
    
    /**
     * Crea un objeto Mensaje desde un ResultSet
     */
    private Mensaje crearMensajeDesdeResultSet(ResultSet rs) throws SQLException {
        Mensaje msg = new Mensaje(
            rs.getInt("id"),
            rs.getString("contenido"),
            rs.getString("estado"),
            rs.getTimestamp("fecha_creacion").toLocalDateTime(),
            rs.getInt("usuario_id")
        );
        
        // Cargar razones de bloqueo si existen
        String razones = rs.getString("razones_bloqueo");
        if (razones != null && !razones.isEmpty()) {
            List<String> razonesList = Arrays.asList(razones.split(";"));
            msg.setRazonesBloqueo(razonesList);
        }
        
        return msg;
    }
    
    /**
     * Aprueba un mensaje
     * @param id ID del mensaje a aprobar
     * @return true si la operación fue exitosa
     */
    public boolean aprobarMensaje(int id) {
        String sql = "UPDATE mensajes SET estado = 'APROBADO', " +
                     "fecha_moderacion = CURRENT_TIMESTAMP WHERE id = ?";
        return actualizarEstado(id, sql);
    }
    
    /**
     * Rechaza un mensaje
     * @param id ID del mensaje a rechazar
     * @return true si la operación fue exitosa
     */
    public boolean rechazarMensaje(int id) {
        String sql = "UPDATE mensajes SET estado = 'RECHAZADO', " +
                     "fecha_moderacion = CURRENT_TIMESTAMP WHERE id = ?";
        return actualizarEstado(id, sql);
    }
    
    /**
     * Método privado que ejecuta la actualización del estado
     * @param id ID del mensaje
     * @param sql Consulta SQL a ejecutar
     * @return true si la operación fue exitosa
     */
    private boolean actualizarEstado(int id, String sql) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Inserta un nuevo mensaje en la base de datos
     * @param mensaje El mensaje a insertar
     * @return El ID del mensaje insertado, o -1 si hubo error
     */
    public int insertarMensaje(Mensaje mensaje) {
        String sql = "INSERT INTO mensajes (contenido, estado, fecha_creacion, usuario_id, razones_bloqueo) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, mensaje.getContenido());
            stmt.setString(2, mensaje.getEstado().name());
            stmt.setTimestamp(3, Timestamp.valueOf(mensaje.getFechaCreacion()));
            stmt.setInt(4, mensaje.getUsuarioId());
            
            // Convertir lista de razones a String separado por ;
            String razonesStr = mensaje.getRazonesBloqueo().isEmpty() ? 
                               null : String.join(";", mensaje.getRazonesBloqueo());
            stmt.setString(5, razonesStr);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Obtener el ID generado
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al insertar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Obtiene un mensaje por su ID
     * @param id ID del mensaje
     * @return El mensaje encontrado, o null si no existe
     */
    public Mensaje obtenerMensajePorId(int id) {
        String sql = "SELECT id, contenido, estado, fecha_creacion, usuario_id, razones_bloqueo " +
                     "FROM mensajes WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return crearMensajeDesdeResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener mensaje por ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Obtiene estadísticas del sistema de moderación
     * @return Mapa con las estadísticas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT estado, COUNT(*) as cantidad FROM mensajes GROUP BY estado";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int totalMensajes = 0;
            while (rs.next()) {
                String estado = rs.getString("estado");
                int cantidad = rs.getInt("cantidad");
                stats.put(estado.toLowerCase(), cantidad);
                totalMensajes += cantidad;
            }
            
            stats.put("total", totalMensajes);
            
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
}

