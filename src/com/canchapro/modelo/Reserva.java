package com.canchapro.modelo;

import com.canchapro.excepciones.ValidacionNegocioException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private String codigo;
    private Cliente cliente;
    private Cancha cancha;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private double costoCancha;
    private List<ServicioAdicional> servicios;
    private List<Pago> pagos;
    private EstadoReserva estado;
    private LocalDate fechaCancelacion;
    private String motivoCancelacion;
    private String operadorCancelacion;

    public Reserva(String codigo, Cliente cliente, Cancha cancha, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin, double costoCancha, EstadoReserva estado) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.cancha = cancha;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.costoCancha = costoCancha;
        this.servicios = new ArrayList<>();
        this.pagos = new ArrayList<>();
        this.estado = estado;
    }

    public void agregarServicio(ServicioAdicional servicio) {
        if (servicio != null) {
            this.servicios.add(servicio);
        }
    }

    public void agregarPago(Pago pago) throws ValidacionNegocioException {
        if (pago.getMonto() <= 0) {
            throw new ValidacionNegocioException("El monto del pago debe ser mayor a Q0.00.");
        }
        if (pago.getMonto() > getSaldoPendiente() + 0.001) {
            throw new ValidacionNegocioException(String.format("El pago (Q%.2f) excede el saldo pendiente (Q%.2f).", pago.getMonto(), getSaldoPendiente()));
        }
        this.pagos.add(pago);
        if (this.estado == EstadoReserva.PENDIENTE && getTotalPagado() > 0) {
            this.estado = EstadoReserva.CONFIRMADA;
        }
    }

    public void cargarPagoDesdeArchivo(Pago pago) {
        this.pagos.add(pago);
    }

    public double getTotalServicios() {
        double suma = 0;
        for (ServicioAdicional s : servicios) {
            suma += s.getPrecio();
        }
        return suma;
    }

    public double getTotal() {
        return costoCancha + getTotalServicios();
    }

    public double getTotalPagado() {
        double suma = 0;
        for (Pago p : pagos) {
            suma += p.getMonto();
        }
        return suma;
    }

    public double getSaldoPendiente() {
        double saldo = getTotal() - getTotalPagado();
        return Math.max(0.0, Math.round(saldo * 100.0) / 100.0);
    }

    public void cambiarEstado(EstadoReserva nuevoEstado) throws ValidacionNegocioException {
        if (this.estado == EstadoReserva.FINALIZADA || this.estado == EstadoReserva.CANCELADA) {
            throw new ValidacionNegocioException("No se puede modificar el estado de una reserva " + this.estado + ".");
        }
        if (nuevoEstado == EstadoReserva.PENDIENTE && this.estado != EstadoReserva.PENDIENTE) {
            throw new ValidacionNegocioException("Una reserva confirmada o en curso no puede regresar a PENDIENTE.");
        }
        if (nuevoEstado == EstadoReserva.CANCELADA) {
            throw new ValidacionNegocioException("Use el método cancelar() indicando motivo y operador.");
        }
        this.estado = nuevoEstado;
    }

    public void cancelar(String motivo, String operador) throws ValidacionNegocioException {
        if (this.estado == EstadoReserva.FINALIZADA || this.estado == EstadoReserva.CANCELADA) {
            throw new ValidacionNegocioException("No se puede cancelar una reserva que ya está " + this.estado + ".");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionNegocioException("Debe indicar un motivo de cancelación.");
        }
        this.estado = EstadoReserva.CANCELADA;
        this.fechaCancelacion = LocalDate.now();
        this.motivoCancelacion = motivo.trim();
        this.operadorCancelacion = (operador == null || operador.trim().isEmpty()) ? "Operador" : operador.trim();
    }

    public void restaurarCancelacion(LocalDate fechaCancelacion, String motivo, String operador) {
        this.fechaCancelacion = fechaCancelacion;
        this.motivoCancelacion = motivo;
        this.operadorCancelacion = operador;
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Cancha getCancha() { return cancha; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public double getCostoCancha() { return costoCancha; }
    public List<ServicioAdicional> getServicios() { return servicios; }
    public List<Pago> getPagos() { return pagos; }
    public EstadoReserva getEstado() { return estado; }
    public LocalDate getFechaCancelacion() { return fechaCancelacion; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public String getOperadorCancelacion() { return operadorCancelacion; }
}
