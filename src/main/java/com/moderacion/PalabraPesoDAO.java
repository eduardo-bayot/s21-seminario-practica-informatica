package com.moderacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para acceder a la tabla de palabras con peso
 */
public class PalabraPesoDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/moderacion_db";
    private static final String USER = "moderacion_user";
    private static final String PASSWORD = "moderacion_pass";
    
    /**
     * Obtiene el peso de una palabra
     */
    public Optional<PalabraPeso> obtenerPesoPalabra(String palabra) {
        String sql = "SELECT id, palabra, peso, contexto FROM palabras_peso " +
                     "WHERE LOWER(palabra) = LOWER(?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, palabra);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PalabraPeso pp = new PalabraPeso(
                        rs.getInt("id"),
                        rs.getString("palabra"),
                        rs.getDouble("peso"),
                        rs.getString("contexto")
                    );
                    return Optional.of(pp);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener peso de palabra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene todas las palabras con peso
     */
    public List<PalabraPeso> obtenerTodas() {
        List<PalabraPeso> palabras = new ArrayList<>();
        String sql = "SELECT id, palabra, peso, contexto FROM palabras_peso ORDER BY peso DESC";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                PalabraPeso pp = new PalabraPeso(
                    rs.getInt("id"),
                    rs.getString("palabra"),
                    rs.getDouble("peso"),
                    rs.getString("contexto")
                );
                palabras.add(pp);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener palabras con peso: " + e.getMessage());
            e.printStackTrace();
        }
        
        return palabras;
    }
    
    /**
     * Obtiene el peso de una palabra o devuelve un peso por defecto
     */
    public double obtenerPesoOPorDefecto(String palabra, double porDefecto) {
        Optional<PalabraPeso> pp = obtenerPesoPalabra(palabra);
        return pp.map(PalabraPeso::getPeso).orElse(porDefecto);
    }
}

