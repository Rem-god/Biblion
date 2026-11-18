package com.atenea.biblion.view;

import com.atenea.biblion.controller.ReporteController;
import com.atenea.biblion.dao.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diálogo de Administración de Copias de Seguridad.
 * Guarda los respaldos en la carpeta de Documentos del usuario.
 */
public class RespaldoDialog extends JDialog {

    private final ReporteController reporteController;

    private JLabel lblRutaDb;
    private JLabel lblTamanoDb;
    private JTable tblRespaldos;
    private DefaultTableModel modeloRespaldos;

    public RespaldoDialog(Frame parent, ReporteController reporteController) {
        super(parent, "Copias de Seguridad", true);
        this.reporteController = reporteController;

        initUI();
        cargarInformacionDb();
        listarRespaldos();
    }

    private void initUI() {
        setSize(580, 430);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // Header Minimalista
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(248, 250, 252));
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel lblTit = new JLabel("Copias de Seguridad");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Gestión y almacenamiento de respaldos en Documentos.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        pnlHeader.add(lblTit, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Panel Central
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Info actual de DB
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 4, 4));
        pnlInfo.setBorder(BorderFactory.createTitledBorder(" Ubicación de Datos "));

        lblRutaDb = new JLabel("Archivo: -");
        lblTamanoDb = new JLabel("Tamaño: -");

        pnlInfo.add(lblRutaDb);
        pnlInfo.add(lblTamanoDb);
        pnlCenter.add(pnlInfo, BorderLayout.NORTH);

        // Tabla de respaldos generados
        JPanel pnlLista = new JPanel(new BorderLayout(5, 5));
        pnlLista.setBorder(BorderFactory.createTitledBorder(" Respaldos Guardados "));

        String[] cols = {"Archivo", "Tamaño", "Fecha de Creación"};
        modeloRespaldos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblRespaldos = new JTable(modeloRespaldos);
        tblRespaldos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblRespaldos.setRowHeight(24);
        pnlLista.add(new JScrollPane(tblRespaldos), BorderLayout.CENTER);

        pnlCenter.add(pnlLista, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // Botones
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnCrearRespaldo = new JButton("Crear Respaldo Ahora");
        btnCrearRespaldo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCrearRespaldo.addActionListener(e -> generarRespaldo());

        JButton btnAbrirCarpeta = new JButton("Abrir Carpeta");
        btnAbrirCarpeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAbrirCarpeta.addActionListener(e -> abrirCarpeta());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        pnlBtns.add(btnCrearRespaldo);
        pnlBtns.add(btnAbrirCarpeta);
        pnlBtns.add(btnCerrar);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private void cargarInformacionDb() {
        String path = DatabaseManager.getInstance().getDbPath();
        File f = new File(path);
        lblRutaDb.setText("Archivo: " + f.getAbsolutePath());
        if (f.exists()) {
            lblTamanoDb.setText(String.format("Tamaño: %.2f KB | Modificado: %s",
                    f.length() / 1024.0, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(f.lastModified()))));
        } else {
            lblTamanoDb.setText("Tamaño: 0 KB (Nuevo)");
        }
    }

    private void listarRespaldos() {
        modeloRespaldos.setRowCount(0);
        File dir = new File(DatabaseManager.getCarpetaRespaldosPorDefecto());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".odb"));
            if (files != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                for (File f : files) {
                    modeloRespaldos.addRow(new Object[]{
                            f.getName(),
                            String.format("%.2f KB", f.length() / 1024.0),
                            sdf.format(new Date(f.lastModified()))
                    });
                }
            }
        }
    }

    private void generarRespaldo() {
        try {
            File destino = reporteController.ejecutarRespaldo(DatabaseManager.getCarpetaRespaldosPorDefecto());
            JOptionPane.showMessageDialog(this,
                    "Copia de seguridad creada con éxito en Documentos.\n\nArchivo: " + destino.getName() +
                            "\nCarpeta: " + destino.getParent(),
                    "Respaldo Exitoso", JOptionPane.INFORMATION_MESSAGE);
            listarRespaldos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar respaldo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirCarpeta() {
        try {
            File dir = new File(DatabaseManager.getCarpetaRespaldosPorDefecto());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Desktop.getDesktop().open(dir);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta: " + ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
