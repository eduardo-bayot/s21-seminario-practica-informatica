package com.moderacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para acceder a la tabla de palabras prohibidas
 */
public class PalabraProhibidaDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/moderacion_db";
    private static final String USER = "moderacion_user";
    private static final String PASSWORD = "moderacion_pass";
    
    /**
     * Obtiene todas las palabras prohibidas
     */
    public List<PalabraProhibida> obtenerTodas() {
        List<PalabraProhibida> palabras = new ArrayList<>();
        String sql = "SELECT id, palabra, razon, severidad FROM palabras_prohibidas";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                PalabraProhibida pp = new PalabraProhibida(
                    rs.getInt("id"),
                    rs.getString("palabra"),
                    rs.getString("razon"),
                    rs.getInt("severidad")
                );
                palabras.add(pp);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener palabras prohibidas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return palabras;
    }
    
    /**
     * Busca si alguna palabra del mensaje es prohibida
     */
    public PalabraProhibida buscarPalabraProhibida(String palabra) {
        String sql = "SELECT id, palabra, razon, severidad FROM palabras_prohibidas " +
                     "WHERE LOWER(?) LIKE CONCAT('%', LOWER(palabra), '%')";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, palabra);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PalabraProhibida(
                        rs.getInt("id"),
                        rs.getString("palabra"),
                        rs.getString("razon"),
                        rs.getInt("severidad")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar palabra prohibida: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Verifica si el contenido contiene palabras prohibidas
     */
    public boolean contienePalabraProhibida(String contenido) {
        String[] palabras = contenido.toLowerCase().split("\\s+");
        
        for (String palabra : palabras) {
            // Limpiar puntuación
            palabra = palabra.replaceAll("[^a-z]", "");
            if (!palabra.isEmpty()) {
                if (buscarPalabraProhibida(palabra) != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Obtiene la primera palabra prohibida encontrada en un texto
     */
    public PalabraProhibida obtenerPrimeraPalabraProhibida(String contenido) {
        String[] palabras = contenido.toLowerCase().split("\\s+");
        
        for (String palabra : palabras) {
            palabra = palabra.replaceAll("[^a-z]", "");
            if (!palabra.isEmpty()) {
                PalabraProhibida pp = buscarPalabraProhibida(palabra);
                if (pp != null) {
                    return pp;
                }
            }
        }
        return null;
    }
}

