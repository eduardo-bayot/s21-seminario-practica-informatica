package com.moderacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un mensaje en el sistema
 */
public class Mensaje {
    private int id;
    private String contenido;
    private EstadoMensaje estado;
    private LocalDateTime fechaCreacion;
    private int usuarioId;
    private List<String> razonesBloqueo;
    
    /**
     * Constructor de la clase Mensaje
     */
    public Mensaje(int id, String contenido, EstadoMensaje estado, 
                   LocalDateTime fechaCreacion, int usuarioId) {
        this.id = id;
        this.contenido = contenido;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.usuarioId = usuarioId;
        this.razonesBloqueo = new ArrayList<>();
    }
    
    public Mensaje(int id, String contenido, String estado, 
                   LocalDateTime fechaCreacion, int usuarioId) {
        this.id = id;
        this.contenido = contenido;
        this.estado = parsearEstado(estado);
        this.fechaCreacion = fechaCreacion;
        this.usuarioId = usuarioId;
        this.razonesBloqueo = new ArrayList<>();
    }
    
    private EstadoMensaje parsearEstado(String estadoStr) {
        try {
            return EstadoMensaje.valueOf(estadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EstadoMensaje.PENDIENTE;
        }
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public EstadoMensaje getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoMensaje estado) {
        this.estado = estado;
    }
    
    public void setEstado(String estado) {
        this.estado = parsearEstado(estado);
    }
    
    public List<String> getRazonesBloqueo() {
        return razonesBloqueo;
    }
    
    public void setRazonesBloqueo(List<String> razones) {
        this.razonesBloqueo = razones != null ? razones : new ArrayList<>();
    }
    
    public void agregarRazonBloqueo(String razon) {
        this.razonesBloqueo.add(razon);
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    @Override
    public String toString() {
        String preview = contenido.length() > 50 
                         ? contenido.substring(0, 50) + "..." 
                         : contenido;
        String razones = razonesBloqueo.isEmpty() ? "" : " (" + razonesBloqueo.size() + " razones)";
        return "Mensaje #" + id + " [" + estado.getDescripcion() + "]: " + preview + razones;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Mensaje mensaje = (Mensaje) obj;
        return id == mensaje.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
}

