package com.moderacion;

import javax.swing.SwingUtilities;

/**
 * Clase principal que inicia la aplicación de moderación
 */
public class Main {
    static {
        // Cargar el driver de MariaDB
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de MariaDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VentanaAdmin().setVisible(true);
            }
        });
    }
}

