package com.canchapro.vista;

import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.EstadoReserva;
import com.canchapro.modelo.Reserva;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PanelDashboard extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;

    private JLabel lblReservasHoy = new JLabel("0");
    private JLabel lblPendientes = new JLabel("0");
    private JLabel lblIngresosHoy = new JLabel("Q0.00");
    private JLabel lblSaldoPorCobrar = new JLabel("Q0.00");

    private DefaultTableModel modeloTabla;
    private JTable tablaHoy;

    public PanelDashboard(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Encabezado
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titulo = new JLabel("Panel Principal - " + sistema.getConfiguracion().getNombreComplejo() + " (" + LocalDate.now() + ")");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        top.add(titulo, BorderLayout.WEST);

        // Tarjetas KPI
        JPanel panelKPI = new JPanel(new GridLayout(1, 4, 15, 0));
        panelKPI.setOpaque(false);
        panelKPI.add(EstilosUI.crearTarjetaKPI("RESERVAS DE HOY", lblReservasHoy, EstilosUI.PRIMARIO));
        panelKPI.add(EstilosUI.crearTarjetaKPI("PENDIENTES / CONFIRMADAS", lblPendientes, EstilosUI.ADVERTENCIA));
        panelKPI.add(EstilosUI.crearTarjetaKPI("INGRESOS COBRADOS HOY", lblIngresosHoy, EstilosUI.SECUNDARIO));
        panelKPI.add(EstilosUI.crearTarjetaKPI("SALDO PENDIENTE TOTAL", lblSaldoPorCobrar, EstilosUI.PELIGRO));

        JPanel norte = new JPanel(new BorderLayout(0, 15));
        norte.setOpaque(false);
        norte.add(top, BorderLayout.NORTH);
        norte.add(panelKPI, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // Tabla de reservas del día
        String[] cols = {"Código", "Cliente", "Cancha", "Horario", "Total", "Pagado", "Saldo", "Estado"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHoy = new JTable(modeloTabla);
        tablaHoy.setRowHeight(28);
        tablaHoy.setFont(EstilosUI.FUENTE_NORMAL);

        JPanel centro = new JPanel(new BorderLayout(5, 8));
        centro.setBackground(EstilosUI.BLANCO);
        centro.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel sub = new JLabel("Agenda Operativa de Hoy (" + LocalDate.now() + ")");
        sub.setFont(EstilosUI.FUENTE_SUBTITULO);
        centro.add(sub, BorderLayout.NORTH);
        centro.add(new JScrollPane(tablaHoy), BorderLayout.CENTER);

        // Botones rápidos de estado
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        JButton btnEnCurso = EstilosUI.crearBoton("Marcar EN CURSO", EstilosUI.PRIMARIO);
        JButton btnFinalizar = EstilosUI.crearBoton("Marcar FINALIZADA", new Color(37, 99, 235));
        JButton btnCancelar = EstilosUI.crearBoton("Cancelar Reserva", EstilosUI.PELIGRO);

        btnEnCurso.addActionListener(e -> cambiarEstadoSeleccionado(EstadoReserva.EN_CURSO));
        btnFinalizar.addActionListener(e -> cambiarEstadoSeleccionado(EstadoReserva.FINALIZADA));
        btnCancelar.addActionListener(e -> cancelarSeleccionada());

        acciones.add(btnEnCurso);
        acciones.add(btnFinalizar);
        acciones.add(btnCancelar);
        centro.add(acciones, BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);
        refrescar();
    }

    private void cambiarEstadoSeleccionado(EstadoReserva nuevoEstado) {
        int fila = tablaHoy.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return;
        }
        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        Reserva r = sistema.getReservas().stream().filter(res -> res.getCodigo().equals(codigo)).findFirst().orElse(null);
        try {
            sistema.actualizarEstadoReserva(r, nuevoEstado);
            onRefrescarGlobal.run();
        } catch (ValidacionNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regla de Negocio", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelarSeleccionada() {
        int fila = tablaHoy.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return;
        }
        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        Reserva r = sistema.getReservas().stream().filter(res -> res.getCodigo().equals(codigo)).findFirst().orElse(null);
        String motivo = JOptionPane.showInputDialog(this, "Motivo de cancelación para " + codigo + ":");
        if (motivo != null) {
            try {
                sistema.cancelarReserva(r, motivo);
                onRefrescarGlobal.run();
                JOptionPane.showMessageDialog(this, "Reserva cancelada y conservada en el historial.");
            } catch (ValidacionNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public final void refrescar() {
        LocalDate hoy = LocalDate.now();
        List<Reserva> deHoy = sistema.getReservas().stream()
                .filter(r -> r.getFecha().equals(hoy))
                .collect(Collectors.toList());

        long activasHoy = deHoy.stream().filter(r -> r.getEstado() != EstadoReserva.CANCELADA).count();
        long pendientesTotal = sistema.getReservas().stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA)
                .count();
        double ingresosHoy = deHoy.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .mapToDouble(Reserva::getTotalPagado)
                .sum();
        double saldoPendienteGlobal = sistema.getReservas().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .mapToDouble(Reserva::getSaldoPendiente)
                .sum();

        lblReservasHoy.setText(String.valueOf(activasHoy));
        lblPendientes.setText(String.valueOf(pendientesTotal));
        lblIngresosHoy.setText(String.format("Q%.2f", ingresosHoy));
        lblSaldoPorCobrar.setText(String.format("Q%.2f", saldoPendienteGlobal));

        modeloTabla.setRowCount(0);
        for (Reserva r : deHoy) {
            modeloTabla.addRow(new Object[]{
                    r.getCodigo(),
                    r.getCliente().getNombre(),
                    r.getCancha().getNombre(),
                    r.getHoraInicio() + " - " + r.getHoraFin(),
                    String.format("Q%.2f", r.getTotal()),
                    String.format("Q%.2f", r.getTotalPagado()),
                    String.format("Q%.2f", r.getSaldoPendiente()),
                    r.getEstado()
            });
        }
    }
}
