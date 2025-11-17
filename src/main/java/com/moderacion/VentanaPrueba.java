package com.moderacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Ventana de prueba para enviar mensajes al sistema de moderación
 * Consume el API REST y muestra los resultados
 */
public class VentanaPrueba extends JFrame {
    
    private JTextArea areaMensaje;
    private JTextArea areaResultado;
    private JButton btnEnviar;
    private JButton btnLimpiar;
    private JLabel labelEstado;
    private final String API_URL = "http://localhost:7000/api/mensajes";
    private final Gson gson;
    
    public VentanaPrueba() {
        this.gson = new Gson();
        inicializarComponentes();
    }
    
    private void inicializarComponentes() {
        setTitle("Ventana de Prueba - Sistema de Moderación");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal con padding
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Panel superior - Título e instrucciones
        JPanel panelTitulo = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Prueba de Integración - API de Moderación");
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panelTitulo.add(lblTitulo, BorderLayout.NORTH);
        
        JLabel lblInstrucciones = new JLabel("<html><i>Escribe un mensaje para probarlo en el sistema de moderación automática</i></html>");
        lblInstrucciones.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblInstrucciones.setForeground(Color.GRAY);
        lblInstrucciones.setBorder(new EmptyBorder(5, 0, 10, 0));
        panelTitulo.add(lblInstrucciones, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelTitulo, BorderLayout.NORTH);
        
        // Panel central - Área de mensaje
        JPanel panelMensaje = new JPanel(new BorderLayout(5, 5));
        panelMensaje.setBorder(BorderFactory.createTitledBorder("Mensaje a Enviar"));
        
        areaMensaje = new JTextArea(5, 40);
        areaMensaje.setLineWrap(true);
        areaMensaje.setWrapStyleWord(true);
        areaMensaje.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaMensaje.setText("Hola, este es un mensaje de prueba");
        
        JScrollPane scrollMensaje = new JScrollPane(areaMensaje);
        panelMensaje.add(scrollMensaje, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        btnEnviar = new JButton("Enviar al Sistema");
        btnEnviar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btnEnviar.setBackground(new Color(33, 150, 243));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);
        btnEnviar.addActionListener(e -> enviarMensaje());
        
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnLimpiar.addActionListener(e -> limpiarCampos());
        
        panelBotones.add(btnEnviar);
        panelBotones.add(btnLimpiar);
        
        panelMensaje.add(panelBotones, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelMensaje, BorderLayout.CENTER);
        
        // Panel inferior - Resultado
        JPanel panelResultado = new JPanel(new BorderLayout(5, 5));
        panelResultado.setBorder(BorderFactory.createTitledBorder("Respuesta del Sistema"));
        
        labelEstado = new JLabel(" ");
        labelEstado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        labelEstado.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelResultado.add(labelEstado, BorderLayout.NORTH);
        
        areaResultado = new JTextArea(12, 40);
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        areaResultado.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollResultado = new JScrollPane(areaResultado);
        panelResultado.add(scrollResultado, BorderLayout.CENTER);
        
        panelPrincipal.add(panelResultado, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        // Ejemplos rápidos
        JPanel panelEjemplos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEjemplos.setBorder(BorderFactory.createTitledBorder("Ejemplos Rápidos"));
        
        JButton btnEjemplo1 = new JButton("Mensaje normal");
        btnEjemplo1.addActionListener(e -> areaMensaje.setText("Hola a todos, ¿cómo están hoy?"));
        
        JButton btnEjemplo2 = new JButton("Con mayúsculas");
        btnEjemplo2.addActionListener(e -> areaMensaje.setText("HOLA ESTO ES UN MENSAJE EN MAYÚSCULAS!!!"));
        
        JButton btnEjemplo3 = new JButton("Con email");
        btnEjemplo3.addActionListener(e -> areaMensaje.setText("Mi correo es test@ejemplo.com si quieres contactarme"));
        
        JButton btnEjemplo4 = new JButton("Múltiples problemas");
        btnEjemplo4.addActionListener(e -> areaMensaje.setText(
            "HOLA!!! MI CORREO ES test@ejemplo.com Y MI TELÉFONO ES +54-351-123-4567!!!! URGENTE!!!! spam spam spam spam"
        ));
        
        panelEjemplos.add(btnEjemplo1);
        panelEjemplos.add(btnEjemplo2);
        panelEjemplos.add(btnEjemplo3);
        panelEjemplos.add(btnEjemplo4);
        
        add(panelEjemplos, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 600));
    }
    
