package com.canchapro.modelo;

public class DetalleVenta {
    private String numeroVenta;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public DetalleVenta(String numeroVenta, Producto producto, int cantidad, double precioUnitario) {
        this.numeroVenta = numeroVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public String getNumeroVenta() { return numeroVenta; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
}
