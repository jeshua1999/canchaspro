package com.canchapro.persistencia;

import java.io.*;
import java.util.Properties;

public class ConfiguracionSistema {
    private final String rutaConfig;
    private boolean moduloTiendaActivo;
    private String nombreComplejo;
    private String operadorActual;

    public ConfiguracionSistema(String directorioDatos) {
        this.rutaConfig = directorioDatos + File.separator + "config.properties";
        cargar();
    }

    public final void cargar() {
        Properties props = new Properties();
        File archivo = new File(rutaConfig);
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error leyendo config.properties: " + e.getMessage());
            }
        }
        this.moduloTiendaActivo = Boolean.parseBoolean(props.getProperty("modulo_tienda", "true"));
        this.nombreComplejo = props.getProperty("nombre_complejo", "Complejo Deportivo La 10");
        this.operadorActual = props.getProperty("operador_actual", "Admin");
        guardar();
    }

    public void guardar() {
        Properties props = new Properties();
        props.setProperty("modulo_tienda", String.valueOf(moduloTiendaActivo));
        props.setProperty("nombre_complejo", nombreComplejo);
        props.setProperty("operador_actual", operadorActual);
        File archivo = new File(rutaConfig);
        archivo.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            props.store(fos, "Configuracion de CanchaPro");
        } catch (IOException e) {
            System.err.println("Error guardando config.properties: " + e.getMessage());
        }
    }

    public boolean isModuloTiendaActivo() { return moduloTiendaActivo; }
    public void setModuloTiendaActivo(boolean moduloTiendaActivo) {
        this.moduloTiendaActivo = moduloTiendaActivo;
        guardar();
    }

    public String getNombreComplejo() { return nombreComplejo; }
    public String getOperadorActual() { return operadorActual; }
    public void setOperadorActual(String operadorActual) {
        this.operadorActual = operadorActual;
        guardar();
    }
}
