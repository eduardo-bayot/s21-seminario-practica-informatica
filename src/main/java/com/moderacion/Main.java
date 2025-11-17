package com.moderacion;

import javax.swing.SwingUtilities;

/**
 * Clase principal que inicia la aplicación de moderación
 * Inicia el servidor REST API y la interfaz gráfica
 */
public class Main {
    
    private static ServicioWebREST servicioWeb;
    
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
        // Iniciar el servicio web REST en un hilo separado
        new Thread(() -> {
            servicioWeb = new ServicioWebREST();
            servicioWeb.iniciar();
        }).start();
        
        // Esperar un momento para que el servidor inicie
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Iniciar la interfaz gráfica
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("💻 Iniciando interfaz gráfica de administración...");
                VentanaAdmin ventana = new VentanaAdmin();
                ventana.setVisible(true);
                
                // Configurar shutdown hook para detener el servidor
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (servicioWeb != null) {
                        servicioWeb.detener();
                    }
                }));
            }
        });
        
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE MODERACIÓN ESCALONADA - INICIADO          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("  Panel de administración: ABIERTO");
        System.out.println("  API REST: http://localhost:7000");
        System.out.println("  Para pruebas: Menú 'Herramientas' > 'Ventana de Prueba'");
        System.out.println("═══════════════════════════════════════════════════════════\n");
    }
    
    /**
     * Obtiene la instancia del servicio web REST
     */
    public static ServicioWebREST getServicioWeb() {
        return servicioWeb;
    }
}

