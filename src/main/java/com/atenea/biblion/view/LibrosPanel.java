package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.util.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Gestión del Catálogo de Libros (CRUD).
 * Permite filtrar por título, autor, ISBN o editorial, y actualizar precios en bloque.
 */
public class LibrosPanel extends JPanel {

    private final LibroController libroController;
    private final EditorialController editorialController;
    private final Runnable onDataChangedCallback;

    private JTextField txtBuscar;
    private JComboBox<EditorialItem> cbFiltroEditorial;
    private JTable tblLibros;
    private DefaultTableModel modeloTabla;
    private JLabel lblContador;

    public LibrosPanel(LibroController libroController, EditorialController editorialController, Runnable onDataChanged) {
        this.libroController = libroController;
        this.editorialController = editorialController;
        this.onDataChangedCallback = onDataChanged;

        initUI();
        cargarEditorialesEnFiltro();
        cargarLibros();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Barra Superior de Filtros y Búsqueda
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlFiltros.setBorder(BorderFactory.createTitledBorder(" Filtros de Búsqueda de Catálogo "));

        pnlFiltros.add(new JLabel("Buscar (Título / Autor / ISBN):"));
        txtBuscar = new JTextField(18);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.addActionListener(e -> filtrarLibros());
        pnlFiltros.add(txtBuscar);

        pnlFiltros.add(new JLabel("Editorial:"));
        cbFiltroEditorial = new JComboBox<>();
        cbFiltroEditorial.setPreferredSize(new Dimension(200, 28));
        cbFiltroEditorial.addActionListener(e -> filtrarLibros());
        pnlFiltros.add(cbFiltroEditorial);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.setBackground(new Color(41, 128, 185));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> filtrarLibros());
        pnlFiltros.add(btnBuscar);

