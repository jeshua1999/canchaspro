package com.canchapro.vista;

import com.canchapro.modelo.*;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PanelReportes extends JPanel {
    private final SistemaCanchaPro sistema;
    private JTextArea areaReporte;
    private JTextField txtDesde;
    private JTextField txtHasta;

    public PanelReportes(SistemaCanchaPro sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        JLabel titulo = new JLabel("Reportería Gerencial y Operativa");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        top.add(titulo);

        txtDesde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 9);
        txtHasta = new JTextField(LocalDate.now().plusMonths(1).toString(), 9);
        JButton btnGenerar = EstilosUI.crearBoton("Generar Reporte Consolidado", EstilosUI.PRIMARIO);
        btnGenerar.addActionListener(e -> refrescar());

        top.add(new JLabel(" Desde:")); 
        top.add(txtDesde);
        top.add(new JLabel("Hasta:")); 
        top.add(txtHasta);
        top.add(btnGenerar);
        add(top, BorderLayout.NORTH);

        areaReporte = new JTextArea();
        areaReporte.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaReporte.setEditable(false);
        areaReporte.setMargin(new Insets(15, 15, 15, 15));
        add(new JScrollPane(areaReporte), BorderLayout.CENTER);

        refrescar();
    }

    public final void refrescar() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim());
            hasta = LocalDate.parse(txtHasta.getText().trim());
        } catch (Exception e) {
            desde = LocalDate.now().minusMonths(1);
            hasta = LocalDate.now().plusMonths(1);
        }

        final LocalDate fDesde = desde;
        final LocalDate fHasta = hasta;

        List<Reserva> enRango = sistema.getReservas().stream()
                .filter(r -> !r.getFecha().isBefore(fDesde) && !r.getFecha().isAfter(fHasta))
                .collect(Collectors.toList());

        List<Reserva> validas = enRango.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .collect(Collectors.toList());

        List<Reserva> canceladas = enRango.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CANCELADA)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append(" REPORTE EJECUTIVO - ").append(sistema.getConfiguracion().getNombreComplejo().toUpperCase()).append("\n");
        sb.append(" Período analizado: ").append(fDesde).append(" al ").append(fHasta).append("\n");
        sb.append("====================================================================\n\n");

        // 1. Resumen Financiero
        double totalFacturadoCanchas = validas.stream().mapToDouble(Reserva::getTotal).sum();
        double totalCobradoCanchas = validas.stream().mapToDouble(Reserva::getTotalPagado).sum();
        double totalPendiente = validas.stream().mapToDouble(Reserva::getSaldoPendiente).sum();
        double totalTienda = sistema.getVentas().stream()
                .filter(v -> !v.getFechaHora().toLocalDate().isBefore(fDesde) && !v.getFechaHora().toLocalDate().isAfter(fHasta))
                .mapToDouble(Venta::getTotal).sum();

        sb.append("1. INDICADORES FINANCIEROS DEL PERÍODO\n");
        sb.append(String.format("   - Total Facturado en Reservas:    Q%.2f (%d reservas activas)\n", totalFacturadoCanchas, validas.size()));
        sb.append(String.format("   - Total Cobrado (Ingresos):       Q%.2f\n", totalCobradoCanchas));
        sb.append(String.format("   - Saldos Pendientes por Cobrar:   Q%.2f\n", totalPendiente));
        
        // CORRECCIÓN: Se evalúa directamente el booleano devuelto por isModuloTiendaActivo()
        if (sistema.getConfiguracion().isModuloTiendaActivo()) {
            sb.append(String.format("   - Ingresos Tienda / Cafetería:    Q%.2f\n", totalTienda));
            double granTotal = totalCobradoCanchas + totalTienda;
            double pctCancha = granTotal > 0 ? (totalCobradoCanchas * 100.0 / granTotal) : 0;
            double pctTienda = granTotal > 0 ? (totalTienda * 100.0 / granTotal) : 0;
            sb.append(String.format("   - Distribución Ingresos:          Canchas %.1f%% | Tienda %.1f%%\n", pctCancha, pctTienda));
        }
        sb.append("\n");

        // 2. Uso por Cancha (Cancha más utilizada)
        sb.append("2. RESERVAS E INGRESOS POR CANCHA\n");
        Map<String, Long> conteoCancha = validas.stream()
                .collect(Collectors.groupingBy(r -> r.getCancha().getCodigo() + " - " + r.getCancha().getNombre(), Collectors.counting()));
        conteoCancha.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    double ingCancha = validas.stream()
                            .filter(r -> (r.getCancha().getCodigo() + " - " + r.getCancha().getNombre()).equals(entry.getKey()))
                            .mapToDouble(Reserva::getTotal).sum();
                    sb.append(String.format("   - %-25s : %d reservas | Facturado: Q%.2f\n", entry.getKey(), entry.getValue(), ingCancha));
                });
        sb.append("\n");

        // 3. Horarios de mayor demanda
        sb.append("3. HORARIOS DE MAYOR DEMANDA\n");
        Map<LocalTime, Long> porHora = validas.stream()
                .collect(Collectors.groupingBy(Reserva::getHoraInicio, Collectors.counting()));
        porHora.entrySet().stream()
                .sorted(Map.Entry.<LocalTime, Long>comparingByValue().reversed()) // <-- Corregido aquí
                .limit(5)
                .forEach(e -> sb.append(String.format("   - Hora %s : %d reservas\n", e.getKey(), e.getValue())));
        sb.append("\n");
//incompatible types:Comparator Entry String,Long cannot be converted to comparator ? super entry local time long 
        // 4. Clientes con mayor cantidad de reservas
        sb.append("4. TOP CLIENTES FRECUENTES\n");
        Map<String, Long> porCliente = validas.stream()
                .collect(Collectors.groupingBy(r -> r.getCliente().getNombre(), Collectors.counting()));
        porCliente.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append(String.format("   - %-25s : %d reservas\n", e.getKey(), e.getValue())));
        sb.append("\n");

        // 5. Reservas con saldo pendiente
        sb.append("5. DETALLE DE PAGOS PENDIENTES\n");
        validas.stream().filter(r -> r.getSaldoPendiente() > 0).forEach(r ->
                sb.append(String.format("   - %s | %s | %s | Saldo: Q%.2f\n",
                        r.getCodigo(), r.getFecha(), r.getCliente().getNombre(), r.getSaldoPendiente()))
        );
        sb.append("\n");

        // 6. Historial de Cancelaciones
        sb.append("6. RESERVAS CANCELADAS (").append(canceladas.size()).append(")\n");
        for (Reserva r : canceladas) {
            sb.append(String.format("   - %s | Fecha: %s | Cliente: %s | Motivo: %s (Op: %s)\n",
                    r.getCodigo(), r.getFecha(), r.getCliente().getNombre(), r.getMotivoCancelacion(), r.getOperadorCancelacion()));
        }

        areaReporte.setText(sb.toString());
        areaReporte.setCaretPosition(0);
    }
}