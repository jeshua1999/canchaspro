package com.canchapro.modelo;

import java.time.LocalDate;

public class Cliente {
    private String codigo;
    private String nombre;
    private String telefono;
    private String correo;
    private LocalDate fechaRegistro;

    public Cliente(String codigo, String nombre, String telefono, String correo, LocalDate fechaRegistro) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo == null ? "" : correo;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDate.now();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + telefono + ")";
    }
}
