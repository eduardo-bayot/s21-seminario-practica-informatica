package com.moderacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}

