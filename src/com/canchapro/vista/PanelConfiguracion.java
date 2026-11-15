package com.canchapro.vista;

import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelConfiguracion extends JPanel {
    private final SistemaCanchaPro sistema;
    private final Runnable onConfiguracionCambiada;

    public PanelConfiguracion(SistemaCanchaPro sistema, Runnable onConfiguracionCambiada) {
        this.sistema = sistema;
        this.onConfiguracionCambiada = onConfiguracionCambiada;
        setLayout(new BorderLayout(15, 15));
        setBackground(EstilosUI.FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Configuración del Sistema y Módulos");
        titulo.setFont(EstilosUI.FUENTE_TITULO);
        add(titulo, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(EstilosUI.BLANCO);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;

        JCheckBox chkTienda = new JCheckBox("Activar Módulo Adicional: Tienda / Cafetería (modulo_tienda=true)");
        chkTienda.setFont(EstilosUI.FUENTE_SUBTITULO);
        chkTienda.setBackground(EstilosUI.BLANCO);
        chkTienda.setSelected(sistema.getConfiguracion().isModuloTiendaActivo());
        chkTienda.addActionListener(e -> {
            sistema.getConfiguracion().setModuloTiendaActivo(chkTienda.isSelected());
            onConfiguracionCambiada.run();
            JOptionPane.showMessageDialog(this,
                    "Módulo Tienda/Cafetería " + (chkTienda.isSelected() ? "ACTIVADO" : "DESACTIVADO") +
                    ". La configuración se guardó en datos/config.properties.");
        });
        card.add(chkTienda, gbc);

        gbc.gridy++;
        JPanel pOp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pOp.setOpaque(false);
        pOp.add(new JLabel("Operador en turno (auditoría de cancelaciones):  "));
        JTextField txtOperador = new JTextField(sistema.getConfiguracion().getOperadorActual(), 15);
        JButton btnGuardarOp = EstilosUI.crearBoton("Actualizar Operador", EstilosUI.PRIMARIO);
        btnGuardarOp.addActionListener(e -> {
            sistema.getConfiguracion().setOperadorActual(txtOperador.getText());
            JOptionPane.showMessageDialog(this, "Operador actualizado.");
        });
        pOp.add(txtOperador);
        pOp.add(Box.createHorizontalStrut(8));
        pOp.add(btnGuardarOp);
        card.add(pOp, gbc);

        gbc.gridy++;
        JPanel pArchivos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pArchivos.setOpaque(false);
        JButton btnRecargar = EstilosUI.crearBoton("Recargar Archivos CSV", new Color(37, 99, 235));
        btnRecargar.addActionListener(e -> {
            sistema.recargarTodo();
            onConfiguracionCambiada.run();
            if (!sistema.getErroresCarga().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Se detectaron líneas corruptas:" + String.join("", sistema.getErroresCarga()),"Advertencia de Integridad", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Archivos CSV recargados sin errores.");
            }
        });

        JButton btnRestaurar = EstilosUI.crearBoton("Restaurar Datos de Prueba Iniciales", EstilosUI.PELIGRO);
        btnRestaurar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Desea sobrescribir los CSV con los datos de demostración?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                sistema.restaurarDatosDePrueba();
                onConfiguracionCambiada.run();
                JOptionPane.showMessageDialog(this, "Datos de prueba restaurados.");
            }
        });

        pArchivos.add(btnRecargar);
        pArchivos.add(btnRestaurar);
        card.add(pArchivos, gbc);

        add(card, BorderLayout.CENTER);
    }
}
