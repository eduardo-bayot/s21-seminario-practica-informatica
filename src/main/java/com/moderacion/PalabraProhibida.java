package com.moderacion;

/**
 * Representa una palabra prohibida en el sistema
 */
public class PalabraProhibida {
    private int id;
    private String palabra;
    private String razon;
    private int severidad; // 1-5, donde 5 es más severa
    
    public PalabraProhibida(int id, String palabra, String razon, int severidad) {
        this.id = id;
        this.palabra = palabra;
        this.razon = razon;
        this.severidad = severidad;
    }
    
    // Getters y setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getPalabra() {
        return palabra;
    }
    
    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }
    
    public String getRazon() {
        return razon;
    }
    
    public void setRazon(String razon) {
        this.razon = razon;
    }
    
    public int getSeveridad() {
        return severidad;
    }
    
    public void setSeveridad(int severidad) {
        this.severidad = severidad;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PalabraProhibida that = (PalabraProhibida) obj;
        return palabra.equalsIgnoreCase(that.palabra);
    }
    
    @Override
    public int hashCode() {
        return palabra.toLowerCase().hashCode();
    }
}

