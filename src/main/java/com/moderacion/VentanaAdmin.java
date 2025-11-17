package com.moderacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;

/**
 * Ventana principal de administración para la moderación de mensajes
 */
public class VentanaAdmin extends JFrame {
    private JList<Mensaje> listaMensajes;
    private DefaultListModel<Mensaje> modeloLista;
    private JTextArea areaContenido;
    private JLabel labelEstado;
    private JTextArea areaRazones;
    private MensajeDAO mensajeDAO;
    private ServicioValidacion servicioValidacion;
    
    public VentanaAdmin() {
        mensajeDAO = new MensajeDAO();
        servicioValidacion = new ServicioValidacion(mensajeDAO);
        inicializarComponentes();
        cargarMensajesPendientes();
    }
    
    /**
     * Inicializa los componentes de la interfaz gráfica
     */
    private void inicializarComponentes() {
        setTitle("Panel de Administración - Moderación de Mensajes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Crear barra de menú
        crearMenuBar();
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel izquierdo - Lista de mensajes
        JPanel panelLista = new JPanel(new BorderLayout());
        panelLista.setBorder(BorderFactory.createTitledBorder("Mensajes Pendientes"));
        
        modeloLista = new DefaultListModel<>();
        listaMensajes = new JList<>(modeloLista);
        listaMensajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaMensajes.setCellRenderer(new MensajeListRenderer());
        
        JScrollPane scrollLista = new JScrollPane(listaMensajes);
        scrollLista.setPreferredSize(new Dimension(300, 400));
        
        panelLista.add(scrollLista, BorderLayout.CENTER);
        
        // Panel derecho - Detalles
        JPanel panelDetalles = new JPanel(new BorderLayout());
        panelDetalles.setBorder(BorderFactory.createTitledBorder("Detalles del Mensaje"));
        
        // Área de contenido
        areaContenido = new JTextArea();
        areaContenido.setEditable(false);
        areaContenido.setWrapStyleWord(true);
        areaContenido.setLineWrap(true);
        areaContenido.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        JScrollPane scrollContenido = new JScrollPane(areaContenido);
        scrollContenido.setPreferredSize(new Dimension(400, 300));
        
        panelDetalles.add(scrollContenido, BorderLayout.CENTER);
        
        // Label de estado
        labelEstado = new JLabel("Estado: -");
        labelEstado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        labelEstado.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelDetalles.add(labelEstado, BorderLayout.NORTH);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        JButton btnAprobar = new JButton("✓ Aprobar");
        btnAprobar.setBackground(new Color(76, 175, 80));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnAprobar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aprobarMensajeSeleccionado();
            }
        });
        
        JButton btnRechazar = new JButton("✗ Rechazar");
        btnRechazar.setBackground(new Color(244, 67, 54));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnRechazar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rechazarMensajeSeleccionado();
            }
        });
        
        JButton btnActualizar = new JButton("↻ Actualizar Lista");
        btnActualizar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarMensajesPendientes();
            }
        });
        
        JButton btnValidar = new JButton("✓ Validar Automáticamente");
        btnValidar.setBackground(new Color(33, 150, 243));
        btnValidar.setForeground(Color.WHITE);
        btnValidar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnValidar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarMensajeSeleccionado();
            }
        });
        
        panelBotones.add(btnAprobar);
        panelBotones.add(btnRechazar);
        panelBotones.add(btnValidar);
        panelBotones.add(btnActualizar);
        
        // Crear panel para botones y razones
        JPanel panelBotonesRazones = new JPanel(new BorderLayout());
        panelBotonesRazones.add(panelBotones, BorderLayout.NORTH);
        
        JPanel panelRazones = new JPanel(new BorderLayout(5, 5));
        panelRazones.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel labelRazones = new JLabel("Razones de bloqueo:");
        labelRazones.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panelRazones.add(labelRazones, BorderLayout.NORTH);
        areaRazones = new JTextArea(3, 20);
        areaRazones.setEditable(false);
        areaRazones.setWrapStyleWord(true);
        areaRazones.setLineWrap(true);
        areaRazones.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        areaRazones.setBackground(new Color(255, 245, 245));
        JScrollPane scrollRazones = new JScrollPane(areaRazones);
        scrollRazones.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelRazones.add(scrollRazones, BorderLayout.CENTER);
        
        panelBotonesRazones.add(panelRazones, BorderLayout.CENTER);
        panelDetalles.add(panelBotonesRazones, BorderLayout.SOUTH);
        
        
        // Configurar layout principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                              panelLista, panelDetalles);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.3);
        
        panelPrincipal.add(splitPane, BorderLayout.CENTER);
        
        
        // Listener para selección de mensajes
        listaMensajes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalleMensaje();
            }
        });
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Carga los mensajes pendientes desde la base de datos
     */
    private void cargarMensajesPendientes() {
        List<Mensaje> mensajes = mensajeDAO.obtenerMensajesPendientes();
        modeloLista.clear();
        
        for (Mensaje msg : mensajes) {
            modeloLista.addElement(msg);
        }
        
        JOptionPane.showMessageDialog(this, 
            "Cargados " + mensajes.size() + " mensajes pendientes.",
            "Información", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Muestra el detalle del mensaje seleccionado
     */
    private void mostrarDetalleMensaje() {
        Mensaje seleccionado = listaMensajes.getSelectedValue();
        if (seleccionado != null) {
            areaContenido.setText(seleccionado.getContenido());
            labelEstado.setText("Estado: " + seleccionado.getEstado().getDescripcion());
            
            // Mostrar razones de bloqueo
            if (!seleccionado.getRazonesBloqueo().isEmpty()) {
                String razonesTexto = String.join("\n- ", seleccionado.getRazonesBloqueo());
                areaRazones.setText("- " + razonesTexto);
                areaRazones.setForeground(new Color(200, 0, 0));
            } else {
                areaRazones.setText("Sin razones de bloqueo");
                areaRazones.setForeground(new Color(100, 100, 100));
            }
        } else {
            areaContenido.setText("");
            labelEstado.setText("Estado: -");
            areaRazones.setText("");
        }
    }
    
    /**
     * Aprueba el mensaje actualmente seleccionado
     */
    private void aprobarMensajeSeleccionado() {
        Mensaje seleccionado = listaMensajes.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un mensaje primero.",
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Desea aprobar este mensaje?",
            "Confirmar aprobación",
            JOptionPane.YES_NO_OPTION);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            if (mensajeDAO.aprobarMensaje(seleccionado.getId())) {
                JOptionPane.showMessageDialog(this,
                    "Mensaje aprobado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarMensajesPendientes();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al aprobar el mensaje.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Rechaza el mensaje actualmente seleccionado
     */
    private void rechazarMensajeSeleccionado() {
        Mensaje seleccionado = listaMensajes.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un mensaje primero.",
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Desea rechazar este mensaje?",
            "Confirmar rechazo",
            JOptionPane.YES_NO_OPTION);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            if (mensajeDAO.rechazarMensaje(seleccionado.getId())) {
                JOptionPane.showMessageDialog(this,
                    "Mensaje rechazado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarMensajesPendientes();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al rechazar el mensaje.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Valida automáticamente el mensaje seleccionado
     */
    private void validarMensajeSeleccionado() {
        Mensaje seleccionado = listaMensajes.getSelectedValue();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un mensaje primero.",
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Ejecutar validaciones
        List<String> razones = servicioValidacion.validarMensaje(seleccionado);
        
        if (!razones.isEmpty()) {
            // Actualizar el estado del mensaje
            seleccionado.setRazonesBloqueo(razones);
            EstadoMensaje nuevoEstado = servicioValidacion.determinarEstado(razones);
            seleccionado.setEstado(nuevoEstado);
            
            // Actualizar en la base de datos
            actualizarMensajeEnBD(seleccionado);
            
            // Mostrar resultados
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Validación completada.\n\n");
            mensaje.append("Estado: ").append(nuevoEstado.getDescripcion()).append("\n");
            mensaje.append("Razones encontradas: ").append(razones.size()).append("\n\n");
            for (String razon : razones) {
                mensaje.append("- ").append(razon).append("\n");
            }
            
            JOptionPane.showMessageDialog(this,
                mensaje.toString(),
                "Resultado de Validación",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Actualizar la vista
            mostrarDetalleMensaje();
            // Recargar la lista para reflejar cambios
            cargarMensajesPendientes();
        } else {
            JOptionPane.showMessageDialog(this,
                "El mensaje no presenta problemas detectables por las validaciones automáticas.",
                "Validación OK",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Actualiza el mensaje en la base de datos con su estado y razones
     */
    private void actualizarMensajeEnBD(Mensaje mensaje) {
        String URL = "jdbc:mariadb://localhost:3306/moderacion_db";
        String USER = "moderacion_user";
        String PASSWORD = "moderacion_pass";
        String sql = "UPDATE mensajes SET estado = ?, razones_bloqueo = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, mensaje.getEstado().name());
            String razonesTexto = String.join(";", mensaje.getRazonesBloqueo());
            stmt.setString(2, razonesTexto);
            stmt.setInt(3, mensaje.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crea la barra de menú
     */
    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menú Herramientas
        JMenu menuHerramientas = new JMenu("Herramientas");
        
        JMenuItem itemVentanaPrueba = new JMenuItem("🧪 Ventana de Prueba");
        itemVentanaPrueba.addActionListener(e -> abrirVentanaPrueba());
        
        JMenuItem itemEstadisticas = new JMenuItem("Ver Estadísticas");
        itemEstadisticas.addActionListener(e -> mostrarEstadisticas());
        
        menuHerramientas.add(itemVentanaPrueba);
        menuHerramientas.addSeparator();
        menuHerramientas.add(itemEstadisticas);
        
        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        
        JMenuItem itemAcerca = new JMenuItem("ℹAcerca de");
        itemAcerca.addActionListener(e -> mostrarAcercaDe());
        
        menuAyuda.add(itemAcerca);
        
        menuBar.add(menuHerramientas);
        menuBar.add(menuAyuda);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Abre la ventana de prueba
     */
    private void abrirVentanaPrueba() {
        VentanaPrueba ventanaPrueba = new VentanaPrueba();
        ventanaPrueba.setVisible(true);
    }
    
    /**
     * Muestra estadísticas del sistema
     */
    private void mostrarEstadisticas() {
        try {
            java.util.Map<String, Object> stats = mensajeDAO.obtenerEstadisticas();
            
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("ESTADÍSTICAS DEL SISTEMA\n\n");
            mensaje.append("═══════════════════════════════════\n\n");
            
            int total = (int) stats.getOrDefault("total", 0);
            mensaje.append("Total de mensajes: ").append(total).append("\n\n");
            
            mensaje.append("Por estado:\n");
            mensaje.append("  • Nuevos: ").append(stats.getOrDefault("nuevo", 0)).append("\n");
            mensaje.append("  • Pendientes: ").append(stats.getOrDefault("pendiente", 0)).append("\n");
            mensaje.append("  • Sospechosos: ").append(stats.getOrDefault("sospechoso", 0)).append("\n");
            mensaje.append("  • Bloqueados: ").append(stats.getOrDefault("bloqueado", 0)).append("\n");
            mensaje.append("  • Aprobados: ").append(stats.getOrDefault("aprobado", 0)).append("\n");
            mensaje.append("  • Rechazados: ").append(stats.getOrDefault("rechazado", 0)).append("\n");
            
            JOptionPane.showMessageDialog(this,
                mensaje.toString(),
                "Estadísticas del Sistema",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al obtener estadísticas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Muestra información acerca del sistema
     */
    private void mostrarAcercaDe() {
        String mensaje = "Sistema de Moderación Escalonada y Eficaz\n\n" +
            "Versión: 1.0.0\n" +
            "Autor: Eduardo Agustin Bayot\n\n" +
            "Universidad Siglo 21\n" +
            "Seminario de Práctica de Informática\n" +
            "Trabajo Práctico 4 - 2025\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Características:\n" +
            "• 8 algoritmos de validación automática\n" +
            "• API REST (http://localhost:7000)\n" +
            "• Interfaz gráfica con Java Swing\n" +
            "• Persistencia en MySQL con JDBC\n" +
            "• Ventana de prueba integrada\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Tecnologías:\n" +
            "• Java 11+\n" +
            "• Javalin (REST API)\n" +
            "• MySQL/MariaDB\n" +
            "• Java Swing";
        
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Acerca del Sistema",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Renderizador personalizado para los mensajes en la lista
     */
    class MensajeListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, 
                Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Mensaje) {
                Mensaje msg = (Mensaje) value;
                setText(msg.toString());
                
                // Colores de fondo y texto según el estado
                Color bgColor = Color.WHITE;
                Color fgColor = Color.BLACK;
                
                switch (msg.getEstado()) {
                    case BLOQUEADO:
                        bgColor = isSelected ? new Color(255, 180, 180) : new Color(255, 220, 220);
                        fgColor = new Color(139, 0, 0); // Rojo oscuro
                        break;
                    case SOSPECHOSO:
                        bgColor = isSelected ? new Color(255, 235, 150) : new Color(255, 245, 200);
                        fgColor = new Color(139, 90, 0); // Naranja oscuro
                        break;
                    case APROBADO:
                        bgColor = isSelected ? new Color(180, 255, 180) : new Color(220, 255, 220);
                        fgColor = new Color(0, 100, 0); // Verde oscuro
                        break;
                    case RECHAZADO:
                        bgColor = isSelected ? new Color(220, 200, 200) : new Color(240, 230, 230);
                        fgColor = new Color(100, 50, 50); // Marrón rojizo
                        break;
                    case PENDIENTE:
                        bgColor = isSelected ? new Color(200, 220, 255) : new Color(230, 240, 255);
                        fgColor = new Color(0, 51, 102); // Azul oscuro
                        break;
                    case NUEVO:
                        bgColor = isSelected ? new Color(230, 230, 230) : new Color(250, 250, 250);
                        fgColor = Color.BLACK;
                        break;
                }
                
                setBackground(bgColor);
                setForeground(fgColor);
                setOpaque(true);
            }
            
            return this;
        }
    }
}

