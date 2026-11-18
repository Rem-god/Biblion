package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo modal para dar de alta o editar información de un libro.
 */
public class LibroDialog extends JDialog {

    private final LibroController libroController;
    private final EditorialController editorialController;
    private final Runnable onSuccessCallback;
    private final Libro libroAEditar;

    private JTextField txtIsbn;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JSpinner spPrecio;
    private JSpinner spStock;
    private JComboBox<Editorial> cbEditorial;

    public LibroDialog(Frame parent, LibroController libroController, EditorialController editorialController,
                       Libro libroAEditar, Runnable onSuccess) {
        super(parent, libroAEditar == null ? "Registrar Nuevo Libro en Catálogo" : "Editar Datos de Libro", true);
        this.libroController = libroController;
        this.editorialController = editorialController;
        this.libroAEditar = libroAEditar;
        this.onSuccessCallback = onSuccess;

        initUI();
        cargarEditoriales();
        if (libroAEditar != null) {
            cargarDatosExistentes();
        }
    }

    private void initUI() {
        setSize(500, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(33, 63, 115));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel(libroAEditar == null ? "Nuevo Título Comercial" : "Modificación de Título");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // ISBN
        g.gridx = 0; g.gridy = 0; g.weightx = 0.3;
        pnlForm.add(new JLabel("ISBN:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtIsbn = new JTextField();
        pnlForm.add(txtIsbn, g);

        // Título
        g.gridx = 0; g.gridy = 1; g.weightx = 0.3;
        pnlForm.add(new JLabel("Título de la Obra:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtTitulo = new JTextField();
        pnlForm.add(txtTitulo, g);

        // Autor
        g.gridx = 0; g.gridy = 2; g.weightx = 0.3;
        pnlForm.add(new JLabel("Autor Completo:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtAutor = new JTextField();
        pnlForm.add(txtAutor, g);

        // Editorial
        g.gridx = 0; g.gridy = 3; g.weightx = 0.3;
        pnlForm.add(new JLabel("Editorial Proveedora:"), g);
        g.gridx = 1; g.weightx = 0.7;
        cbEditorial = new JComboBox<>();
        pnlForm.add(cbEditorial, g);

        // Precio
        g.gridx = 0; g.gridy = 4; g.weightx = 0.3;
        pnlForm.add(new JLabel("Precio Unitario ($):"), g);
        g.gridx = 1; g.weightx = 0.7;
        spPrecio = new JSpinner(new SpinnerNumberModel(250.0, 0.0, 99999.0, 10.0));
        pnlForm.add(spPrecio, g);

        // Stock
        g.gridx = 0; g.gridy = 5; g.weightx = 0.3;
        pnlForm.add(new JLabel("Stock Inicial en Tienda:"), g);
        g.gridx = 1; g.weightx = 0.7;
        spStock = new JSpinner(new SpinnerNumberModel(10, 0, 10000, 1));
        pnlForm.add(spStock, g);

        add(pnlForm, BorderLayout.CENTER);

        // Botones
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = new JButton("Guardar en Catálogo");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setBackground(new Color(33, 63, 115));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());

        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnGuardar);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private void cargarEditoriales() {
        cbEditorial.removeAllItems();
        List<Editorial> editoriales = editorialController.obtenerTodas();
        for (Editorial e : editoriales) {
            cbEditorial.addItem(e);
        }
    }

    private void cargarDatosExistentes() {
        txtIsbn.setText(libroAEditar.getIsbn());
        txtTitulo.setText(libroAEditar.getTitulo());
        txtAutor.setText(libroAEditar.getAutor());
        spPrecio.setValue(libroAEditar.getPrecio());
        spStock.setValue(libroAEditar.getStock());

        if (libroAEditar.getEditorial() != null) {
            for (int i = 0; i < cbEditorial.getItemCount(); i++) {
                Editorial ed = cbEditorial.getItemAt(i);
                if (ed.getIdEditorial() == libroAEditar.getEditorial().getIdEditorial()) {
                    cbEditorial.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void guardar() {
        String isbn = txtIsbn.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        double precio = ((Number) spPrecio.getValue()).doubleValue();
        int stock = ((Number) spStock.getValue()).intValue();
        Editorial editorial = (Editorial) cbEditorial.getSelectedItem();

        if (editorial == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una editorial.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (libroAEditar == null) {
                libroController.registrarLibro(isbn, titulo, autor, precio, stock, editorial.getIdEditorial());
                JOptionPane.showMessageDialog(this, "Libro registrado exitosamente en la base de datos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                libroController.actualizarLibro(libroAEditar.getIdLibro(), isbn, titulo, autor, precio, stock, editorial.getIdEditorial());
                JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error de Validación", JOptionPane.ERROR_MESSAGE);
        }
    }
}

