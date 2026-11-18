package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.util.FormatUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Gestión de Editoriales y Navegación del Catálogo Anidado.
 * Permite visualizar el grafo de objetos Editorial -> List<Libro> directamente
 * sin necesidad de uniones relacionales complejas (JOIN).
 */
public class EditorialesPanel extends JPanel {

    private final EditorialController editorialController;
    private final Runnable onDataChangedCallback;

    private JTable tblEditoriales;
    private DefaultTableModel modeloEditoriales;

    private JTable tblLibrosAnidados;
    private DefaultTableModel modeloLibrosAnidados;
    private JLabel lblTituloAnidado;

    public EditorialesPanel(EditorialController editorialController, Runnable onDataChanged) {
        this.editorialController = editorialController;
        this.onDataChangedCallback = onDataChanged;

        initUI();
        cargarEditoriales();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // División vertical: Arriba Editoriales, Abajo Catálogo Anidado
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelEditoriales(), crearPanelCatálogoAnidado());
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(260);

        add(splitPane, BorderLayout.CENTER);
        add(crearBarraAcciones(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelEditoriales() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBorder(BorderFactory.createTitledBorder(" Casas Editoriales Asociadas "));

        String[] cols = {"ID", "Razón Social / Nombre", "País de Origen", "Correo Comercial", "Títulos en Catálogo"};
        modeloEditoriales = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblEditoriales = new JTable(modeloEditoriales);
        tblEditoriales.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblEditoriales.setRowHeight(25);
        tblEditoriales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblEditoriales.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblEditoriales.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblEditoriales.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblEditoriales.getColumnModel().getColumn(3).setPreferredWidth(220);
        tblEditoriales.getColumnModel().getColumn(4).setPreferredWidth(120);

        tblEditoriales.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarCatalogoAnidado();
            }
        });

        pnl.add(new JScrollPane(tblEditoriales), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel crearPanelCatálogoAnidado() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBorder(BorderFactory.createTitledBorder(" Catálogo de Libros Publicados "));

        lblTituloAnidado = new JLabel("Seleccione una editorial arriba para inspeccionar su colección de libros.");
        lblTituloAnidado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTituloAnidado.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        pnl.add(lblTituloAnidado, BorderLayout.NORTH);

        String[] cols = {"ID Libro", "ISBN", "Título de la Obra", "Autor", "Precio", "Stock Actual"};
        modeloLibrosAnidados = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblLibrosAnidados = new JTable(modeloLibrosAnidados);
        tblLibrosAnidados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLibrosAnidados.setRowHeight(24);

        tblLibrosAnidados.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblLibrosAnidados.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblLibrosAnidados.getColumnModel().getColumn(2).setPreferredWidth(250);
        tblLibrosAnidados.getColumnModel().getColumn(3).setPreferredWidth(180);
        tblLibrosAnidados.getColumnModel().getColumn(4).setPreferredWidth(90);
        tblLibrosAnidados.getColumnModel().getColumn(5).setPreferredWidth(80);

        pnl.add(new JScrollPane(tblLibrosAnidados), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel crearBarraAcciones() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnNueva = new JButton("+ Nueva Editorial");
        btnNueva.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNueva.setBackground(new Color(40, 167, 69));
        btnNueva.setForeground(Color.WHITE);
        btnNueva.addActionListener(e -> abrirDialogoNueva());

        JButton btnEditar = new JButton("Editar Editorial");
        btnEditar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEditar.addActionListener(e -> abrirDialogoEditar());

        JButton btnEliminar = new JButton("Eliminar Editorial");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.setForeground(new Color(180, 40, 40));
        btnEliminar.addActionListener(e -> eliminarSeleccionada());

        JButton btnRecargar = new JButton("Recargar");
        btnRecargar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRecargar.addActionListener(e -> cargarEditoriales());

        pnl.add(btnNueva);
        pnl.add(btnEditar);
        pnl.add(btnEliminar);
        pnl.add(btnRecargar);

        return pnl;
    }

    public void cargarEditoriales() {
        modeloEditoriales.setRowCount(0);
        modeloLibrosAnidados.setRowCount(0);
        lblTituloAnidado.setText("Seleccione una editorial arriba para inspeccionar su colección de libros.");

        List<Editorial> lista = editorialController.obtenerTodas();
        for (Editorial ed : lista) {
            int numLibros = ed.getLibros() != null ? ed.getLibros().size() : 0;
            modeloEditoriales.addRow(new Object[]{
                    ed.getIdEditorial(),
                    ed.getNombre(),
                    ed.getPais(),
                    ed.getCorreo(),
                    numLibros + " título(s)"
            });
        }
    }

    private void mostrarCatalogoAnidado() {
        int fila = tblEditoriales.getSelectedRow();
        if (fila < 0) {
            modeloLibrosAnidados.setRowCount(0);
            lblTituloAnidado.setText("Seleccione una editorial arriba para inspeccionar su colección de libros.");
            return;
        }

        long idEditorial = (long) modeloEditoriales.getValueAt(fila, 0);
        Editorial editorial = editorialController.obtenerPorId(idEditorial);

        if (editorial != null) {
            List<Libro> libros = editorial.getLibros();
            lblTituloAnidado.setText("<html>Catálogo de <b>" + editorial.getNombre() + "</b> (" +
                    (libros != null ? libros.size() : 0) + " libros registrados) - Contacto: " + editorial.getCorreo() + "</html>");

            modeloLibrosAnidados.setRowCount(0);
            if (libros != null) {
                for (Libro l : libros) {
                    modeloLibrosAnidados.addRow(new Object[]{
                            l.getIdLibro(),
                            l.getIsbn(),
                            l.getTitulo(),
                            l.getAutor(),
                            FormatUtils.formatoMoneda(l.getPrecio()),
                            l.getStock()
                    });
                }
            }
        }
    }

    private void abrirDialogoNueva() {
        Frame f = (Frame) SwingUtilities.getWindowAncestor(this);
        EditorialDialog dlg = new EditorialDialog(f, editorialController, null, () -> {
            cargarEditoriales();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        dlg.setVisible(true);
    }

    private void abrirDialogoEditar() {
        int fila = tblEditoriales.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una editorial para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idEditorial = (long) modeloEditoriales.getValueAt(fila, 0);
        Editorial ed = editorialController.obtenerPorId(idEditorial);
        if (ed != null) {
            Frame f = (Frame) SwingUtilities.getWindowAncestor(this);
            EditorialDialog dlg = new EditorialDialog(f, editorialController, ed, () -> {
                cargarEditoriales();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dlg.setVisible(true);
        }
    }

    private void eliminarSeleccionada() {
        int fila = tblEditoriales.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una editorial para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idEditorial = (long) modeloEditoriales.getValueAt(fila, 0);
        String nombre = (String) modeloEditoriales.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar la editorial '" + nombre + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                editorialController.eliminarEditorial(idEditorial);
                JOptionPane.showMessageDialog(this, "Editorial eliminada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarEditoriales();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar: " + ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

