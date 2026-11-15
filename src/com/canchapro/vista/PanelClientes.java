package com.canchapro.vista;

import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.Cliente;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelClientes extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;

    public PanelClientes(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Formulario superior de registro
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBackground(EstilosUI.BLANCO);
        form.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Cliente"));

        JTextField txtNombre = new JTextField(16);
        JTextField txtTel = new JTextField(10);
        JTextField txtCorreo = new JTextField(16);
        JButton btnRegistrar = EstilosUI.crearBoton("Guardar Cliente", EstilosUI.PRIMARIO);

        form.add(new JLabel("Nombre:")); form.add(txtNombre);
        form.add(new JLabel("Teléfono:")); form.add(txtTel);
        form.add(new JLabel("Correo (opcional):")); form.add(txtCorreo);
        form.add(btnRegistrar);

        btnRegistrar.addActionListener(e -> {
            try {
                sistema.registrarCliente(txtNombre.getText(), txtTel.getText(), txtCorreo.getText());
                txtNombre.setText(""); txtTel.setText(""); txtCorreo.setText("");
                onRefrescarGlobal.run();
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.");
            } catch (ValidacionNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });

        add(form, BorderLayout.NORTH);

        // Búsqueda y Tabla
        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setBackground(EstilosUI.BLANCO);
        centro.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBusqueda.setOpaque(false);
        barraBusqueda.add(new JLabel("Buscar por código, nombre o teléfono:"));
        txtBuscar = new JTextField(20);
        JButton btnBuscar = EstilosUI.crearBoton("Buscar", EstilosUI.SECUNDARIO);
        btnBuscar.addActionListener(e -> refrescar());
        barraBusqueda.add(txtBuscar);
        barraBusqueda.add(btnBuscar);
        centro.add(barraBusqueda, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Teléfono", "Correo", "Fecha Registro"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);
        refrescar();
    }

    public final void refrescar() {
        modelo.setRowCount(0);
        for (Cliente c : sistema.buscarClientes(txtBuscar != null ? txtBuscar.getText() : "")) {
            modelo.addRow(new Object[]{c.getCodigo(), c.getNombre(), c.getTelefono(), c.getCorreo(), c.getFechaRegistro()});
        }
    }
}
