package com.canchapro.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private String numero;
    private LocalDateTime fechaHora;
    private String codigoReservaOpcional; // Puede ser vacío o null
    private MetodoPago metodoPago;
    private List<DetalleVenta> detalles;

    public Venta(String numero, LocalDateTime fechaHora, String codigoReservaOpcional, MetodoPago metodoPago) {
        this.numero = numero;
        this.fechaHora = fechaHora;
        this.codigoReservaOpcional = (codigoReservaOpcional == null) ? "" : codigoReservaOpcional.trim();
        this.metodoPago = metodoPago;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
    }

    public double getTotal() {
        double total = 0;
        for (DetalleVenta d : detalles) {
            total += d.getSubtotal();
        }
        return total;
    }

    public String getNumero() { return numero; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getCodigoReservaOpcional() { return codigoReservaOpcional; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public List<DetalleVenta> getDetalles() { return detalles; }
}
