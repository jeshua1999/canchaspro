package com.canchapro.vista;

import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.EstadoCancha;
import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PanelDisponibilidad extends JPanel {
    private final SistemaCanchaPro sistema;
    private JTextField txtFecha;
    private DefaultTableModel modelo;

    public PanelDisponibilidad(SistemaCanchaPro sistema) {
        this.sistema = sistema;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        JLabel titulo = new JLabel("Matriz de Disponibilidad por Cancha y Horario");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        top.add(titulo);

        top.add(new JLabel("   Fecha (AAAA-MM-DD):"));
        txtFecha = new JTextField(LocalDate.now().toString(), 10);
        top.add(txtFecha);

        JButton btnVer = EstilosUI.crearBoton("Actualizar Parrilla", EstilosUI.PRIMARIO);
        btnVer.addActionListener(e -> refrescar());
        top.add(btnVer);
        add(top, BorderLayout.NORTH);

        modelo = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(EstilosUI.FUENTE_NORMAL);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        refrescar();
    }

    public final void refrescar() {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Use formato AAAA-MM-DD.");
            return;
        }

        List<Cancha> canchas = sistema.getCanchas();
        String[] columnas = new String[canchas.size() + 1];
        columnas[0] = "Franja Horaria";
        for (int i = 0; i < canchas.size(); i++) {
            columnas[i + 1] = canchas.get(i).getNombre() + " (" + canchas.get(i).getTipo() + ")";
        }

        modelo.setDataVector(new Object[0][0], columnas);

        for (int h = 8; h < 23; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            LocalTime fin = inicio.plusHours(1);
            Object[] fila = new Object[canchas.size() + 1];
            fila[0] = String.format("%02d:00 - %02d:00", h, h + 1);

            for (int i = 0; i < canchas.size(); i++) {
                Cancha c = canchas.get(i);
                if (c.getEstado() != EstadoCancha.DISPONIBLE) {
                    fila[i + 1] = "[" + c.getEstado() + "]";
                } else if (inicio.isBefore(c.getHoraApertura()) || fin.isAfter(c.getHoraCierre())) {
                    fila[i + 1] = "CERRADO";
                } else if (sistema.existeTraslape(c, fecha, inicio, fin)) {
                    fila[i + 1] = "OCUPADO";
                } else {
                    double tarifa = sistema.calcularCostoCancha(c, fecha, inicio, 1);
                    fila[i + 1] = "LIBRE (Q" + String.format("%.0f", tarifa) + ")";
                }
            }
            modelo.addRow(fila);
        }
    }
}
