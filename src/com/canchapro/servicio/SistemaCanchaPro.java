package com.canchapro.servicio;

import com.canchapro.excepciones.ConflictoHorarioException;
import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.*;
import com.canchapro.persistencia.ConfiguracionSistema;
import com.canchapro.persistencia.RepositorioCSV;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class SistemaCanchaPro {
    private final ConfiguracionSistema configuracion;
    private final RepositorioCSV repositorio;

    private List<Cliente> clientes;
    private List<Cancha> canchas;
    private List<Tarifa> tarifas;
    private List<ServicioAdicional> servicios;
    private List<Reserva> reservas;
    private List<Producto> productos;
    private List<Venta> ventas;

    public SistemaCanchaPro(String directorioDatos) {
        this.configuracion = new ConfiguracionSistema(directorioDatos);
        this.repositorio = new RepositorioCSV(directorioDatos);
        recargarTodo();
    }

    public final void recargarTodo() {
        repositorio.limpiarErroresCarga();
        this.clientes = repositorio.cargarClientes();
        this.canchas = repositorio.cargarCanchas();
        this.tarifas = repositorio.cargarTarifas();
        this.servicios = repositorio.cargarServicios();
        this.productos = repositorio.cargarProductos();

        if (this.canchas.isEmpty()) inicializarDatosPorDefecto();

        Map<String, Cliente> mapCli = clientes.stream().collect(Collectors.toMap(Cliente::getCodigo, c -> c, (a, b) -> a));
        Map<String, Cancha> mapCan = canchas.stream().collect(Collectors.toMap(Cancha::getCodigo, c -> c, (a, b) -> a));
        Map<String, ServicioAdicional> mapServ = servicios.stream().collect(Collectors.toMap(ServicioAdicional::getCodigo, s -> s, (a, b) -> a));
        Map<String, Producto> mapProd = productos.stream().collect(Collectors.toMap(Producto::getCodigo, p -> p, (a, b) -> a));

        this.reservas = repositorio.cargarReservas(mapCli, mapCan, mapServ);
        this.ventas = repositorio.cargarVentas(mapProd);
    }

    public void restaurarDatosDePrueba() {
        inicializarDatosPorDefecto();
        recargarTodo();
    }

    private void inicializarDatosPorDefecto() {
        clientes = new ArrayList<>(Arrays.asList(
                new Cliente("CLI-001", "Carlos Méndez", "5512-3456", "carlos@gmail.com", LocalDate.now().minusDays(20)),
                new Cliente("CLI-002", "Andrea Morales", "4498-7654", "andrea.m@gmail.com", LocalDate.now().minusDays(15)),
                new Cliente("CLI-003", "Jorge Luis Pérez", "3321-0987", "jorgep@hotmail.com", LocalDate.now().minusDays(10)),
                new Cliente("CLI-004", "Club Deportivo Mazate", "5890-1122", "clubmazate@gmail.com", LocalDate.now().minusDays(5))
        ));
        repositorio.guardarClientes(clientes);

        canchas = new ArrayList<>(Arrays.asList(
                new Cancha("C01", "La Bombonera", "Fútbol 5", 10, 150.0, EstadoCancha.DISPONIBLE, LocalTime.of(8, 0), LocalTime.of(23, 0)),
                new Cancha("C02", "El Maracaná", "Fútbol 5", 10, 150.0, EstadoCancha.DISPONIBLE, LocalTime.of(8, 0), LocalTime.of(23, 0)),
                new Cancha("C03", "El Monumental", "Fútbol 7", 14, 200.0, EstadoCancha.DISPONIBLE, LocalTime.of(8, 0), LocalTime.of(23, 0))
        ));
        repositorio.guardarCanchas(canchas);

        tarifas = new ArrayList<>(Arrays.asList(
                new Tarifa("TAR-01", "Lunes-Viernes Diurno", "LUNES_VIERNES", LocalTime.of(8, 0), LocalTime.of(17, 0), 150.0),
                new Tarifa("TAR-02", "Lunes-Viernes Nocturno", "LUNES_VIERNES", LocalTime.of(17, 0), LocalTime.of(23, 0), 225.0),
                new Tarifa("TAR-03", "Sábado y Domingo", "FIN_SEMANA", LocalTime.of(8, 0), LocalTime.of(23, 0), 250.0)
        ));
        repositorio.guardarTarifas(tarifas);

        servicios = new ArrayList<>(Arrays.asList(
                new ServicioAdicional("SRV-01", "Alquiler de Balón Profesional", 25.0, true),
                new ServicioAdicional("SRV-02", "Juego de Chalecos (14 uds)", 20.0, true),
                new ServicioAdicional("SRV-03", "Servicio de Árbitro", 75.0, true),
                new ServicioAdicional("SRV-04", "Iluminación Nocturna Extra", 30.0, true),
                new ServicioAdicional("SRV-05", "Fardo de Agua Pura", 35.0, true)
        ));
        repositorio.guardarServicios(servicios);

        productos = new ArrayList<>(Arrays.asList(
                new Producto("PRD-01", "Gatorade 600ml", "Bebidas", 12.0, 40, 10, true),
                new Producto("PRD-02", "Agua Pura 600ml", "Bebidas", 6.0, 60, 15, true),
                new Producto("PRD-03", "Coca-Cola Lata", "Bebidas", 8.0, 35, 10, true),
                new Producto("PRD-04", "Shuco Completo", "Alimentos", 18.0, 20, 5, true),
                new Producto("PRD-05", "Nachos con Queso", "Snacks", 15.0, 25, 5, true)
        ));
        repositorio.guardarProductos(productos);

        // Reservas de ejemplo iniciales
        LocalDate hoy = LocalDate.now();
        Reserva r1 = new Reserva("RES-001", clientes.get(0), canchas.get(0), hoy, LocalTime.of(16, 0), LocalTime.of(18, 0), 375.0, EstadoReserva.CONFIRMADA);
        r1.agregarServicio(servicios.get(0));
        r1.cargarPagoDesdeArchivo(new Pago("PAG-001", "RES-001", LocalDateTime.now().minusHours(3), 200.0, MetodoPago.TRANSFERENCIA, "Anticipo reserva"));

        Reserva r2 = new Reserva("RES-002", clientes.get(1), canchas.get(2), hoy, LocalTime.of(19, 0), LocalTime.of(21, 0), 450.0, EstadoReserva.CONFIRMADA);
        r2.agregarServicio(servicios.get(2));
        r2.cargarPagoDesdeArchivo(new Pago("PAG-002", "RES-002", LocalDateTime.now().minusHours(2), 525.0, MetodoPago.EFECTIVO, "Pago total"));

        reservas = new ArrayList<>(Arrays.asList(r1, r2));
        repositorio.guardarReservasYPagos(reservas);

        Venta v1 = new Venta("VTA-001", LocalDateTime.now().minusHours(1), "RES-001", MetodoPago.EFECTIVO);
        v1.agregarDetalle(new DetalleVenta("VTA-001", productos.get(0), 4, 12.0));
        ventas = new ArrayList<>(Collections.singletonList(v1));
        repositorio.guardarVentas(ventas);
    }

    // ==================== MOTOR DE DISPONIBILIDAD Y TRASLAPE ====================
    /**
     * Regla oficial: inicioNuevo < finExistente && finNuevo > inicioExistente
     * Solo evalúa reservas activas (no CANCELADA).
     */
    public boolean existeTraslape(Cancha cancha, LocalDate fecha, LocalTime inicioNuevo, LocalTime finNuevo) {
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.CANCELADA) continue;
            if (!r.getCancha().getCodigo().equals(cancha.getCodigo())) continue;
            if (!r.getFecha().equals(fecha)) continue;

            LocalTime inicioExistente = r.getHoraInicio();
            LocalTime finExistente = r.getHoraFin();

            if (inicioNuevo.isBefore(finExistente) && finNuevo.isAfter(inicioExistente)) {
                return true;
            }
        }
        return false;
    }

    public List<Cancha> consultarCanchasDisponibles(LocalDate fecha, LocalTime horaInicio, int duracionHoras, String tipoFiltro)
            throws ValidacionNegocioException {
        if (fecha == null) throw new ValidacionNegocioException("Debe seleccionar una fecha válida.");
        if (horaInicio == null) throw new ValidacionNegocioException("Debe seleccionar una hora de inicio.");
        if (duracionHoras <= 0 || duracionHoras > 8) {
            throw new ValidacionNegocioException("La duración debe estar entre 1 y 8 horas.");
        }
        LocalTime horaFin = horaInicio.plusHours(duracionHoras);
        if (horaFin.isBefore(horaInicio) && !horaFin.equals(LocalTime.MIDNIGHT)) {
            throw new ValidacionNegocioException("El horario excede el cierre del día.");
        }

        List<Cancha> disponibles = new ArrayList<>();
        for (Cancha c : canchas) {
            if (!c.estaOperativa()) continue;
            if (tipoFiltro != null && !tipoFiltro.equalsIgnoreCase("TODOS") && !c.getTipo().equalsIgnoreCase(tipoFiltro)) {
                continue;
            }
            if (horaInicio.isBefore(c.getHoraApertura()) || horaFin.isAfter(c.getHoraCierre())) {
                continue;
            }
            if (!existeTraslape(c, fecha, horaInicio, horaFin)) {
                disponibles.add(c);
            }
        }
        return disponibles;
    }

    // ==================== CÁLCULO DE TARIFAS (NIVEL AVANZADO POR TRAMOS) ====================
    /**
     * Calcula el precio dividiendo por hora para soportar reservas que cruzan
     * de franja diurna (150/h) a nocturna (225/h) automáticamente.
     */
    public double calcularCostoCancha(Cancha cancha, LocalDate fecha, LocalTime horaInicio, int duracionHoras) {
        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
        String tipoDiaBuscado = esFinDeSemana ? "FIN_SEMANA" : "LUNES_VIERNES";

        double total = 0.0;
        for (int i = 0; i < duracionHoras; i++) {
            LocalTime tramoInicio = horaInicio.plusHours(i);
            double tarifaHora = obtenerTarifaParaHora(tipoDiaBuscado, tramoInicio, cancha.getPrecioBase());
            total += tarifaHora;
        }
        return total;
    }

    private double obtenerTarifaParaHora(String tipoDia, LocalTime hora, double fallback) {
        for (Tarifa t : tarifas) {
            if (t.getTipoDia().equalsIgnoreCase(tipoDia)) {
                if (!hora.isBefore(t.getHoraInicio()) && hora.isBefore(t.getHoraFin())) {
                    return t.getPrecioPorHora();
                }
            }
        }
        return fallback;
    }

    // ==================== GESTIÓN DE RESERVAS Y PAGOS ====================
    public Reserva crearReserva(Cliente cliente, Cancha cancha, LocalDate fecha, LocalTime horaInicio,
                                int duracionHoras, List<ServicioAdicional> serviciosSeleccionados,
                                double montoAnticipo, MetodoPago metodoPago)
            throws ValidacionNegocioException, ConflictoHorarioException {

        if (cliente == null) throw new ValidacionNegocioException("Debe seleccionar un cliente.");
        if (cancha == null) throw new ValidacionNegocioException("Debe seleccionar una cancha.");
        if (!cancha.estaOperativa()) {
            throw new ValidacionNegocioException("La cancha " + cancha.getNombre() + " está en estado " + cancha.getEstado() + ".");
        }
        if (duracionHoras <= 0) throw new ValidacionNegocioException("La duración debe ser al menos 1 hora.");

        LocalTime horaFin = horaInicio.plusHours(duracionHoras);
        if (horaInicio.isBefore(cancha.getHoraApertura()) || horaFin.isAfter(cancha.getHoraCierre())) {
            throw new ValidacionNegocioException("Horario fuera de operación de la cancha (" + cancha.getHoraApertura() + " - " + cancha.getHoraCierre() + ").");
        }

        if (existeTraslape(cancha, fecha, horaInicio, horaFin)) {
            throw new ConflictoHorarioException("Conflicto de horario: la cancha " + cancha.getNombre() + " ya tiene una reserva que se traslapa.");
        }

        double costoCancha = calcularCostoCancha(cancha, fecha, horaInicio, duracionHoras);
        String codigoReserva = generarSiguienteCodigo("RES-", reservas.size() + 1);

        EstadoReserva estadoInicial = montoAnticipo > 0 ? EstadoReserva.CONFIRMADA : EstadoReserva.PENDIENTE;
        Reserva nueva = new Reserva(codigoReserva, cliente, cancha, fecha, horaInicio, horaFin, costoCancha, estadoInicial);

        if (serviciosSeleccionados != null) {
            for (ServicioAdicional s : serviciosSeleccionados) {
                nueva.agregarServicio(s);
            }
        }

        if (montoAnticipo < 0) {
            throw new ValidacionNegocioException("El anticipo no puede ser negativo.");
        }
        if (montoAnticipo > 0) {
            String codPago = generarSiguienteCodigo("PAG-", contarTotalPagos() + 1);
            Pago pagoInicial = new Pago(codPago, codigoReserva, LocalDateTime.now(), montoAnticipo,
                    metodoPago != null ? metodoPago : MetodoPago.EFECTIVO, "Pago inicial / Anticipo");
            nueva.agregarPago(pagoInicial);
        }

        reservas.add(nueva);
        repositorio.guardarReservasYPagos(reservas);
        return nueva;
    }

    public void registrarAbono(Reserva reserva, double monto, MetodoPago metodo, String observacion)
            throws ValidacionNegocioException {
        if (reserva == null) throw new ValidacionNegocioException("Seleccione una reserva.");
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ValidacionNegocioException("No se pueden registrar pagos a una reserva cancelada.");
        }
        String codPago = generarSiguienteCodigo("PAG-", contarTotalPagos() + 1);
        Pago pago = new Pago(codPago, reserva.getCodigo(), LocalDateTime.now(), monto, metodo, observacion);
        reserva.agregarPago(pago);
        repositorio.guardarReservasYPagos(reservas);
    }

    public void cancelarReserva(Reserva reserva, String motivo) throws ValidacionNegocioException {
        if (reserva == null) throw new ValidacionNegocioException("Seleccione una reserva.");
        reserva.cancelar(motivo, configuracion.getOperadorActual());
        repositorio.guardarReservasYPagos(reservas);
    }

    public void actualizarEstadoReserva(Reserva reserva, EstadoReserva nuevoEstado) throws ValidacionNegocioException {
        if (reserva == null) throw new ValidacionNegocioException("Seleccione una reserva.");
        reserva.cambiarEstado(nuevoEstado);
        repositorio.guardarReservasYPagos(reservas);
    }

    private int contarTotalPagos() {
        int total = 0;
        for (Reserva r : reservas) total += r.getPagos().size();
        return total;
    }

    // ==================== CLIENTES Y CANCHAS ====================
    public Cliente registrarCliente(String nombre, String telefono, String correo) throws ValidacionNegocioException {
        if (nombre == null || nombre.trim().isEmpty()) throw new ValidacionNegocioException("El nombre del cliente es obligatorio.");
        if (telefono == null || telefono.trim().isEmpty()) throw new ValidacionNegocioException("El teléfono es obligatorio.");

        for (Cliente c : clientes) {
            if (c.getTelefono().equalsIgnoreCase(telefono.trim())) {
                throw new ValidacionNegocioException("Ya existe un cliente registrado con ese teléfono: " + c.getNombre());
            }
        }
        String codigo = generarSiguienteCodigo("CLI-", clientes.size() + 1);
        Cliente nuevo = new Cliente(codigo, nombre.trim(), telefono.trim(), correo, LocalDate.now());
        clientes.add(nuevo);
        repositorio.guardarClientes(clientes);
        return nuevo;
    }

    public void guardarCambiosClientes() {
        repositorio.guardarClientes(clientes);
    }

    public List<Cliente> buscarClientes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) return new ArrayList<>(clientes);
        String f = filtro.toLowerCase().trim();
        return clientes.stream()
                .filter(c -> c.getCodigo().toLowerCase().contains(f)
                        || c.getNombre().toLowerCase().contains(f)
                        || c.getTelefono().toLowerCase().contains(f))
                .collect(Collectors.toList());
    }

    public void registrarCancha(String codigo, String nombre, String tipo, int capacidad, double precioBase,
                                EstadoCancha estado, LocalTime apertura, LocalTime cierre) throws ValidacionNegocioException {
        if (codigo == null || codigo.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionNegocioException("Código y nombre de cancha son obligatorios.");
        }
        for (Cancha c : canchas) {
            if (c.getCodigo().equalsIgnoreCase(codigo.trim())) {
                throw new ValidacionNegocioException("Ya existe una cancha con el código " + codigo);
            }
        }
        canchas.add(new Cancha(codigo.trim().toUpperCase(), nombre.trim(), tipo, capacidad, precioBase, estado, apertura, cierre));
        repositorio.guardarCanchas(canchas);
    }

    public void guardarCambiosCanchas() {
        repositorio.guardarCanchas(canchas);
    }

    // ==================== TIENDA / CAFETERÍA ====================
    public Producto registrarProducto(String nombre, String categoria, double precio, int existencia, int stockMinimo)
            throws ValidacionNegocioException {
        if (nombre == null || nombre.trim().isEmpty()) throw new ValidacionNegocioException("Nombre de producto obligatorio.");
        if (precio <= 0) throw new ValidacionNegocioException("El precio debe ser mayor a Q0.00.");
        if (existencia < 0) throw new ValidacionNegocioException("La existencia inicial no puede ser negativa.");

        String cod = generarSiguienteCodigo("PRD-", productos.size() + 1);
        Producto p = new Producto(cod, nombre.trim(), categoria, precio, existencia, stockMinimo, true);
        productos.add(p);
        repositorio.guardarProductos(productos);
        return p;
    }

    public void guardarCambiosProductos() {
        repositorio.guardarProductos(productos);
    }

    public Venta registrarVenta(Map<Producto, Integer> carrito, String codigoReservaOpcional, MetodoPago metodo)
            throws ValidacionNegocioException {
        if (!configuracion.isModuloTiendaActivo()) {
            throw new ValidacionNegocioException("El módulo de Tienda/Cafetería está desactivado.");
        }
        if (carrito == null || carrito.isEmpty()) {
            throw new ValidacionNegocioException("Agregue al menos un producto a la venta.");
        }
        if (codigoReservaOpcional != null && !codigoReservaOpcional.trim().isEmpty()) {
            boolean existeRes = reservas.stream().anyMatch(r -> r.getCodigo().equalsIgnoreCase(codigoReservaOpcional.trim()));
            if (!existeRes) {
                throw new ValidacionNegocioException("El código de reserva asociado no existe: " + codigoReservaOpcional);
            }
        }

        // Validar stock antes de descontar
        for (Map.Entry<Producto, Integer> entry : carrito.entrySet()) {
            Producto prod = entry.getKey();
            int cant = entry.getValue();
            if (cant <= 0) throw new ValidacionNegocioException("Cantidad inválida para " + prod.getNombre());
            if (cant > prod.getExistencia()) {
                throw new ValidacionNegocioException("Stock insuficiente para " + prod.getNombre() +
                        ". Disponible: " + prod.getExistencia() + ", solicitado: " + cant);
            }
        }

        String numVenta = generarSiguienteCodigo("VTA-", ventas.size() + 1);
        Venta venta = new Venta(numVenta, LocalDateTime.now(), codigoReservaOpcional, metodo);

        for (Map.Entry<Producto, Integer> entry : carrito.entrySet()) {
            Producto prod = entry.getKey();
            int cant = entry.getValue();
            prod.setExistencia(prod.getExistencia() - cant);
            venta.agregarDetalle(new DetalleVenta(numVenta, prod, cant, prod.getPrecio()));
        }

        ventas.add(venta);
        repositorio.guardarProductos(productos);
        repositorio.guardarVentas(ventas);
        return venta;
    }

    private String generarSiguienteCodigo(String prefijo, int numero) {
        return String.format("%s%03d", prefijo, numero);
    }

    // Getters
    public ConfiguracionSistema getConfiguracion() { return configuracion; }
    public List<String> getErroresCarga() { return repositorio.getErroresCarga(); }
    public List<Cliente> getClientes() { return clientes; }
    public List<Cancha> getCanchas() { return canchas; }
    public List<Tarifa> getTarifas() { return tarifas; }
    public List<ServicioAdicional> getServicios() { return servicios; }
    public List<Reserva> getReservas() { return reservas; }
    public List<Producto> getProductos() { return productos; }
    public List<Venta> getVentas() { return ventas; }
}
