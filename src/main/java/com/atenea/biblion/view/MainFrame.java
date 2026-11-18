package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.controller.ReporteController;
import com.atenea.biblion.controller.VentaController;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema Biblion para la Librería Atenea.
 * Diseño minimalista, limpio y moderno.
 */
public class MainFrame extends JFrame {

    private final EditorialController editorialController;
    private final LibroController libroController;
    private final VentaController ventaController;
    private final ReporteController reporteController;

    private JTabbedPane tabbedPane;
    private VentasPanel ventasPanel;
    private LibrosPanel librosPanel;
    private EditorialesPanel editorialesPanel;
    private InventarioCriticoPanel inventarioCriticoPanel;
    private HistorialVentasPanel historialVentasPanel;

    private JLabel lblStatusDb;
    private JLabel lblStatusKpis;

    public MainFrame() {
        this.editorialController = new EditorialController();
        this.libroController = new LibroController();
        this.ventaController = new VentaController();
        this.reporteController = new ReporteController();

        initUI();
        actualizarKpisBarraEstado();
    }

    private void initUI() {
        setTitle("Biblion — Librería Atenea");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Encabezado Minimalista
        add(crearEncabezadoMinimalista(), BorderLayout.NORTH);

        // 2. Pestañas de Navegación Limpias
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ventasPanel = new VentasPanel(ventaController, this::notificarCambioGlobal);
        librosPanel = new LibrosPanel(libroController, editorialController, this::notificarCambioGlobal);
        editorialesPanel = new EditorialesPanel(editorialController, this::notificarCambioGlobal);
        inventarioCriticoPanel = new InventarioCriticoPanel(reporteController, libroController, this::notificarCambioGlobal);
        historialVentasPanel = new HistorialVentasPanel(ventaController);

        tabbedPane.addTab("Punto de Venta", ventasPanel);
        tabbedPane.addTab("Catálogo de Libros", librosPanel);
        tabbedPane.addTab("Casas Editoriales", editorialesPanel);
        tabbedPane.addTab("Inventario Crítico", inventarioCriticoPanel);
        tabbedPane.addTab("Historial y Arqueo", historialVentasPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) {
                ventasPanel.enfocarScanner();
            } else if (idx == 1) {
                librosPanel.cargarLibros();
            } else if (idx == 2) {
                editorialesPanel.cargarEditoriales();
            } else if (idx == 3) {
                inventarioCriticoPanel.cargarReporte();
            } else if (idx == 4) {
                historialVentasPanel.cargarHistorial();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Barra Inferior de Estado Minimalista
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    private JPanel crearEncabezadoMinimalista() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        // Título y Subtítulo Minimalistas
        JPanel pnlTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTitulos.setOpaque(false);

        JLabel lblLogo = new JLabel("Biblion");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Librería y Distribuidora Cultural Atenea");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        pnlTitulos.add(lblLogo);
        pnlTitulos.add(lblSub);

        // Zona Derecha
        JPanel pnlDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        pnlDerecha.setOpaque(false);

        JLabel lblAlumno = new JLabel("Román Emmanuel Castañeda Barboza • TecNM Jalisco");
        lblAlumno.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAlumno.setForeground(new Color(100, 116, 139));

        JButton btnBackup = new JButton("Copias de Seguridad");
        btnBackup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBackup.setFocusPainted(false);
        btnBackup.addActionListener(e -> abrirDialogoRespaldo());

        pnlDerecha.add(lblAlumno);
        pnlDerecha.add(btnBackup);

        header.add(pnlTitulos, BorderLayout.WEST);
        header.add(pnlDerecha, BorderLayout.EAST);

        return header;
    }

    private JPanel crearBarraEstado() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(7, 20, 7, 20)
        ));

        lblStatusDb = new JLabel("🟢 Sistema en línea");
        lblStatusDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusDb.setForeground(new Color(40, 130, 60));

        lblStatusKpis = new JLabel("Cargando información...");
        lblStatusKpis.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusKpis.setForeground(new Color(71, 85, 105));

        bar.add(lblStatusDb, BorderLayout.WEST);
        bar.add(lblStatusKpis, BorderLayout.EAST);

        return bar;
    }

    private void actualizarKpisBarraEstado() {
        long titulos = reporteController.getTotalTitulos();
        long editoriales = reporteController.getTotalEditoriales();
        long ventas = reporteController.getTotalVentasRealizadas();
        lblStatusKpis.setText(String.format("Catálogo: %d títulos  |  Editoriales: %d  |  Ventas registradas: %d",
                titulos, editoriales, ventas));
    }

    private void notificarCambioGlobal() {
        actualizarKpisBarraEstado();
        librosPanel.cargarEditorialesEnFiltro();
        editorialesPanel.cargarEditoriales();
    }

    private void abrirDialogoRespaldo() {
        RespaldoDialog dlg = new RespaldoDialog(this, reporteController);
        dlg.setVisible(true);
    }
}