    /**
     * Envía el mensaje al API REST y muestra el resultado
     */
    private void enviarMensaje() {
        String contenido = areaMensaje.getText().trim();
        
        if (contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor escribe un mensaje primero", 
                "Campo vacío", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        btnEnviar.setEnabled(false);
        labelEstado.setText("Enviando mensaje al sistema...");
        areaResultado.setText("Conectando con el API REST...\n");
        
        // Ejecutar en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Crear JSON del request
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("contenido", contenido);
                requestJson.addProperty("usuario_id", 1);
                
                String jsonInputString = gson.toJson(requestJson);
                
                // Hacer POST request
                URL url = new URL(API_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                
                // Enviar el JSON
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // Leer la respuesta
                int responseCode = con.getResponseCode();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
                );
                
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parsear respuesta JSON
                JsonObject respuesta = gson.fromJson(response.toString(), JsonObject.class);
                
                // Actualizar UI en el thread de Swing
                SwingUtilities.invokeLater(() -> mostrarResultado(respuesta, responseCode));
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> mostrarError(e));
            } finally {
                SwingUtilities.invokeLater(() -> btnEnviar.setEnabled(true));
            }
        }).start();
    }
    
    /**
     * Muestra el resultado del API en la interfaz
     */
    private void mostrarResultado(JsonObject respuesta, int responseCode) {
        StringBuilder resultado = new StringBuilder();
        
        resultado.append("═══════════════════════════════════════════\n");
        resultado.append("  RESPUESTA DEL SISTEMA DE MODERACIÓN\n");
        resultado.append("═══════════════════════════════════════════\n\n");
        
        resultado.append("Código HTTP: ").append(responseCode).append("\n\n");
        
        if (respuesta.has("success") && respuesta.get("success").getAsBoolean()) {
            String estado = respuesta.get("estado").getAsString();
            String estadoDesc = respuesta.get("estado_descripcion").getAsString();
            String mensaje = respuesta.get("mensaje").getAsString();
            int mensajeId = respuesta.get("mensaje_id").getAsInt();
            int totalRazones = respuesta.get("total_razones").getAsInt();
            
            resultado.append("SUCCESS: Mensaje procesado correctamente\n\n");
            resultado.append("ID del Mensaje: ").append(mensajeId).append("\n");
            resultado.append("Contenido: ").append(respuesta.get("contenido").getAsString()).append("\n");
            resultado.append("Estado: ").append(estado).append(" (").append(estadoDesc).append(")\n");
            resultado.append("Fecha: ").append(respuesta.get("fecha_creacion").getAsString()).append("\n");
            resultado.append("Total de razones: ").append(totalRazones).append("\n\n");
            resultado.append("Mensaje: ").append(mensaje).append("\n\n");
            
            if (totalRazones > 0) {
                resultado.append(" RAZONES DE BLOQUEO DETECTADAS:\n");
                resultado.append("───────────────────────────────────────────\n");
                var razones = respuesta.getAsJsonArray("razones_bloqueo");
                for (int i = 0; i < razones.size(); i++) {
                    resultado.append(String.format("%d. %s\n", i + 1, razones.get(i).getAsString()));
                }
            } else {
                resultado.append("✅ No se detectaron problemas en el mensaje\n");
            }
            
            // Actualizar label de estado con color
            if (estado.equals("BLOQUEADO")) {
                labelEstado.setText("MENSAJE BLOQUEADO");
                labelEstado.setForeground(Color.RED);
            } else if (estado.equals("SOSPECHOSO")) {
                labelEstado.setText("MENSAJE SOSPECHOSO");
                labelEstado.setForeground(Color.ORANGE.darker());
            } else {
                labelEstado.setText("MENSAJE APROBADO");
                labelEstado.setForeground(new Color(0, 150, 0));
            }
            
        } else {
            resultado.append("ERROR: ").append(respuesta.get("error").getAsString()).append("\n");
            labelEstado.setText("Error en el procesamiento");
            labelEstado.setForeground(Color.RED);
        }
        
        resultado.append("\n═══════════════════════════════════════════\n");
        
        areaResultado.setText(resultado.toString());
        areaResultado.setCaretPosition(0);
    }
    
    /**
     * Muestra un error de conexión
     */
    private void mostrarError(Exception e) {
        String mensaje = "ERROR DE CONEXIÓN\n\n" +
                        "No se pudo conectar con el API REST.\n" +
                        "Verifica que el servicio esté corriendo en http://localhost:7000\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Para iniciar el servicio, ejecuta Main.java";
        
        areaResultado.setText(mensaje);
        labelEstado.setText("Error de conexión");
        labelEstado.setForeground(Color.RED);
        
        JOptionPane.showMessageDialog(this,
            "No se pudo conectar con el API REST.\n" +
            "Asegúrate de que el servicio esté corriendo.",
            "Error de conexión",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Limpia los campos de la ventana
     */
    private void limpiarCampos() {
        areaMensaje.setText("");
        areaResultado.setText("");
        labelEstado.setText(" ");
        areaMensaje.requestFocus();
    }
}

