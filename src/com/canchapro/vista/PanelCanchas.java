package com.canchapro.vista;

import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.EstadoCancha;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;

public class PanelCanchas extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;
    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelCanchas(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Formulario agregar cancha
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(EstilosUI.BLANCO);
        form.setBorder(BorderFactory.createTitledBorder("Agregar Nueva Cancha"));

        JTextField txtCod = new JTextField(5);
        JTextField txtNombre = new JTextField(12);
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Fútbol 5", "Fútbol 7", "Fútbol 11"});
        JSpinner spCap = new JSpinner(new SpinnerNumberModel(10, 6, 22, 2));
        JTextField txtPrecio = new JTextField("150.0", 6);
        JButton btnAdd = EstilosUI.crearBoton("Registrar Cancha", EstilosUI.PRIMARIO);

        form.add(new JLabel("Código:")); form.add(txtCod);
        form.add(new JLabel("Nombre:")); form.add(txtNombre);
        form.add(new JLabel("Tipo:")); form.add(cbTipo);
        form.add(new JLabel("Capacidad:")); form.add(spCap);
        form.add(new JLabel("Tarifa Base Q:")); form.add(txtPrecio);
        form.add(btnAdd);

        btnAdd.addActionListener(e -> {
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                sistema.registrarCancha(txtCod.getText(), txtNombre.getText(), (String) cbTipo.getSelectedItem(),
                        (int) spCap.getValue(), precio, EstadoCancha.DISPONIBLE, LocalTime.of(8, 0), LocalTime.of(23, 0));
                txtCod.setText(""); txtNombre.setText("");
                onRefrescarGlobal.run();
                JOptionPane.showMessageDialog(this, "Cancha agregada.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio base inválido.");
            } catch (ValidacionNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        add(form, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Tipo", "Capacidad", "Tarifa Base", "Estado", "Horario"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Cambio de estado (DISPONIBLE / MANTENIMIENTO / INACTIVA)
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        JComboBox<EstadoCancha> cbEstado = new JComboBox<>(EstadoCancha.values());
        JButton btnEstado = EstilosUI.crearBoton("Cambiar Estado de Cancha Seleccionada", EstilosUI.ADVERTENCIA);
        btnEstado.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una cancha.");
                return;
            }
            String cod = (String) modelo.getValueAt(fila, 0);
            for (Cancha c : sistema.getCanchas()) {
                if (c.getCodigo().equals(cod)) {
                    c.setEstado((EstadoCancha) cbEstado.getSelectedItem());
                    break;
                }
            }
            sistema.guardarCambiosCanchas();
            onRefrescarGlobal.run();
        });
        sur.add(new JLabel("Nuevo Estado:"));
        sur.add(cbEstado);
        sur.add(btnEstado);
        add(sur, BorderLayout.SOUTH);

        refrescar();
    }

    public final void refrescar() {
        modelo.setRowCount(0);
        for (Cancha c : sistema.getCanchas()) {
            modelo.addRow(new Object[]{
                    c.getCodigo(), c.getNombre(), c.getTipo(), c.getCapacidad() + " jug.",
                    String.format("Q%.2f", c.getPrecioBase()), c.getEstado(),
                    c.getHoraApertura() + " - " + c.getHoraCierre()
            });
        }
    }
}
