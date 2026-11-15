package com.canchapro.vista;

import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.MetodoPago;
import com.canchapro.modelo.Pago;
import com.canchapro.modelo.Reserva;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelPagos extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;

    private DefaultTableModel modeloReservas;
    private DefaultTableModel modeloPagos;
    private JTable tablaReservas;

    public PanelPagos(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Control de Pagos, Abonos y Saldos Pendientes");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        add(titulo, BorderLayout.NORTH);

        modeloReservas = new DefaultTableModel(new String[]{"Código", "Fecha", "Cliente", "Cancha", "Total", "Pagado", "Saldo Pendiente", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setRowHeight(26);
        tablaReservas.getSelectionModel().addListSelectionListener(e -> cargarDetallePagos());

        modeloPagos = new DefaultTableModel(new String[]{"Cód. Pago", "Fecha y Hora", "Monto", "Método", "Observación"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaPagos = new JTable(modeloPagos);
        tablaPagos.setRowHeight(24);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaReservas), new JScrollPane(tablaPagos));
        split.setDividerLocation(260);
        add(split, BorderLayout.CENTER);

        // Panel inferior para abonar
        JPanel formPago = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        formPago.setBackground(EstilosUI.BLANCO);
        formPago.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Abono a Reserva Seleccionada"));

        JTextField txtMonto = new JTextField(8);
        JComboBox<MetodoPago> cbMetodo = new JComboBox<>(MetodoPago.values());
        JTextField txtObs = new JTextField(18);
        JButton btnAbonar = EstilosUI.crearBoton("Registrar Pago", EstilosUI.PRIMARIO);

        formPago.add(new JLabel("Monto Q:")); formPago.add(txtMonto);
        formPago.add(new JLabel("Método:")); formPago.add(cbMetodo);
        formPago.add(new JLabel("Observación:")); formPago.add(txtObs);
        formPago.add(btnAbonar);

        btnAbonar.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una reserva de la lista superior.");
                return;
            }
            String codRes = (String) modeloReservas.getValueAt(fila, 0);
            Reserva r = sistema.getReservas().stream().filter(res -> res.getCodigo().equals(codRes)).findFirst().orElse(null);
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                sistema.registrarAbono(r, monto, (MetodoPago) cbMetodo.getSelectedItem(), txtObs.getText());
                txtMonto.setText(""); txtObs.setText("");
                onRefrescarGlobal.run();
                JOptionPane.showMessageDialog(this, "Pago registrado. Nuevo saldo: Q" + String.format("%.2f", r.getSaldoPendiente()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un monto numérico válido.");
            } catch (ValidacionNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación de Pago", JOptionPane.WARNING_MESSAGE);
            }
        });

        add(formPago, BorderLayout.SOUTH);
        refrescar();
    }

    private void cargarDetallePagos() {
        modeloPagos.setRowCount(0);
        int fila = tablaReservas.getSelectedRow();
        if (fila < 0) return;
        String codRes = (String) modeloReservas.getValueAt(fila, 0);
        Reserva r = sistema.getReservas().stream().filter(res -> res.getCodigo().equals(codRes)).findFirst().orElse(null);
        if (r != null) {
            for (Pago p : r.getPagos()) {
                modeloPagos.addRow(new Object[]{
                        p.getCodigoPago(), p.getFechaHora().toString().replace("T", " "),
                        String.format("Q%.2f", p.getMonto()), p.getMetodo(), p.getObservacion()
                });
            }
        }
    }

    public final void refrescar() {
        modeloReservas.setRowCount(0);
        for (Reserva r : sistema.getReservas()) {
            modeloReservas.addRow(new Object[]{
                    r.getCodigo(), r.getFecha(), r.getCliente().getNombre(), r.getCancha().getNombre(),
                    String.format("Q%.2f", r.getTotal()),
                    String.format("Q%.2f", r.getTotalPagado()),
                    String.format("Q%.2f", r.getSaldoPendiente()),
                    r.getEstado()
            });
        }
    }
}
