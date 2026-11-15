package com.canchapro.modelo;

public class ServicioAdicional {
    private String codigo;
    private String nombre;
    private double precio;
    private boolean activo;

    public ServicioAdicional(String codigo, String nombre, double precio, boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.activo = activo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " (Q" + String.format("%.2f", precio) + ")";
    }
}
