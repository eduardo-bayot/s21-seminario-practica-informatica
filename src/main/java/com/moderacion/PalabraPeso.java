package com.moderacion;

/**
 * Representa una palabra con peso asociado para el algoritmo de moderación
 */
public class PalabraPeso {
    private int id;
    private String palabra;
    private double peso;
    private String contexto; // opcional: indica el contexto de uso
    
    public PalabraPeso(int id, String palabra, double peso, String contexto) {
        this.id = id;
        this.palabra = palabra;
        this.peso = peso;
        this.contexto = contexto;
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
    
    public double getPeso() {
        return peso;
    }
    
    public void setPeso(double peso) {
        this.peso = peso;
    }
    
    public String getContexto() {
        return contexto;
    }
    
    public void setContexto(String contexto) {
        this.contexto = contexto;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PalabraPeso that = (PalabraPeso) obj;
        return palabra.equalsIgnoreCase(that.palabra);
    }
    
    @Override
    public int hashCode() {
        return palabra.toLowerCase().hashCode();
    }
}

