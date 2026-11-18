package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.model.Editorial;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para dar de alta o modificar una Casa Editorial.
 */
public class EditorialDialog extends JDialog {

    private final EditorialController editorialController;
    private final Editorial editorialAEditar;
    private final Runnable onSuccessCallback;

    private JTextField txtNombre;
    private JTextField txtPais;
    private JTextField txtCorreo;

    public EditorialDialog(Frame parent, EditorialController editorialController, Editorial editorialAEditar, Runnable onSuccess) {
        super(parent, editorialAEditar == null ? "Nueva Editorial Asociada" : "Editar Datos de Editorial", true);
        this.editorialController = editorialController;
        this.editorialAEditar = editorialAEditar;
        this.onSuccessCallback = onSuccess;

        initUI();
        if (editorialAEditar != null) {
            cargarDatos();
        }
    }

    private void initUI() {
        setSize(460, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(248, 250, 252));
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JLabel lblTitle = new JLabel(editorialAEditar == null ? "Nueva Editorial" : "Editar Editorial");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0.3;
        pnlForm.add(new JLabel("Razón Social / Nombre:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtNombre = new JTextField();
        pnlForm.add(txtNombre, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.3;
        pnlForm.add(new JLabel("País de Origen:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtPais = new JTextField();
        pnlForm.add(txtPais, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0.3;
        pnlForm.add(new JLabel("Correo Electrónico:"), g);
        g.gridx = 1; g.weightx = 0.7;
        txtCorreo = new JTextField();
        pnlForm.add(txtCorreo, g);

        add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = new JButton("Guardar Editorial");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setBackground(new Color(33, 63, 115));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());

        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnGuardar);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        txtNombre.setText(editorialAEditar.getNombre());
        txtPais.setText(editorialAEditar.getPais());
        txtCorreo.setText(editorialAEditar.getCorreo());
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String pais = txtPais.getText().trim();
        String correo = txtCorreo.getText().trim();

        try {
            if (editorialAEditar == null) {
                editorialController.registrarEditorial(nombre, pais, correo);
                JOptionPane.showMessageDialog(this, "Editorial registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                editorialController.actualizarEditorial(editorialAEditar.getIdEditorial(), nombre, pais, correo);
                JOptionPane.showMessageDialog(this, "Editorial actualizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
        }
    }
}

