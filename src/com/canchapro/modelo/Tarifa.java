package com.canchapro.modelo;

import java.time.LocalTime;

public class Tarifa {
    private String codigo;
    private String descripcion;
    private String tipoDia; // LUNES_VIERNES o FIN_SEMANA
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private double precioPorHora;

    public Tarifa(String codigo, String descripcion, String tipoDia, LocalTime horaInicio, LocalTime horaFin, double precioPorHora) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.tipoDia = tipoDia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.precioPorHora = precioPorHora;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getTipoDia() { return tipoDia; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public double getPrecioPorHora() { return precioPorHora; }
    public void setPrecioPorHora(double precioPorHora) { this.precioPorHora = precioPorHora; }
}
