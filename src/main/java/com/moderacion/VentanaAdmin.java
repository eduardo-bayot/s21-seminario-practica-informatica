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
                
                // Colorear según el estado
                if (msg.getEstado() == EstadoMensaje.BLOQUEADO) {
                    setBackground(isSelected ? new Color(255, 200, 200) : new Color(255, 240, 240));
                    setForeground(Color.RED);
                } else if (msg.getEstado() == EstadoMensaje.SOSPECHOSO) {
                    setBackground(isSelected ? new Color(255, 255, 200) : new Color(255, 255, 230));
                    setForeground(Color.ORANGE.darker());
                } else if (msg.getEstado() == EstadoMensaje.APROBADO) {
                    setBackground(isSelected ? new Color(200, 255, 200) : new Color(240, 255, 240));
                }
            }
            
            return this;
        }
    }
}

