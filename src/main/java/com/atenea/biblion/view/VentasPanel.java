package com.atenea.biblion.view;

import com.atenea.biblion.controller.VentaController;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.model.Venta;
import com.atenea.biblion.util.FormatUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel de Punto de Venta (POS) y Caja.
 * Incorpora:
 * 1. Entrada ágil por código de barras (foco automático constante en ISBN).
 * 2. Bloqueo visual inmediato de venta con stock en cero o insuficiente.
 * 3. Descuento transaccional en ObjectDB.
 * 4. Trazabilidad de ventas y arqueo de caja de la jornada.
 */
public class VentasPanel extends JPanel {

    private final VentaController ventaController;
    private final Runnable onDataChangedCallback;

    // Componentes de búsqueda rápida (Pistoleo)
    private JTextField txtIsbn;
    private JButton btnBuscar;

    // Ficha del Libro Seleccionado
    private JLabel lblTitulo;
    private JLabel lblAutor;
    private JLabel lblEditorial;
    private JLabel lblPrecioUnitario;
    private JLabel lblStockDisponible;
    private JPanel pnlAlertaBloqueo;
    private JLabel lblMensajeBloqueo;

    // Controles de Transacción
    private JSpinner spCantidad;
    private JLabel lblTotalCalculado;
    private JButton btnProcesarVenta;
    private JButton btnLimpiar;

    // Tabla de Arqueo / Últimas Ventas
    private JTable tblVentasRecientes;
    private DefaultTableModel modeloTablaVentas;
    private JLabel lblTotalArqueo;

    private Libro libroActual = null;

    public VentasPanel(VentaController ventaController, Runnable onDataChanged) {
        this.ventaController = ventaController;
        this.onDataChangedCallback = onDataChanged;
        initUI();
        cargarVentasRecientes();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Panel Principal Dividido: Izquierda (POS / Despacho), Derecha (Arqueo / Ventas Recientes)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelDespacho(), crearPanelArqueo());
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(620);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Construye el panel de despacho rápido con lector de código de barras y bloqueo de stock.
     */
    private JPanel crearPanelDespacho() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 1. Zona Superior: Entrada ágil por código de barras
        JPanel pnlScanner = new JPanel(new BorderLayout(8, 8));
        pnlScanner.setBorder(BorderFactory.createTitledBorder(" Entrada por Código de Barras "));

        JPanel pnlInput = new JPanel(new BorderLayout(8, 8));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        txtIsbn = new JTextField();
        txtIsbn.setFont(new Font("Consolas", Font.BOLD, 16));
        txtIsbn.setToolTipText("Escanee el código de barras o ingrese el ISBN y presione ENTER");

        // Al presionar Enter, se dispara la búsqueda automática (pistoleo directo)
        txtIsbn.addActionListener(e -> buscarLibroPorIsbn());