        JButton btnLimpiar = new JButton("Limpiar Filtros");
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            if (cbFiltroEditorial.getItemCount() > 0) {
                cbFiltroEditorial.setSelectedIndex(0);
            }
            cargarLibros();
        });
        pnlFiltros.add(btnLimpiar);

        add(pnlFiltros, BorderLayout.NORTH);

        // 2. Tabla Central de Catálogo
        String[] columnas = {"ID", "ISBN", "Título de la Obra", "Autor", "Editorial", "Precio", "Stock", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLibros = new JTable(modeloTabla);
        tblLibros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLibros.setRowHeight(26);
        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblLibros.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblLibros.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblLibros.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblLibros.getColumnModel().getColumn(3).setPreferredWidth(160);
        tblLibros.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblLibros.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblLibros.getColumnModel().getColumn(6).setPreferredWidth(60);
        tblLibros.getColumnModel().getColumn(7).setPreferredWidth(95);

        // Renderizador para colorear el estado del stock
        tblLibros.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    if ("AGOTADO".equals(estado)) {
                        c.setForeground(new Color(220, 53, 69));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if ("CRÍTICO".equals(estado)) {
                        c.setForeground(new Color(210, 120, 0));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(30, 130, 60));
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tblLibros);
        add(scroll, BorderLayout.CENTER);

        // 3. Barra Inferior de Acciones
        JPanel pnlAcciones = new JPanel(new BorderLayout());
        pnlAcciones.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));

        lblContador = new JLabel("Total de títulos: 0");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblContador.setForeground(new Color(100, 100, 100));
        pnlAcciones.add(lblContador, BorderLayout.WEST);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnNuevo = new JButton("+ Nuevo Libro");
        btnNuevo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNuevo.setBackground(new Color(40, 167, 69));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.addActionListener(e -> abrirDialogoNuevo());

        JButton btnEditar = new JButton("Editar Selección");
        btnEditar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEditar.addActionListener(e -> abrirDialogoEditar());

        JButton btnEliminar = new JButton("Dar de Baja");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.setForeground(new Color(180, 40, 40));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // Botón para Ajuste Funcional #3
        JButton btnAjustePrecios = new JButton("Actualizar Precios en Bloque (%)");
        btnAjustePrecios.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAjustePrecios.setBackground(new Color(33, 63, 115));
        btnAjustePrecios.setForeground(Color.WHITE);
        btnAjustePrecios.setToolTipText("Ajusta los precios de todos los libros de una editorial en un porcentaje determinado");
        btnAjustePrecios.addActionListener(e -> abrirDialogoAjustePrecios());

        JButton btnRecargar = new JButton("Recargar");
        btnRecargar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRecargar.addActionListener(e -> cargarLibros());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnAjustePrecios);
        pnlBotones.add(btnRecargar);

        pnlAcciones.add(pnlBotones, BorderLayout.EAST);
        add(pnlAcciones, BorderLayout.SOUTH);
    }

    public void cargarEditorialesEnFiltro() {
        cbFiltroEditorial.removeAllItems();
        cbFiltroEditorial.addItem(new EditorialItem(0L, "-- Todas las Editoriales --"));
        List<Editorial> editoriales = editorialController.obtenerTodas();
        for (Editorial e : editoriales) {
            cbFiltroEditorial.addItem(new EditorialItem(e.getIdEditorial(), e.getNombre()));
        }
    }

    public void cargarLibros() {
        List<Libro> libros = libroController.obtenerTodos();
        mostrarLibrosEnTabla(libros);
    }

    private void filtrarLibros() {
        String texto = txtBuscar.getText().trim();
        EditorialItem item = (EditorialItem) cbFiltroEditorial.getSelectedItem();
        Long idEditorial = (item != null && item.id > 0) ? item.id : null;

        List<Libro> libros = libroController.buscarConFiltros(texto, idEditorial);
        mostrarLibrosEnTabla(libros);
    }

    private void mostrarLibrosEnTabla(List<Libro> libros) {
        modeloTabla.setRowCount(0);
        for (Libro l : libros) {
            String editorial = (l.getEditorial() != null) ? l.getEditorial().getNombre() : "N/A";
            String estado;
            if (l.getStock() <= 0) {
                estado = "AGOTADO";
            } else if (l.getStock() <= 4) {
                estado = "CRÍTICO";
            } else {
                estado = "Disponible";
            }

            modeloTabla.addRow(new Object[]{
                    l.getIdLibro(),
                    l.getIsbn(),
                    l.getTitulo(),
                    l.getAutor(),
                    editorial,
                    FormatUtils.formatoMoneda(l.getPrecio()),
                    l.getStock(),
                    estado
            });
        }
        lblContador.setText("Total de títulos mostrados: " + libros.size());
    }

    private void abrirDialogoNuevo() {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        LibroDialog dlg = new LibroDialog(frame, libroController, editorialController, null, () -> {
            cargarLibros();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        dlg.setVisible(true);
    }

    private void abrirDialogoEditar() {
        int fila = tblLibros.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idLibro = (long) modeloTabla.getValueAt(fila, 0);
        Libro libro = libroController.buscarPorId(idLibro);
        if (libro != null) {
            Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
            LibroDialog dlg = new LibroDialog(frame, libroController, editorialController, libro, () -> {
                cargarLibros();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dlg.setVisible(true);
        }
    }

    private void eliminarSeleccionado() {
        int fila = tblLibros.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla para dar de baja.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idLibro = (long) modeloTabla.getValueAt(fila, 0);
        String titulo = (String) modeloTabla.getValueAt(fila, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el libro '" + titulo + "' del catálogo?",
                "Confirmar Baja de Ejemplar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                libroController.eliminarLibro(idLibro);
                JOptionPane.showMessageDialog(this, "El libro fue eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarLibros();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirDialogoAjustePrecios() {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        ActualizarPreciosDialog dlg = new ActualizarPreciosDialog(frame, editorialController, libroController, () -> {
            cargarLibros();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        dlg.setVisible(true);
    }

    /**
     * Elemento auxiliar para combo box con ID y Nombre.
     */
    private static class EditorialItem {
        final long id;
        final String nombre;

        EditorialItem(long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}

