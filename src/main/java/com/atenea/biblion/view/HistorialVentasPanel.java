package com.atenea.biblion.view;

import com.atenea.biblion.controller.VentaController;
import com.atenea.biblion.model.Venta;
import com.atenea.biblion.util.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel de Historial de Ventas, Consultas por Periodo y Arqueo de Turno.
 * Atiende el requisito de Consultas y Reportes #1 y el control de arqueo del Problema P2.
 */
public class HistorialVentasPanel extends JPanel {

    private final VentaController ventaController;

    private JComboBox<String> cbPeriodo;
    private JTextField txtFiltroTexto;
    private JTable tblVentas;
    private DefaultTableModel modeloTabla;

    private JLabel lblKpiTotal;
    private JLabel lblKpiTransacciones;
    private JLabel lblKpiEjemplares;
    private JLabel lblKpiTicketPromedio;

    public HistorialVentasPanel(VentaController ventaController) {
        this.ventaController = ventaController;
        initUI();
        cargarHistorial();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Zona Norte: KPI Cards y Filtros
        JPanel pnlNorte = new JPanel(new BorderLayout(10, 10));

        // 4 KPI Cards
        JPanel pnlKpis = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlKpis.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel kpiTotal = crearCardKpi("TOTAL LIQUIDADO", "$0.00", new Color(40, 167, 69));
        lblKpiTotal = (JLabel) ((JPanel) kpiTotal.getComponent(0)).getComponent(1);

        JPanel kpiTx = crearCardKpi("TRANSACCIONES", "0", new Color(33, 63, 115));
        lblKpiTransacciones = (JLabel) ((JPanel) kpiTx.getComponent(0)).getComponent(1);

        JPanel kpiUnidades = crearCardKpi("EJEMPLARES VENDIDOS", "0", new Color(142, 68, 173));
        lblKpiEjemplares = (JLabel) ((JPanel) kpiUnidades.getComponent(0)).getComponent(1);

        JPanel kpiPromedio = crearCardKpi("TICKET PROMEDIO", "$0.00", new Color(22, 160, 133));
        lblKpiTicketPromedio = (JLabel) ((JPanel) kpiPromedio.getComponent(0)).getComponent(1);

        pnlKpis.add(kpiTotal);
        pnlKpis.add(kpiTx);
        pnlKpis.add(kpiUnidades);
        pnlKpis.add(kpiPromedio);

        // Barra de Filtros
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlFiltros.setBorder(BorderFactory.createTitledBorder(" Filtros de Consulta y Periodo "));

        pnlFiltros.add(new JLabel("Periodo:"));
        cbPeriodo = new JComboBox<>(new String[]{"Todas las fechas", "Hoy (Jornada Activa)", "Últimos 7 días", "Este Mes"});
        cbPeriodo.addActionListener(e -> filtrarVentas());
        pnlFiltros.add(cbPeriodo);

        pnlFiltros.add(new JLabel("Filtrar por Título o ISBN:"));
        txtFiltroTexto = new JTextField(15);
        txtFiltroTexto.addActionListener(e -> filtrarVentas());
        pnlFiltros.add(txtFiltroTexto);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setBackground(new Color(41, 128, 185));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.addActionListener(e -> filtrarVentas());
        pnlFiltros.add(btnFiltrar);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            cbPeriodo.setSelectedIndex(0);
            txtFiltroTexto.setText("");
            cargarHistorial();
        });
        pnlFiltros.add(btnLimpiar);

        pnlNorte.add(pnlKpis, BorderLayout.NORTH);
        pnlNorte.add(pnlFiltros, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        // 2. Tabla Central de Transacciones
        String[] columnas = {"Folio Venta", "Fecha y Hora", "Título de la Obra", "ISBN", "Editorial", "Cantidad", "Precio Unitario", "Total Liquidado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblVentas = new JTable(modeloTabla);
        tblVentas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblVentas.setRowHeight(25);

        tblVentas.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblVentas.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblVentas.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblVentas.getColumnModel().getColumn(3).setPreferredWidth(125);
        tblVentas.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblVentas.getColumnModel().getColumn(5).setPreferredWidth(60);
        tblVentas.getColumnModel().getColumn(6).setPreferredWidth(90);
        tblVentas.getColumnModel().getColumn(7).setPreferredWidth(100);

        add(new JScrollPane(tblVentas), BorderLayout.CENTER);

        // 3. Barra Inferior con botón de Arqueo
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));

        JButton btnArqueo = new JButton("Generar Balance / Cierre de Turno");
        btnArqueo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnArqueo.setBackground(new Color(33, 63, 115));
        btnArqueo.setForeground(Color.WHITE);
        btnArqueo.addActionListener(e -> generarReporteArqueo());

        pnlAcciones.add(btnArqueo);
        add(pnlAcciones, BorderLayout.SOUTH);
    }

    private JPanel crearCardKpi(String titulo, String valorInicial, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel cont = new JPanel(new GridLayout(2, 1, 0, 3));
        cont.setOpaque(false);

        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(new Color(110, 120, 130));

        JLabel v = new JLabel(valorInicial);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(color);

        cont.add(t);
        cont.add(v);

        card.add(cont, BorderLayout.CENTER);

        JPanel strip = new JPanel();
        strip.setBackground(color);
        strip.setPreferredSize(new Dimension(4, 0));
        card.add(strip, BorderLayout.WEST);

        return card;
    }

    public void cargarHistorial() {
        List<Venta> ventas = ventaController.obtenerTodasLasVentas();
        mostrarVentasEnTabla(ventas);
    }

    private void filtrarVentas() {
        List<Venta> ventas = ventaController.obtenerTodasLasVentas();

        // Filtro por fecha
        int periodoIdx = cbPeriodo.getSelectedIndex();
        LocalDate hoy = LocalDate.now();

        if (periodoIdx == 1) { // Hoy
            LocalDateTime inicio = hoy.atStartOfDay();
            LocalDateTime fin = hoy.atTime(LocalTime.MAX);
            ventas = ventas.stream().filter(v -> !v.getFechaHora().isBefore(inicio) && !v.getFechaHora().isAfter(fin)).collect(Collectors.toList());
        } else if (periodoIdx == 2) { // Últimos 7 días
            LocalDateTime inicio = hoy.minusDays(7).atStartOfDay();
            ventas = ventas.stream().filter(v -> !v.getFechaHora().isBefore(inicio)).collect(Collectors.toList());
        } else if (periodoIdx == 3) { // Este mes
            LocalDateTime inicio = hoy.withDayOfMonth(1).atStartOfDay();
            ventas = ventas.stream().filter(v -> !v.getFechaHora().isBefore(inicio)).collect(Collectors.toList());
        }

        // Filtro por texto
        String texto = txtFiltroTexto.getText().trim().toLowerCase();
        if (!texto.isEmpty()) {
            ventas = ventas.stream().filter(v -> {
                if (v.getLibro() == null) return false;
                boolean matchTitulo = v.getLibro().getTitulo().toLowerCase().contains(texto);
                boolean matchIsbn = v.getLibro().getIsbn().toLowerCase().contains(texto);
                return matchTitulo || matchIsbn;
            }).collect(Collectors.toList());
        }

        mostrarVentasEnTabla(ventas);
    }

    private void mostrarVentasEnTabla(List<Venta> ventas) {
        modeloTabla.setRowCount(0);
        double totalRecaudado = 0.0;
        int totalEjemplares = 0;

        for (Venta v : ventas) {
            String titulo = (v.getLibro() != null) ? v.getLibro().getTitulo() : "N/A";
            String isbn = (v.getLibro() != null) ? v.getLibro().getIsbn() : "-";
            String editorial = (v.getLibro() != null && v.getLibro().getEditorial() != null) ? v.getLibro().getEditorial().getNombre() : "N/A";
            double precioUnit = (v.getLibro() != null) ? v.getLibro().getPrecio() : 0.0;

            totalRecaudado += v.getTotal();
            totalEjemplares += v.getCantidad();

            modeloTabla.addRow(new Object[]{
                    "#" + v.getIdVenta(),
                    FormatUtils.formatoFechaHora(v.getFechaHora()),
                    titulo,
                    isbn,
                    editorial,
                    v.getCantidad(),
                    FormatUtils.formatoMoneda(precioUnit),
                    FormatUtils.formatoMoneda(v.getTotal())
            });
        }

        lblKpiTotal.setText(FormatUtils.formatoMoneda(totalRecaudado));
        lblKpiTransacciones.setText(String.valueOf(ventas.size()));
        lblKpiEjemplares.setText(String.valueOf(totalEjemplares));

        double promedio = ventas.isEmpty() ? 0.0 : totalRecaudado / ventas.size();
        lblKpiTicketPromedio.setText(FormatUtils.formatoMoneda(promedio));
    }

    private void generarReporteArqueo() {
        int txCount = tblVentas.getRowCount();
        String total = lblKpiTotal.getText();
        String ejemplares = lblKpiEjemplares.getText();
        String promedio = lblKpiTicketPromedio.getText();
        String periodo = (String) cbPeriodo.getSelectedItem();

        String reporte = String.format(
                "===========================================================\n" +
                        "  LIBRERÍA Y DISTRIBUIDORA CULTURAL ATENEA\n" +
                        "  COMPROBANTE FORMAL DE ARQUEO Y CIERRE DE CAJA\n" +
                        "===========================================================\n" +
                        "Periodo Evaluado:        %s\n" +
                        "Fecha de Emisión:        %s\n" +
                        "-----------------------------------------------------------\n" +
                        "Transacciones Registradas: %d ventas\n" +
                        "Ejemplares Despachados:    %s unidades\n" +
                        "Ticket Promedio:           %s\n" +
                        "TOTAL LIQUIDADO EN CAJA:   %s\n" +
                        "-----------------------------------------------------------\n" +
                        "Política Operativa:\n" +
                        "El arqueo en efectivo del turno debe cuadrar exactamente\n" +
                        "con el total liquidado registrado en el sistema.\n" +
                        "===========================================================",
                periodo,
                FormatUtils.formatoFechaHora(LocalDateTime.now()),
                txCount,
                ejemplares,
                promedio,
                total
        );

        JTextArea ta = new JTextArea(reporte);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(540, 320));

        JOptionPane.showMessageDialog(this, scroll, "Balance de Arqueo de Turno", JOptionPane.INFORMATION_MESSAGE);
    }
}

