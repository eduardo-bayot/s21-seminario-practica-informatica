package com.moderacion;

/**
 * Enum que representa los posibles estados de un mensaje
 */
public enum EstadoMensaje {
    NUEVO("Nuevo"),
    PENDIENTE("Pendiente"),
    EN_MODERACION("En Moderación"),
    BLOQUEADO("Bloqueado"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    SOSPECHOSO("Sospechoso");
    
    private final String descripcion;
    
    EstadoMensaje(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}

