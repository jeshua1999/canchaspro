package com.canchapro.vista;

import com.canchapro.excepciones.ValidacionNegocioException;
import com.canchapro.modelo.*;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PanelTienda extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onRefrescarGlobal;

    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloCarrito;
    private JComboBox<Producto> cbProductos;
    private JSpinner spCantidad;
    private JTextField txtReservaOpcional;
    private JComboBox<MetodoPago> cbMetodoPago;
    private JLabel lblTotalVenta = new JLabel("Total Venta: Q0.00");

    private final Map<Producto, Integer> carrito = new LinkedHashMap<>();

    public PanelTienda(SistemaCanchaPro sistema, Runnable onRefrescarGlobal) {
        this.sistema = sistema;
        this.onRefrescarGlobal = onRefrescarGlobal;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Módulo Tienda / Cafetería - Punto de Venta e Inventario");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        add(titulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(520);

        // Izquierda: Inventario y Alta de Productos
        JPanel panelInv = new JPanel(new BorderLayout(8, 8));
        panelInv.setBackground(EstilosUI.BLANCO);
        panelInv.setBorder(BorderFactory.createTitledBorder("Inventario de Productos"));

        modeloProductos = new DefaultTableModel(new String[]{"Código", "Producto", "Categoría", "Precio", "Stock", "Estado Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaProd = new JTable(modeloProductos);
        tablaProd.setRowHeight(25);
        panelInv.add(new JScrollPane(tablaProd), BorderLayout.CENTER);

        JPanel formNuevoProd = new JPanel(new GridLayout(3, 4, 5, 5));
        formNuevoProd.setBorder(BorderFactory.createTitledBorder("Nuevo Producto / Reabastecer"));
        JTextField txtNombre = new JTextField();
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"Bebidas", "Snacks", "Alimentos", "Accesorios"});
        JTextField txtPrecio = new JTextField();
        JSpinner spStock = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        JButton btnCrearProd = EstilosUI.crearBoton("Agregar Producto", EstilosUI.PRIMARIO);

        formNuevoProd.add(new JLabel("Nombre:")); formNuevoProd.add(txtNombre);
        formNuevoProd.add(new JLabel("Categoría:")); formNuevoProd.add(cbCat);
        formNuevoProd.add(new JLabel("Precio Q:")); formNuevoProd.add(txtPrecio);
        formNuevoProd.add(new JLabel("Existencia:")); formNuevoProd.add(spStock);
        formNuevoProd.add(new JLabel("")); formNuevoProd.add(btnCrearProd);

        btnCrearProd.addActionListener(e -> {
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                sistema.registrarProducto(txtNombre.getText(), (String) cbCat.getSelectedItem(), precio, (int) spStock.getValue(), 5);
                txtNombre.setText(""); txtPrecio.setText("");
                onRefrescarGlobal.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio numérico inválido.");
            } catch (ValidacionNegocioException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        panelInv.add(formNuevoProd, BorderLayout.SOUTH);

        // Derecha: Carrito de Venta
        JPanel panelVenta = new JPanel(new BorderLayout(8, 8));
        panelVenta.setBackground(EstilosUI.BLANCO);
        panelVenta.setBorder(BorderFactory.createTitledBorder("Nueva Venta"));

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.setOpaque(false);
        cbProductos = new JComboBox<>();
        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JButton btnAgregarCarrito = EstilosUI.crearBoton("+ Agregar", EstilosUI.SECUNDARIO);
        btnAgregarCarrito.addActionListener(e -> agregarAlCarrito());

        selector.add(cbProductos);
        selector.add(new JLabel("Cant:"));
        selector.add(spCantidad);
        selector.add(btnAgregarCarrito);
        panelVenta.add(selector, BorderLayout.NORTH);

        modeloCarrito = new DefaultTableModel(new String[]{"Producto", "Cant.", "P. Unitario", "Subtotal"}, 0);
        JTable tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(25);
        panelVenta.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel cierreVenta = new JPanel(new GridLayout(4, 2, 6, 6));
        cierreVenta.setBorder(new EmptyBorder(8, 8, 8, 8));
        txtReservaOpcional = new JTextField();
        cbMetodoPago = new JComboBox<>(MetodoPago.values());
        JButton btnConfirmarVenta = EstilosUI.crearBoton("Confirmar Venta", EstilosUI.PRIMARIO);
        JButton btnLimpiar = EstilosUI.crearBoton("Vaciar", EstilosUI.PELIGRO);

        btnConfirmarVenta.addActionListener(e -> procesarVenta());
        btnLimpiar.addActionListener(e -> { carrito.clear(); actualizarTablaCarrito(); });

        cierreVenta.add(new JLabel("Cód. Reserva (Opcional):")); cierreVenta.add(txtReservaOpcional);
        cierreVenta.add(new JLabel("Método de Pago:")); cierreVenta.add(cbMetodoPago);
        lblTotalVenta.setFont(EstilosUI.FUENTE_SUBTITULO);
        cierreVenta.add(lblTotalVenta); cierreVenta.add(new JLabel(""));
        cierreVenta.add(btnLimpiar); cierreVenta.add(btnConfirmarVenta);

        panelVenta.add(cierreVenta, BorderLayout.SOUTH);

        split.setLeftComponent(panelInv);
        split.setRightComponent(panelVenta);
        add(split, BorderLayout.CENTER);

        refrescar();
    }

    private void agregarAlCarrito() {
        Producto p = (Producto) cbProductos.getSelectedItem();
        if (p == null) return;
        int cant = (int) spCantidad.getValue();
        int yaEnCarrito = carrito.getOrDefault(p, 0);
        if (yaEnCarrito + cant > p.getExistencia()) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente. Disponible: " + p.getExistencia(), "Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        carrito.put(p, yaEnCarrito + cant);
        actualizarTablaCarrito();
    }

    private void actualizarTablaCarrito() {
        modeloCarrito.setRowCount(0);
        double total = 0;
        for (Map.Entry<Producto, Integer> entry : carrito.entrySet()) {
            Producto p = entry.getKey();
            int cant = entry.getValue();
            double sub = p.getPrecio() * cant;
            total += sub;
            modeloCarrito.addRow(new Object[]{p.getNombre(), cant, String.format("Q%.2f", p.getPrecio()), String.format("Q%.2f", sub)});
        }
        lblTotalVenta.setText(String.format("Total Venta: Q%.2f", total));
    }

    private void procesarVenta() {
        try {
            Venta v = sistema.registrarVenta(carrito, txtReservaOpcional.getText(), (MetodoPago) cbMetodoPago.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Venta registrada: " + v.getNumero() + "Total: Q" + String.format("%.2f", v.getTotal()));
            carrito.clear();
            txtReservaOpcional.setText("");
            actualizarTablaCarrito();
            onRefrescarGlobal.run();
        } catch (ValidacionNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error en Venta", JOptionPane.WARNING_MESSAGE);
        }
    }

    public final void refrescar() {
        modeloProductos.setRowCount(0);
        cbProductos.removeAllItems();
        for (Producto p : sistema.getProductos()) {
            modeloProductos.addRow(new Object[]{
                    p.getCodigo(), p.getNombre(), p.getCategoria(),
                    String.format("Q%.2f", p.getPrecio()), p.getExistencia(),
                    p.tieneStockBajo() ? "STOCK BAJO" : "OK"
            });
            if (p.isActivo() && p.getExistencia() > 0) {
                cbProductos.addItem(p);
            }
        }
    }
}
