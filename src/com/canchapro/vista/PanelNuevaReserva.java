package com.canchapro.vista;

import com.canchapro.excepciones.CanchaProException;
import com.canchapro.modelo.*;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PanelNuevaReserva extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;

    private JComboBox<Cliente> cbClientes;
    private JTextField txtFecha;
    private JComboBox<String> cbHoraInicio;
    private JSpinner spDuracion;
    private JComboBox<String> cbTipoCancha;
    private JComboBox<Cancha> cbCanchasDisponibles;

    private JPanel panelCheckServicios;
    private List<JCheckBox> checkServicios = new ArrayList<>();

    private JLabel lblCostoCancha = new JLabel("Q0.00");
    private JLabel lblCostoServicios = new JLabel("Q0.00");
    private JLabel lblTotalReserva = new JLabel("Q0.00");
    private JTextField txtAnticipo = new JTextField("0.00", 10);
    private JComboBox<MetodoPago> cbMetodoPago = new JComboBox<>(MetodoPago.values());

    public PanelNuevaReserva(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Registrar Nueva Reserva");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        add(titulo, BorderLayout.NORTH);

        JPanel contenedor = new JPanel(new GridLayout(1, 2, 20, 0));
        contenedor.setOpaque(false);

        // Columna Izquierda: Paso 1 - Cliente, Fecha, Hora y Disponibilidad
        JPanel colIzq = new JPanel(new GridBagLayout());
        colIzq.setBackground(EstilosUI.BLANCO);
        colIzq.setBorder(BorderFactory.createTitledBorder("1. Datos de Solicitud y Disponibilidad"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        colIzq.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1;
        cbClientes = new JComboBox<>();
        colIzq.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy++;
        colIzq.add(new JLabel("Fecha (AAAA-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFecha = new JTextField(LocalDate.now().toString());
        colIzq.add(txtFecha, gbc);

        gbc.gridx = 0; gbc.gridy++;
        colIzq.add(new JLabel("Hora Inicio:"), gbc);
        gbc.gridx = 1;
        cbHoraInicio = new JComboBox<>();
        for (int h = 8; h <= 22; h++) {
            cbHoraInicio.addItem(String.format("%02d:00", h));
        }
        colIzq.add(cbHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy++;
        colIzq.add(new JLabel("Duración (horas):"), gbc);
        gbc.gridx = 1;
        spDuracion = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        colIzq.add(spDuracion, gbc);

        gbc.gridx = 0; gbc.gridy++;
        colIzq.add(new JLabel("Tipo de Cancha:"), gbc);
        gbc.gridx = 1;
        cbTipoCancha = new JComboBox<>(new String[]{"TODOS", "Fútbol 5", "Fútbol 7"});
        colIzq.add(cbTipoCancha, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JButton btnConsultar = EstilosUI.crearBoton("Consultar Canchas Disponibles", EstilosUI.PRIMARIO);
        btnConsultar.addActionListener(e -> consultarDisponibilidad());
        colIzq.add(btnConsultar, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        colIzq.add(new JLabel("Cancha Disponible:"), gbc);
        gbc.gridx = 1;
        cbCanchasDisponibles = new JComboBox<>();
        cbCanchasDisponibles.addActionListener(e -> recalcularTotales());
        colIzq.add(cbCanchasDisponibles, gbc);

        // Columna Derecha: Paso 2 - Servicios Adicionales y Pago Inicial
        JPanel colDer = new JPanel(new BorderLayout(10, 10));
        colDer.setBackground(EstilosUI.BLANCO);
        colDer.setBorder(BorderFactory.createTitledBorder("2. Servicios Adicionales, Cálculo y Anticipo"));

        panelCheckServicios = new JPanel();
        panelCheckServicios.setLayout(new BoxLayout(panelCheckServicios, BoxLayout.Y_AXIS));
        panelCheckServicios.setBackground(EstilosUI.BLANCO);
        colDer.add(new JScrollPane(panelCheckServicios), BorderLayout.CENTER);

        JPanel panelResumenPago = new JPanel(new GridLayout(6, 2, 8, 8));
        panelResumenPago.setBackground(EstilosUI.BLANCO);
        panelResumenPago.setBorder(new EmptyBorder(10, 10, 10, 10));

        panelResumenPago.add(new JLabel("Costo Alquiler Cancha:"));
        lblCostoCancha.setFont(EstilosUI.FUENTE_SUBTITULO);
        panelResumenPago.add(lblCostoCancha);

        panelResumenPago.add(new JLabel("Servicios Adicionales:"));
        lblCostoServicios.setFont(EstilosUI.FUENTE_SUBTITULO);
        panelResumenPago.add(lblCostoServicios);

        panelResumenPago.add(new JLabel("TOTAL A PAGAR:"));
        lblTotalReserva.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalReserva.setForeground(EstilosUI.PRIMARIO);
        panelResumenPago.add(lblTotalReserva);

        panelResumenPago.add(new JLabel("Anticipo / Primer Pago (Q):"));
        panelResumenPago.add(txtAnticipo);

        panelResumenPago.add(new JLabel("Método de Pago:"));
        panelResumenPago.add(cbMetodoPago);

        JButton btnGuardar = EstilosUI.crearBoton("Confirmar y Guardar Reserva", EstilosUI.SECUNDARIO);
        btnGuardar.addActionListener(e -> guardarReserva());
        panelResumenPago.add(new JLabel(""));
        panelResumenPago.add(btnGuardar);

        colDer.add(panelResumenPago, BorderLayout.SOUTH);

        contenedor.add(colIzq);
        contenedor.add(colDer);
        add(contenedor, BorderLayout.CENTER);

        refrescar();
    }

    private void consultarDisponibilidad() {
        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            LocalTime hora = LocalTime.parse((String) cbHoraInicio.getSelectedItem());
            int duracion = (int) spDuracion.getValue();
            String tipo = (String) cbTipoCancha.getSelectedItem();

            List<Cancha> disponibles = sistema.consultarCanchasDisponibles(fecha, hora, duracion, tipo);
            cbCanchasDisponibles.removeAllItems();
            for (Cancha c : disponibles) {
                cbCanchasDisponibles.addItem(c);
            }
            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay canchas disponibles para ese horario o están en mantenimiento.", "Sin Disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            }
            recalcularTotales();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use AAAA-MM-DD (ej. 2026-11-15).", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
        } catch (CanchaProException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void recalcularTotales() {
        Cancha cancha = (Cancha) cbCanchasDisponibles.getSelectedItem();
        if (cancha == null) {
            lblCostoCancha.setText("Q0.00");
            lblCostoServicios.setText("Q0.00");
            lblTotalReserva.setText("Q0.00");
            return;
        }
        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            LocalTime hora = LocalTime.parse((String) cbHoraInicio.getSelectedItem());
            int duracion = (int) spDuracion.getValue();

            double costoCancha = sistema.calcularCostoCancha(cancha, fecha, hora, duracion);
            double costoServ = 0;
            for (int i = 0; i < checkServicios.size(); i++) {
                if (checkServicios.get(i).isSelected()) {
                    costoServ += sistema.getServicios().get(i).getPrecio();
                }
            }
            lblCostoCancha.setText(String.format("Q%.2f", costoCancha));
            lblCostoServicios.setText(String.format("Q%.2f", costoServ));
            lblTotalReserva.setText(String.format("Q%.2f", costoCancha + costoServ));
        } catch (Exception ignored) {
        }
    }

    private void guardarReserva() {
        try {
            Cliente cliente = (Cliente) cbClientes.getSelectedItem();
            Cancha cancha = (Cancha) cbCanchasDisponibles.getSelectedItem();
            if (cancha == null) {
                JOptionPane.showMessageDialog(this, "Primero consulte y seleccione una cancha disponible.");
                return;
            }
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            LocalTime hora = LocalTime.parse((String) cbHoraInicio.getSelectedItem());
            int duracion = (int) spDuracion.getValue();

            double anticipo;
            try {
                anticipo = Double.parseDouble(txtAnticipo.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El monto del anticipo debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<ServicioAdicional> seleccionados = new ArrayList<>();
            for (int i = 0; i < checkServicios.size(); i++) {
                if (checkServicios.get(i).isSelected()) {
                    seleccionados.add(sistema.getServicios().get(i));
                }
            }

            Reserva creada = sistema.crearReserva(
                    cliente, cancha, fecha, hora, duracion, seleccionados,
                    anticipo, (MetodoPago) cbMetodoPago.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this,
                    "Reserva creada exitosamente.Código: " + creada.getCodigo() +"Total: Q" + String.format("%.2f", creada.getTotal()) + "Saldo pendiente: Q" + String.format("%.2f", creada.getSaldoPendiente()),"Reserva Confirmada", JOptionPane.INFORMATION_MESSAGE);

            cbCanchasDisponibles.removeAllItems();
            txtAnticipo.setText("0.00");
            for (JCheckBox cb : checkServicios) cb.setSelected(false);
            recalcularTotales();
            onRefrescarGlobal.run();
        } catch (CanchaProException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo crear la reserva", JOptionPane.WARNING_MESSAGE);
        }
    }

    public final void refrescar() {
        cbClientes.removeAllItems();
        for (Cliente c : sistema.getClientes()) {
            cbClientes.addItem(c);
        }

        panelCheckServicios.removeAll();
        checkServicios.clear();
        for (ServicioAdicional s : sistema.getServicios()) {
            if (s.isActivo()) {
                JCheckBox cb = new JCheckBox(s.toString());
                cb.setBackground(EstilosUI.BLANCO);
                cb.addActionListener(e -> recalcularTotales());
                checkServicios.add(cb);
                panelCheckServicios.add(cb);
            }
        }
        panelCheckServicios.revalidate();
        panelCheckServicios.repaint();
    }
}
