package com.canchapro.vista;

import com.canchapro.servicio.SistemaCanchaPro;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private final SistemaCanchaPro sistema;
    private final JTabbedPane pestanas;

    private final PanelDashboard panelDashboard;
    private final PanelNuevaReserva panelNuevaReserva;
    private final PanelDisponibilidad panelDisponibilidad;
    private final PanelClientes panelClientes;
    private final PanelCanchas panelCanchas;
    private final PanelPagos panelPagos;
    private final PanelTienda panelTienda;
    private final PanelReportes panelReportes;
    private final PanelConfiguracion panelConfiguracion;

    public VentanaPrincipal(SistemaCanchaPro sistema) {
        this.sistema = sistema;
        setTitle("CanchaPro - Sistema de Gestión y Reservas | Complejo Deportivo La 10");
        setSize(1180, 740);
        setMinimumSize(new Dimension(1024, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pestanas = new JTabbedPane(JTabbedPane.TOP);
        pestanas.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelDashboard = new PanelDashboard(sistema, this::refrescarTodo);
        panelNuevaReserva = new PanelNuevaReserva(sistema, this::refrescarTodo);
        panelDisponibilidad = new PanelDisponibilidad(sistema);
        panelClientes = new PanelClientes(sistema, this::refrescarTodo);
        panelCanchas = new PanelCanchas(sistema, this::refrescarTodo);
        panelPagos = new PanelPagos(sistema, this::refrescarTodo);
        panelTienda = new PanelTienda(sistema, this::refrescarTodo);
        panelReportes = new PanelReportes(sistema);
        panelConfiguracion = new PanelConfiguracion(sistema, this::actualizarVisibilidadModulos);

        pestanas.addTab("Inicio / Dashboard", panelDashboard);
        pestanas.addTab("Nueva Reserva", panelNuevaReserva);
        pestanas.addTab("Disponibilidad", panelDisponibilidad);
        pestanas.addTab("Clientes", panelClientes);
        pestanas.addTab("Canchas", panelCanchas);
        pestanas.addTab("Pagos y Saldos", panelPagos);
        pestanas.addTab("Tienda / Cafetería", panelTienda);
        pestanas.addTab("Reportes", panelReportes);
        pestanas.addTab("Configuración", panelConfiguracion);

        add(pestanas, BorderLayout.CENTER);
        actualizarVisibilidadModulos();

        if (!sistema.getErroresCarga().isEmpty()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Aviso al cargar archivos CSV:" + String.join("", sistema.getErroresCarga()),
                    "Validación de Persistencia", JOptionPane.WARNING_MESSAGE));
        }
    }

    private void actualizarVisibilidadModulos() {
        int idxTienda = pestanas.indexOfComponent(panelTienda);
        boolean activo = sistema.getConfiguracion().isModuloTiendaActivo();
        if (idxTienda >= 0) {
            pestanas.setEnabledAt(idxTienda, activo);
            pestanas.setTitleAt(idxTienda, activo ? "Tienda / Cafetería" : "Tienda (Desactivada)");
        }
        refrescarTodo();
    }

    public void refrescarTodo() {
        panelDashboard.refrescar();
        panelNuevaReserva.refrescar();
        panelDisponibilidad.refrescar();
        panelClientes.refrescar();
        panelCanchas.refrescar();
        panelPagos.refrescar();
        panelTienda.refrescar();
        panelReportes.refrescar();
    }
}
