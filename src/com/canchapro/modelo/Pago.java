package com.canchapro.modelo;

import java.time.LocalDateTime;

public class Pago {
    private String codigoPago;
    private String codigoReserva;
    private LocalDateTime fechaHora;
    private double monto;
    private MetodoPago metodo;
    private String observacion;

    public Pago(String codigoPago, String codigoReserva, LocalDateTime fechaHora, double monto, MetodoPago metodo, String observacion) {
        this.codigoPago = codigoPago;
        this.codigoReserva = codigoReserva;
        this.fechaHora = fechaHora;
        this.monto = monto;
        this.metodo = metodo;
        this.observacion = observacion == null ? "" : observacion;
    }

    public String getCodigoPago() { return codigoPago; }
    public String getCodigoReserva() { return codigoReserva; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public double getMonto() { return monto; }
    public MetodoPago getMetodo() { return metodo; }
    public String getObservacion() { return observacion; }
}
