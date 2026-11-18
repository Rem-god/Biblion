package com.atenea.biblion.view;

import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.controller.ReporteController;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.util.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel de Inventario Crítico y Gestión de Reposición a Editoriales.
 * Atiende el requisito de Consultas y Reportes #3 de Tópicos de Bases de Datos
 * y la estrategia del Problema P3 del Diagnóstico de Gestión de TI.
 */
public class InventarioCriticoPanel extends JPanel {

    private final ReporteController reporteController;
    private final LibroController libroController;
    private final Runnable onDataChangedCallback;

    private JSpinner spUmbral;
    private JTable tblCriticos;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalCriticos;
    private JLabel lblTotalAgotados;

    public InventarioCriticoPanel(ReporteController reporteController, LibroController libroController, Runnable onDataChanged) {
        this.reporteController = reporteController;
        this.libroController = libroController;
        this.onDataChangedCallback = onDataChanged;

        initUI();
        cargarReporte();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Barra Superior con Umbral y KPI Cards
        JPanel pnlNorte = new JPanel(new BorderLayout(10, 10));

        // KPI Badges
        JPanel pnlKpis = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlKpis.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel kpiCriticos = crearCardKpi("TÍTULOS EN INVENTARIO CRÍTICO", "0 títulos", new Color(230, 126, 34));
        lblTotalCriticos = (JLabel) ((JPanel) kpiCriticos.getComponent(0)).getComponent(1);

        JPanel kpiAgotados = crearCardKpi("TÍTULOS COMPLETAMENTE AGOTADOS (STOCK 0)", "0 títulos", new Color(231, 76, 60));
        lblTotalAgotados = (JLabel) ((JPanel) kpiAgotados.getComponent(0)).getComponent(1);

        pnlKpis.add(kpiCriticos);
        pnlKpis.add(kpiAgotados);

        // Barra de control de umbral
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlFiltros.setBorder(BorderFactory.createTitledBorder(" Parámetros de Monitoreo de Desabasto "));

        pnlFiltros.add(new JLabel("Umbral de Stock Mínimo Operativo:"));
        spUmbral = new JSpinner(new SpinnerNumberModel(4, 0, 100, 1));
        spUmbral.setPreferredSize(new Dimension(65, 28));
        spUmbral.addChangeListener(e -> cargarReporte());
        pnlFiltros.add(spUmbral);

        pnlFiltros.add(new JLabel("unidades o menos."));

        JButton btnActualizar = new JButton("Actualizar Alertas");
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setBackground(new Color(41, 128, 185));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.addActionListener(e -> cargarReporte());
        pnlFiltros.add(btnActualizar);

        pnlNorte.add(pnlKpis, BorderLayout.NORTH);
        pnlNorte.add(pnlFiltros, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        // 2. Tabla Central de Títulos en Riesgo
        String[] columnas = {"ID", "ISBN", "Título de la Obra", "Editorial Proveedora", "Correo Comercial", "Precio", "Stock Actual", "Nivel de Alerta"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblCriticos = new JTable(modeloTabla);
        tblCriticos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCriticos.setRowHeight(26);
        tblCriticos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblCriticos.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblCriticos.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblCriticos.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblCriticos.getColumnModel().getColumn(3).setPreferredWidth(160);
        tblCriticos.getColumnModel().getColumn(4).setPreferredWidth(190);
        tblCriticos.getColumnModel().getColumn(5).setPreferredWidth(75);
        tblCriticos.getColumnModel().getColumn(6).setPreferredWidth(65);
        tblCriticos.getColumnModel().getColumn(7).setPreferredWidth(110);

        tblCriticos.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String val = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    if ("AGOTADO".equals(val)) {
                        c.setForeground(new Color(220, 53, 69));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(210, 120, 0));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                }
                return c;
            }
        });

        add(new JScrollPane(tblCriticos), BorderLayout.CENTER);

        // 3. Barra Inferior con Acciones de Reposición (Problema P3)
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton btnRecepcion = new JButton("📥 Recibir Mercancía (+Stock)");
        btnRecepcion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRecepcion.setBackground(new Color(40, 167, 69));
        btnRecepcion.setForeground(Color.WHITE);
        btnRecepcion.setToolTipText("Coteja la remisión física de la editorial e incrementa de inmediato el stock (Estrategia P3)");
        btnRecepcion.addActionListener(e -> recibirMercanciaEditorial());

        JButton btnGenerarPedido = new JButton("📋 Copiar Reporte de Reposición a Proveedores");
        btnGenerarPedido.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerarPedido.setBackground(new Color(33, 63, 115));
        btnGenerarPedido.setForeground(Color.WHITE);
        btnGenerarPedido.setToolTipText("Genera una orden consolidada por editorial con correos de contacto para resurtido");
        btnGenerarPedido.addActionListener(e -> generarReporteReposicion());

        pnlAcciones.add(btnRecepcion);
        pnlAcciones.add(btnGenerarPedido);

        add(pnlAcciones, BorderLayout.SOUTH);
    }

    private JPanel crearCardKpi(String titulo, String valorInicial, Color colorBarra) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel contenido = new JPanel(new GridLayout(2, 1, 0, 4));
        contenido.setOpaque(false);

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTit.setForeground(new Color(110, 120, 130));

        JLabel lblVal = new JLabel(valorInicial);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(colorBarra);

        contenido.add(lblTit);
        contenido.add(lblVal);

        card.add(contenido, BorderLayout.CENTER);

        JPanel barraLateral = new JPanel();
        barraLateral.setBackground(colorBarra);
        barraLateral.setPreferredSize(new Dimension(5, 0));
        card.add(barraLateral, BorderLayout.WEST);

        return card;
    }

    public void cargarReporte() {
        int umbral = ((Number) spUmbral.getValue()).intValue();
        List<Libro> criticos = reporteController.obtenerInventarioCritico(umbral);

        modeloTabla.setRowCount(0);
        int cantAgotados = 0;

        for (Libro l : criticos) {
            String editorial = (l.getEditorial() != null) ? l.getEditorial().getNombre() : "N/A";
            String correo = (l.getEditorial() != null) ? l.getEditorial().getCorreo() : "N/A";
            String alerta;
            if (l.getStock() <= 0) {
                alerta = "AGOTADO";
                cantAgotados++;
            } else {
                alerta = "CRÍTICO (" + l.getStock() + " disp.)";
            }

            modeloTabla.addRow(new Object[]{
                    l.getIdLibro(),
                    l.getIsbn(),
                    l.getTitulo(),
                    editorial,
                    correo,
                    FormatUtils.formatoMoneda(l.getPrecio()),
                    l.getStock(),
                    alerta
            });
        }

        lblTotalCriticos.setText(criticos.size() + " títulos");
        lblTotalAgotados.setText(cantAgotados + " títulos");
    }

    /**
     * Recepción física de paquetería de editoriales e incremento inmediato de stock (Estrategia P3).
     */
    private void recibirMercanciaEditorial() {
        int fila = tblCriticos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione de la tabla el título cuya paquetería física va a recepcionar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idLibro = (long) modeloTabla.getValueAt(fila, 0);
        String titulo = (String) modeloTabla.getValueAt(fila, 2);
        String editorial = (String) modeloTabla.getValueAt(fila, 3);
        int stockActual = (int) modeloTabla.getValueAt(fila, 6);

        String entrada = JOptionPane.showInputDialog(this,
                "Recepción de Mercancía de Editorial:\n" +
                        "Obra: " + titulo + "\n" +
                        "Editorial: " + editorial + "\n" +
                        "Stock actual en sistema: " + stockActual + "\n\n" +
                        "Ingrese la cantidad de ejemplares recibidos físicamente en remisión:",
                "Recepción de Paquete Editorial", JOptionPane.QUESTION_MESSAGE);

        if (entrada != null && !entrada.trim().isEmpty()) {
            try {
                int cantidad = Integer.parseInt(entrada.trim());
                if (cantidad <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad recibida debe ser mayor a cero.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Libro libro = libroController.buscarPorId(idLibro);
                libro.incrementarStock(cantidad);
                libroController.actualizarLibro(
                        libro.getIdLibro(), libro.getIsbn(), libro.getTitulo(), libro.getAutor(),
                        libro.getPrecio(), libro.getStock(), libro.getEditorial().getIdEditorial()
                );

                JOptionPane.showMessageDialog(this,
                        "¡Recepción registrada exitosamente!\nSe ingresaron " + cantidad + " unidades.\nNuevo stock: " + libro.getStock(),
                        "Stock Actualizado", JOptionPane.INFORMATION_MESSAGE);

                cargarReporte();
                if (onDataChangedCallback != null) onDataChangedCallback.run();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Genera un pedido formal consolidado agrupado por editorial para resurtido.
     */
    private void generarReporteReposicion() {
        int umbral = ((Number) spUmbral.getValue()).intValue();
        List<Libro> criticos = reporteController.obtenerInventarioCritico(umbral);

        if (criticos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay títulos con inventario crítico en este momento.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Map<String, StringBuilder> ordenPorEditorial = new HashMap<>();
        for (Libro l : criticos) {
            Editorial ed = l.getEditorial();
            String key = (ed != null) ? ed.getNombre() + " (" + ed.getCorreo() + ")" : "Editorial No Asignada";
            ordenPorEditorial.putIfAbsent(key, new StringBuilder());
            int sugerido = (15 - l.getStock());
            if (sugerido < 5) sugerido = 5;
            ordenPorEditorial.get(key).append(String.format("  - [%s] %s | Stock Actual: %d | Pedido Sugerido: %d ejemplares\n",
                    l.getIsbn(), l.getTitulo(), l.getStock(), sugerido));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================\n");
        sb.append("LIBRERÍA Y DISTRIBUIDORA CULTURAL ATENEA - SOLICITUD FORMAL DE REPOSICIÓN\n");
        sb.append("=========================================================================\n\n");

        for (Map.Entry<String, StringBuilder> entry : ordenPorEditorial.entrySet()) {
            sb.append("EDITORIAL / PROVEEDOR: ").append(entry.getKey()).append("\n");
            sb.append("-------------------------------------------------------------------------\n");
            sb.append(entry.getValue().toString());
            sb.append("\n");
        }

        // Copiar al portapapeles
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(650, 400));

        JOptionPane.showMessageDialog(this, scroll, "Reporte de Reposición Copiado al Portapapeles", JOptionPane.INFORMATION_MESSAGE);
    }
}

