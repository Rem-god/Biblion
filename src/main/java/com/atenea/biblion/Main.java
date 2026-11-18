package com.atenea.biblion;

import com.atenea.biblion.dao.DatabaseManager;
import com.atenea.biblion.util.DataSeeder;
import com.atenea.biblion.view.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Punto de entrada principal de la aplicación Biblion.
 * Desarrollado para la materia de Tópicos de Bases de Datos y Gestión de TI.
 * Institución: Tecnológico Superior de Jalisco.
 * Alumno: Román Emmanuel Castañeda Barboza (230110841).
 */
public class Main {

    public static void main(String[] args) {
        // Configurar Look & Feel moderno (FlatLaf)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo cargar FlatLaf, usando estilo del sistema: " + ex.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // Inicializar persistencia ObjectDB
        System.out.println("Inicializando motor de base de datos orientada a objetos ObjectDB...");
        DatabaseManager.getInstance();

        // Sembrar datos iniciales si la base de datos está vacía (Estrategia P1)
        System.out.println("Verificando catálogo inicial de la Librería Atenea...");
        DataSeeder.sembrarDatosSiEsNecesario();

        // Lanzar interfaz gráfica en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            System.out.println("Sistema Biblion iniciado correctamente.");
        });
    }
}

