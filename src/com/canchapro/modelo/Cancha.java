package com.canchapro.modelo;

import java.time.LocalTime;

public class Cancha {
    private String codigo;
    private String nombre;
    private String tipo;
    private int capacidad;
    private double precioBase;
    private EstadoCancha estado;
    private LocalTime horaApertura;
    private LocalTime horaCierre;

    public Cancha(String codigo, String nombre, String tipo, int capacidad, double precioBase,
                  EstadoCancha estado, LocalTime horaApertura, LocalTime horaCierre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioBase = precioBase;
        this.estado = estado;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
    }

    public boolean estaOperativa() {
        return this.estado == EstadoCancha.DISPONIBLE;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public double getPrecioBase() { return precioBase; }
    public void setPrecioBase(double precioBase) { this.precioBase = precioBase; }
    public EstadoCancha getEstado() { return estado; }
    public void setEstado(EstadoCancha estado) { this.estado = estado; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + tipo + ")";
    }
}
