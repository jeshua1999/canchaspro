package com.canchapro.vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EstilosUI {
    public static final Color PRIMARIO = new Color(22, 101, 52);      // Verde césped oscuro
    public static final Color SECUNDARIO = new Color(34, 197, 94);    // Verde vibrante
    public static final Color FONDO = new Color(244, 246, 248);
    public static final Color BLANCO = Color.WHITE;
    public static final Color TEXTO = new Color(30, 41, 59);
    public static final Color PELIGRO = new Color(220, 38, 38);
    public static final Color ADVERTENCIA = new Color(217, 119, 6);

    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    public static JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color bordeSuperior) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 1, 1, 1, bordeSuperior),
                new EmptyBorder(12, 15, 12, 15)
        ));
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTit.setForeground(Color.GRAY);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(TEXTO);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }
}