        btnBuscar = new JButton("Buscar Ejemplar (Enter)");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.setBackground(new Color(41, 128, 185));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> buscarLibroPorIsbn());

        pnlInput.add(new JLabel("ISBN / Código de Barras: "), BorderLayout.WEST);
        pnlInput.add(txtIsbn, BorderLayout.CENTER);
        pnlInput.add(btnBuscar, BorderLayout.EAST);

        JLabel lblTip = new JLabel("<html><small style='color: #64748b;'>* El cursor permanece en este campo para escanear libros consecutivamente.</small></html>");
        lblTip.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        pnlScanner.add(pnlInput, BorderLayout.CENTER);
        pnlScanner.add(lblTip, BorderLayout.SOUTH);

        // 2. Zona Central: Ficha del Libro y Banner de Alerta / Bloqueo
        JPanel pnlFicha = new JPanel(new BorderLayout(10, 10));
        pnlFicha.setBorder(BorderFactory.createTitledBorder(" Detalle del Ejemplar Seleccionado "));

        // Banner de Bloqueo Visual (Ajuste Funcional #2)
        pnlAlertaBloqueo = new JPanel(new BorderLayout(5, 5));
        pnlAlertaBloqueo.setBackground(new Color(255, 235, 235));
        pnlAlertaBloqueo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        lblMensajeBloqueo = new JLabel("¡BLOQUEO DE VENTA! El ejemplar no cuenta con existencias en almacén.");
        lblMensajeBloqueo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMensajeBloqueo.setForeground(new Color(180, 20, 30));
        pnlAlertaBloqueo.add(lblMensajeBloqueo, BorderLayout.CENTER);
        pnlAlertaBloqueo.setVisible(false); // Oculto hasta que se detecte stock 0

        JPanel pnlDetalles = new JPanel(new GridLayout(5, 2, 8, 8));
        pnlDetalles.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        pnlDetalles.add(crearEtiquetaNegrita("Título:"));
        lblTitulo = new JLabel("-");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlDetalles.add(lblTitulo);

        pnlDetalles.add(crearEtiquetaNegrita("Autor:"));
        lblAutor = new JLabel("-");
        pnlDetalles.add(lblAutor);

        pnlDetalles.add(crearEtiquetaNegrita("Editorial:"));
        lblEditorial = new JLabel("-");
        pnlDetalles.add(lblEditorial);

        pnlDetalles.add(crearEtiquetaNegrita("Precio Unitario:"));
        lblPrecioUnitario = new JLabel("$0.00");
        lblPrecioUnitario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrecioUnitario.setForeground(new Color(33, 100, 45));
        pnlDetalles.add(lblPrecioUnitario);

        pnlDetalles.add(crearEtiquetaNegrita("Stock en Mostrador:"));
        lblStockDisponible = new JLabel("-");
        lblStockDisponible.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlDetalles.add(lblStockDisponible);

        pnlFicha.add(pnlAlertaBloqueo, BorderLayout.NORTH);
        pnlFicha.add(pnlDetalles, BorderLayout.CENTER);

        // 3. Zona Inferior: Controles de Cobro
        JPanel pnlCobro = new JPanel(new BorderLayout(10, 10));
        pnlCobro.setBorder(BorderFactory.createTitledBorder(" Operación de Caja y Despacho "));

        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlControles.add(new JLabel("Cantidad a Comprar:"));

        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spCantidad.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spCantidad.setPreferredSize(new Dimension(80, 32));
        spCantidad.setEnabled(false);
        spCantidad.addChangeListener(e -> actualizarTotalCalculado());
        pnlControles.add(spCantidad);

        pnlControles.add(new JLabel("Total a Cobrar:"));
        lblTotalCalculado = new JLabel("$0.00");
        lblTotalCalculado.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCalculado.setForeground(new Color(20, 120, 50));
        pnlControles.add(lblTotalCalculado);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnProcesarVenta = new JButton("COBRAR Y DESPACHAR VENTA");
        btnProcesarVenta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProcesarVenta.setBackground(new Color(40, 167, 69));
        btnProcesarVenta.setForeground(Color.WHITE);
        btnProcesarVenta.setEnabled(false);
        btnProcesarVenta.addActionListener(e -> formalizarVenta());

        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnProcesarVenta);

        pnlCobro.add(pnlControles, BorderLayout.CENTER);
        pnlCobro.add(pnlBotones, BorderLayout.SOUTH);

        panel.add(pnlScanner, BorderLayout.NORTH);
        panel.add(pnlFicha, BorderLayout.CENTER);
        panel.add(pnlCobro, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Construye el panel derecho para monitoreo de ventas en curso y arqueo de caja.
     */
    private JPanel crearPanelArqueo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(" Trazabilidad y Arqueo de Ventas (Turno Activo) "));

        String[] columnas = {"ID", "Fecha/Hora", "Título", "Cant.", "Total"};
        modeloTablaVentas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblVentasRecientes = new JTable(modeloTablaVentas);
        tblVentasRecientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblVentasRecientes.setRowHeight(24);
        tblVentasRecientes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblVentasRecientes.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblVentasRecientes.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblVentasRecientes.getColumnModel().getColumn(3).setPreferredWidth(45);
        tblVentasRecientes.getColumnModel().getColumn(4).setPreferredWidth(75);

        JScrollPane scroll = new JScrollPane(tblVentasRecientes);
        panel.add(scroll, BorderLayout.CENTER);

        // Barra inferior de arqueo de caja
        JPanel pnlArqueoBar = new JPanel(new BorderLayout(10, 10));
        pnlArqueoBar.setBackground(new Color(245, 248, 252));
        pnlArqueoBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 240)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        lblTotalArqueo = new JLabel("Total Recaudado en Caja: $0.00");
        lblTotalArqueo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalArqueo.setForeground(new Color(33, 63, 115));

        JButton btnRefrescar = new JButton("Actualizar Arqueo");
        btnRefrescar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefrescar.addActionListener(e -> cargarVentasRecientes());

        pnlArqueoBar.add(lblTotalArqueo, BorderLayout.WEST);
        pnlArqueoBar.add(btnRefrescar, BorderLayout.EAST);

        panel.add(pnlArqueoBar, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel crearEtiquetaNegrita(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    /**
     * Búsqueda por ISBN con pistoleo.
     */
    private void buscarLibroPorIsbn() {
        String isbn = txtIsbn.getText().trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor escanee o escriba un código ISBN.", "Atención", JOptionPane.WARNING_MESSAGE);
            enfocarScanner();
            return;
        }

        try {
            libroActual = ventaController.buscarPorIsbnParaVenta(isbn);
            mostrarFichaLibro(libroActual);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Búsqueda de Ejemplar", JOptionPane.ERROR_MESSAGE);
            limpiarFormulario();
        }
    }

    /**
     * Muestra los datos del libro y aplica la regla de bloqueo de stock en cero.
     */
    private void mostrarFichaLibro(Libro libro) {
        lblTitulo.setText(libro.getTitulo());
        lblAutor.setText(libro.getAutor());
        lblEditorial.setText(libro.getEditorial() != null ? libro.getEditorial().getNombre() : "N/A");
        lblPrecioUnitario.setText(FormatUtils.formatoMoneda(libro.getPrecio()));

        int stock = libro.getStock();

        // REGLA DE BLOQUEO DE VENTA CON STOCK EN CERO (Ajuste Funcional #2)
        if (stock <= 0) {
            lblStockDisponible.setText("0 (AGOTADO)");
            lblStockDisponible.setForeground(new Color(220, 53, 69));

            pnlAlertaBloqueo.setVisible(true);
            lblMensajeBloqueo.setText("<html>⛔ <b>BLOQUEO DE VENTA:</b> El ejemplar se encuentra completamente <b>AGOTADO</b>.<br>El sistema prohíbe formalizar transacciones sin existencias en almacén.</html>");

            spCantidad.setEnabled(false);
            spCantidad.setValue(0);
            lblTotalCalculado.setText(FormatUtils.formatoMoneda(0.0));

            btnProcesarVenta.setEnabled(false);
            btnProcesarVenta.setBackground(new Color(180, 180, 180));
            btnProcesarVenta.setText("VENTA BLOQUEADA (STOCK 0)");
        } else {
            // Libro con existencias válidas
            pnlAlertaBloqueo.setVisible(false);
            lblStockDisponible.setText(stock + " unidad(es) disponible(s)");
            lblStockDisponible.setForeground(stock <= 4 ? new Color(210, 120, 0) : new Color(30, 130, 60));

            spCantidad.setEnabled(true);
            SpinnerNumberModel model = new SpinnerNumberModel(1, 1, stock, 1);
            spCantidad.setModel(model);

            btnProcesarVenta.setEnabled(true);
            btnProcesarVenta.setBackground(new Color(40, 167, 69));
            btnProcesarVenta.setText("COBRAR Y DESPACHAR VENTA");

            actualizarTotalCalculado();
        }
    }

    private void actualizarTotalCalculado() {
        if (libroActual != null && libroActual.getStock() > 0) {
            int cantidad = ((Number) spCantidad.getValue()).intValue();
            double total = cantidad * libroActual.getPrecio();
            lblTotalCalculado.setText(FormatUtils.formatoMoneda(total));
        } else {
            lblTotalCalculado.setText(FormatUtils.formatoMoneda(0.0));
        }
    }

    /**
     * Formaliza la transacción con descuento automático de existencias en ObjectDB.
     */
    private void formalizarVenta() {
        if (libroActual == null) {
            JOptionPane.showMessageDialog(this, "No hay ningún libro seleccionado.", "Atención", JOptionPane.WARNING_MESSAGE);
            enfocarScanner();
            return;
        }

        int cantidad = ((Number) spCantidad.getValue()).intValue();

        try {
            Venta venta = ventaController.procesarVenta(libroActual.getIdLibro(), cantidad);

            // Mensaje de éxito / Recibo
            String recibo = String.format(
                    "¡Venta registrada con éxito!\n\n" +
                            "Folio: #%d\n" +
                            "Fecha: %s\n" +
                            "Título: %s\n" +
                            "Editorial: %s\n" +
                            "Cantidad: %d ejemplar(es)\n" +
                            "Total Cobrado: %s\n\n" +
                            "Nuevo stock en tienda: %d unidad(es).",
                    venta.getIdVenta(),
                    FormatUtils.formatoFechaHora(venta.getFechaHora()),
                    libroActual.getTitulo(),
                    libroActual.getEditorial().getNombre(),
                    cantidad,
                    FormatUtils.formatoMoneda(venta.getTotal()),
                    libroActual.getStock() - cantidad
            );

            JOptionPane.showMessageDialog(this, recibo, "Ticket de Venta - Librería Atenea", JOptionPane.INFORMATION_MESSAGE);

            // Refrescar vistas
            limpiarFormulario();
            cargarVentasRecientes();
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la venta: " + ex.getMessage(), "Fallo en Transacción", JOptionPane.ERROR_MESSAGE);
            enfocarScanner();
        }
    }

    public void limpiarFormulario() {
        libroActual = null;
        txtIsbn.setText("");
        lblTitulo.setText("-");
        lblAutor.setText("-");
        lblEditorial.setText("-");
        lblPrecioUnitario.setText("$0.00");
        lblStockDisponible.setText("-");
        lblStockDisponible.setForeground(Color.BLACK);
        pnlAlertaBloqueo.setVisible(false);

        spCantidad.setValue(1);
        spCantidad.setEnabled(false);
        lblTotalCalculado.setText(FormatUtils.formatoMoneda(0.0));

        btnProcesarVenta.setEnabled(false);
        btnProcesarVenta.setBackground(new Color(180, 180, 180));
        btnProcesarVenta.setText("COBRAR Y DESPACHAR VENTA");

        enfocarScanner();
    }

    public void cargarVentasRecientes() {
        modeloTablaVentas.setRowCount(0);
        List<Venta> ventas = ventaController.obtenerTodasLasVentas();
        double totalRecaudado = 0.0;

        for (Venta v : ventas) {
            totalRecaudado += v.getTotal();
            String titulo = v.getLibro() != null ? v.getLibro().getTitulo() : "N/A";
            modeloTablaVentas.addRow(new Object[]{
                    v.getIdVenta(),
                    FormatUtils.formatoFechaHora(v.getFechaHora()),
                    titulo,
                    v.getCantidad(),
                    FormatUtils.formatoMoneda(v.getTotal())
            });
        }

        lblTotalArqueo.setText("Total Recaudado en Caja: " + FormatUtils.formatoMoneda(totalRecaudado));
    }

    /**
     * Mantiene el foco en el campo del ISBN para pistoleo ágil sin ratón (Ajuste Funcional #1).
     */
    public void enfocarScanner() {
        SwingUtilities.invokeLater(() -> {
            txtIsbn.requestFocusInWindow();
            txtIsbn.selectAll();
        });
    }
}

