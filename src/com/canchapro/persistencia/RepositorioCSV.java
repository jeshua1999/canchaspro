package com.canchapro.persistencia;

import com.canchapro.modelo.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class RepositorioCSV {
    private final String dir;
    private final List<String> erroresCarga = new ArrayList<>();

    public RepositorioCSV(String directorioDatos) {
        this.dir = directorioDatos;
        new File(dir).mkdirs();
    }

    public List<String> getErroresCarga() {
        return erroresCarga;
    }

    public void limpiarErroresCarga() {
        erroresCarga.clear();
    }

    private List<String[]> leerLineas(String nombreArchivo) {
        List<String[]> filas = new ArrayList<>();
        File f = new File(dir + File.separator + nombreArchivo);
        if (!f.exists()) return filas;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String linea;
            int numLinea = 0;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                if (numLinea == 1 && linea.toLowerCase().startsWith("codigo")) continue;
                if (numLinea == 1 && linea.toLowerCase().startsWith("numero")) continue;
                if (linea.trim().isEmpty()) continue;
                filas.add(linea.split(";", -1));
            }
        } catch (IOException e) {
            erroresCarga.add("No se pudo leer " + nombreArchivo + ": " + e.getMessage());
        }
        return filas;
    }

    private void escribirLineas(String nombreArchivo, String encabezado, List<String> lineas) {
        File f = new File(dir + File.separator + nombreArchivo);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            bw.write(encabezado);
            bw.newLine();
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo " + nombreArchivo + ": " + e.getMessage());
        }
    }

    // --- CLIENTES ---
    public List<Cliente> cargarClientes() {
        List<Cliente> lista = new ArrayList<>();
        for (String[] p : leerLineas("clientes.csv")) {
            try {
                if (p.length < 5) throw new IllegalArgumentException("Columnas insuficientes");
                lista.add(new Cliente(p[0], p[1], p[2], p[3], LocalDate.parse(p[4])));
            } catch (Exception e) {
                erroresCarga.add("clientes.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarClientes(List<Cliente> clientes) {
        List<String> out = new ArrayList<>();
        for (Cliente c : clientes) {
            out.add(String.join(";", c.getCodigo(), limpiar(c.getNombre()), limpiar(c.getTelefono()),
                    limpiar(c.getCorreo()), c.getFechaRegistro().toString()));
        }
        escribirLineas("clientes.csv", "codigo;nombre;telefono;correo;fecha_registro", out);
    }

    // --- CANCHAS ---
    public List<Cancha> cargarCanchas() {
        List<Cancha> lista = new ArrayList<>();
        for (String[] p : leerLineas("canchas.csv")) {
            try {
                if (p.length < 8) throw new IllegalArgumentException("Columnas insuficientes");
                lista.add(new Cancha(
                        p[0], p[1], p[2],
                        Integer.parseInt(p[3]),
                        Double.parseDouble(p[4]),
                        EstadoCancha.valueOf(p[5]),
                        LocalTime.parse(p[6]),
                        LocalTime.parse(p[7])
                ));
            } catch (Exception e) {
                erroresCarga.add("canchas.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarCanchas(List<Cancha> canchas) {
        List<String> out = new ArrayList<>();
        for (Cancha c : canchas) {
            out.add(String.join(";", c.getCodigo(), limpiar(c.getNombre()), limpiar(c.getTipo()),
                    String.valueOf(c.getCapacidad()), String.valueOf(c.getPrecioBase()),
                    c.getEstado().name(), c.getHoraApertura().toString(), c.getHoraCierre().toString()));
        }
        escribirLineas("canchas.csv", "codigo;nombre;tipo;capacidad;precio_base;estado;hora_apertura;hora_cierre", out);
    }

    // --- TARIFAS ---
    public List<Tarifa> cargarTarifas() {
        List<Tarifa> lista = new ArrayList<>();
        for (String[] p : leerLineas("tarifas.csv")) {
            try {
                if (p.length < 6) throw new IllegalArgumentException("Columnas insuficientes");
                lista.add(new Tarifa(
                        p[0], p[1], p[2],
                        LocalTime.parse(p[3]),
                        LocalTime.parse(p[4]),
                        Double.parseDouble(p[5])
                ));
            } catch (Exception e) {
                erroresCarga.add("tarifas.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarTarifas(List<Tarifa> tarifas) {
        List<String> out = new ArrayList<>();
        for (Tarifa t : tarifas) {
            out.add(String.join(";", t.getCodigo(), limpiar(t.getDescripcion()), t.getTipoDia(),
                    t.getHoraInicio().toString(), t.getHoraFin().toString(), String.valueOf(t.getPrecioPorHora())));
        }
        escribirLineas("tarifas.csv", "codigo;descripcion;tipo_dia;hora_inicio;hora_fin;precio_hora", out);
    }

    // --- SERVICIOS ADICIONALES ---
    public List<ServicioAdicional> cargarServicios() {
        List<ServicioAdicional> lista = new ArrayList<>();
        for (String[] p : leerLineas("servicios.csv")) {
            try {
                if (p.length < 4) throw new IllegalArgumentException("Columnas insuficientes");
                lista.add(new ServicioAdicional(p[0], p[1], Double.parseDouble(p[2]), Boolean.parseBoolean(p[3])));
            } catch (Exception e) {
                erroresCarga.add("servicios.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarServicios(List<ServicioAdicional> servicios) {
        List<String> out = new ArrayList<>();
        for (ServicioAdicional s : servicios) {
            out.add(String.join(";", s.getCodigo(), limpiar(s.getNombre()),
                    String.valueOf(s.getPrecio()), String.valueOf(s.isActivo())));
        }
        escribirLineas("servicios.csv", "codigo;nombre;precio;activo", out);
    }

    // --- RESERVAS Y PAGOS ---
    public List<Reserva> cargarReservas(Map<String, Cliente> mapaClientes, Map<String, Cancha> mapaCanchas, Map<String, ServicioAdicional> mapaServicios) {
        List<Reserva> lista = new ArrayList<>();
        Map<String, Reserva> mapaReservas = new LinkedHashMap<>();

        for (String[] p : leerLineas("reservas.csv")) {
            try {
                if (p.length < 12) throw new IllegalArgumentException("Columnas insuficientes");
                Cliente cli = mapaClientes.get(p[1]);
                Cancha can = mapaCanchas.get(p[2]);
                if (cli == null || can == null) {
                    throw new IllegalArgumentException("Cliente o cancha inexistente en reserva " + p[0]);
                }
                Reserva r = new Reserva(
                        p[0], cli, can,
                        LocalDate.parse(p[3]),
                        LocalTime.parse(p[4]),
                        LocalTime.parse(p[5]),
                        Double.parseDouble(p[6]),
                        EstadoReserva.valueOf(p[7])
                );
                if (!p[8].isEmpty()) {
                    for (String codServ : p[8].split(",")) {
                        ServicioAdicional sa = mapaServicios.get(codServ);
                        if (sa != null) r.agregarServicio(sa);
                    }
                }
                if (!p[9].isEmpty()) {
                    r.restaurarCancelacion(LocalDate.parse(p[9]), p[10], p[11]);
                }
                lista.add(r);
                mapaReservas.put(r.getCodigo(), r);
            } catch (Exception e) {
                erroresCarga.add("reservas.csv línea corrupta: " + String.join(";", p) + " (" + e.getMessage() + ")");
            }
        }

        for (String[] p : leerLineas("pagos.csv")) {
            try {
                if (p.length < 6) throw new IllegalArgumentException("Columnas insuficientes");
                Reserva r = mapaReservas.get(p[1]);
                if (r == null) throw new IllegalArgumentException("Reserva inexistente: " + p[1]);
                Pago pago = new Pago(
                        p[0], p[1],
                        LocalDateTime.parse(p[2]),
                        Double.parseDouble(p[3]),
                        MetodoPago.valueOf(p[4]),
                        p[5]
                );
                r.cargarPagoDesdeArchivo(pago);
            } catch (Exception e) {
                erroresCarga.add("pagos.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarReservasYPagos(List<Reserva> reservas) {
        List<String> outRes = new ArrayList<>();
        List<String> outPagos = new ArrayList<>();

        for (Reserva r : reservas) {
            List<String> codsServ = new ArrayList<>();
            for (ServicioAdicional s : r.getServicios()) {
                codsServ.add(s.getCodigo());
            }
            String fechaCanc = r.getFechaCancelacion() != null ? r.getFechaCancelacion().toString() : "";
            String motCanc = r.getMotivoCancelacion() != null ? limpiar(r.getMotivoCancelacion()) : "";
            String opCanc = r.getOperadorCancelacion() != null ? limpiar(r.getOperadorCancelacion()) : "";

            outRes.add(String.join(";",
                    r.getCodigo(),
                    r.getCliente().getCodigo(),
                    r.getCancha().getCodigo(),
                    r.getFecha().toString(),
                    r.getHoraInicio().toString(),
                    r.getHoraFin().toString(),
                    String.valueOf(r.getCostoCancha()),
                    r.getEstado().name(),
                    String.join(",", codsServ),
                    fechaCanc, motCanc, opCanc
            ));

            for (Pago p : r.getPagos()) {
                outPagos.add(String.join(";",
                        p.getCodigoPago(),
                        p.getCodigoReserva(),
                        p.getFechaHora().toString(),
                        String.valueOf(p.getMonto()),
                        p.getMetodo().name(),
                        limpiar(p.getObservacion())
                ));
            }
        }

        escribirLineas("reservas.csv", "codigo;cod_cliente;cod_cancha;fecha;hora_inicio;hora_fin;costo_cancha;estado;servicios;fecha_cancelacion;motivo_cancelacion;operador", outRes);
        escribirLineas("pagos.csv", "codigo;cod_reserva;fecha_hora;monto;metodo;observacion", outPagos);
    }

    // --- TIENDA: PRODUCTOS Y VENTAS ---
    public List<Producto> cargarProductos() {
        List<Producto> lista = new ArrayList<>();
        for (String[] p : leerLineas("productos.csv")) {
            try {
                if (p.length < 7) throw new IllegalArgumentException("Columnas insuficientes");
                lista.add(new Producto(
                        p[0], p[1], p[2],
                        Double.parseDouble(p[3]),
                        Integer.parseInt(p[4]),
                        Integer.parseInt(p[5]),
                        Boolean.parseBoolean(p[6])
                ));
            } catch (Exception e) {
                erroresCarga.add("productos.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarProductos(List<Producto> productos) {
        List<String> out = new ArrayList<>();
        for (Producto p : productos) {
            out.add(String.join(";",
                    p.getCodigo(), limpiar(p.getNombre()), limpiar(p.getCategoria()),
                    String.valueOf(p.getPrecio()), String.valueOf(p.getExistencia()),
                    String.valueOf(p.getStockMinimo()), String.valueOf(p.isActivo())
            ));
        }
        escribirLineas("productos.csv", "codigo;nombre;categoria;precio;existencia;stock_minimo;activo", out);
    }

    public List<Venta> cargarVentas(Map<String, Producto> mapaProductos) {
        List<Venta> lista = new ArrayList<>();
        Map<String, Venta> mapaVentas = new LinkedHashMap<>();

        for (String[] p : leerLineas("ventas.csv")) {
            try {
                if (p.length < 5) throw new IllegalArgumentException("Columnas insuficientes");
                Venta v = new Venta(p[0], LocalDateTime.parse(p[1]), p[2], MetodoPago.valueOf(p[4]));
                lista.add(v);
                mapaVentas.put(v.getNumero(), v);
            } catch (Exception e) {
                erroresCarga.add("ventas.csv línea corrupta: " + String.join(";", p));
            }
        }

        for (String[] p : leerLineas("detalle_ventas.csv")) {
            try {
                if (p.length < 5) throw new IllegalArgumentException("Columnas insuficientes");
                Venta v = mapaVentas.get(p[0]);
                Producto prod = mapaProductos.get(p[1]);
                if (v == null || prod == null) throw new IllegalArgumentException("Venta o producto no encontrado");
                v.agregarDetalle(new DetalleVenta(p[0], prod, Integer.parseInt(p[2]), Double.parseDouble(p[3])));
            } catch (Exception e) {
                erroresCarga.add("detalle_ventas.csv línea corrupta: " + String.join(";", p));
            }
        }
        return lista;
    }

    public void guardarVentas(List<Venta> ventas) {
        List<String> outVentas = new ArrayList<>();
        List<String> outDetalles = new ArrayList<>();

        for (Venta v : ventas) {
            outVentas.add(String.join(";",
                    v.getNumero(),
                    v.getFechaHora().toString(),
                    limpiar(v.getCodigoReservaOpcional()),
                    String.valueOf(v.getTotal()),
                    v.getMetodoPago().name()
            ));
            for (DetalleVenta d : v.getDetalles()) {
                outDetalles.add(String.join(";",
                        v.getNumero(),
                        d.getProducto().getCodigo(),
                        String.valueOf(d.getCantidad()),
                        String.valueOf(d.getPrecioUnitario()),
                        String.valueOf(d.getSubtotal())
                ));
            }
        }
        escribirLineas("ventas.csv", "numero;fecha_hora;cod_reserva;total;metodo_pago", outVentas);
        escribirLineas("detalle_ventas.csv", "numero_venta;cod_producto;cantidad;precio_unitario;subtotal", outDetalles);
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.replace(";", ",").replace("", " ").trim();
    }
}
