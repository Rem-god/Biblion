package com.atenea.biblion.view;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.model.Editorial;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo para la actualización porcentual de precios por editorial.
 * Diseño minimalista y directo.
 */
public class ActualizarPreciosDialog extends JDialog {

    private final EditorialController editorialController;
    private final LibroController libroController;
    private final Runnable onSuccessCallback;

    private JComboBox<Editorial> cbEditorial;
    private JSpinner spPorcentaje;
    private JLabel lblPreview;

    public ActualizarPreciosDialog(Frame parent, EditorialController edCtrl, LibroController libCtrl, Runnable onSuccess) {
        super(parent, "Actualización de Precios", true);
        this.editorialController = edCtrl;
        this.libroController = libCtrl;
        this.onSuccessCallback = onSuccess;

        initUI();
        cargarEditoriales();
    }

    private void initUI() {
        setSize(500, 320);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(12, 12));

        // Header Minimalista
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel lblTitle = new JLabel("Actualización Porcentual de Precios");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Ajusta los precios de venta de todos los títulos de una editorial.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Editorial
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblEd = new JLabel("Editorial:");
        lblEd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblEd, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cbEditorial = new JComboBox<>();
        cbEditorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(cbEditorial, gbc);

        // Porcentaje
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPct = new JLabel("Porcentaje (%):");
        lblPct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lblPct, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        SpinnerNumberModel model = new SpinnerNumberModel(10.0, -90.0, 500.0, 1.0);
        spPorcentaje = new JSpinner(model);
        spPorcentaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(spPorcentaje, gbc);

        // Explicación / Preview
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        lblPreview = new JLabel("<html><i>Ejemplo: Ingrese <b>+10</b> para incremento por catálogo o <b>-5</b> para descuento general.</i></html>");
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreview.setForeground(new Color(100, 116, 139));
        formPanel.add(lblPreview, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnAplicar = new JButton("Aplicar Ajuste");
        btnAplicar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAplicar.addActionListener(e -> ejecutarAjuste());

        btnPanel.add(btnCancelar);
        btnPanel.add(btnAplicar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void cargarEditoriales() {
        cbEditorial.removeAllItems();
        List<Editorial> editoriales = editorialController.obtenerTodas();
        for (Editorial e : editoriales) {
            cbEditorial.addItem(e);
        }
    }

    private void ejecutarAjuste() {
        Editorial seleccionada = (Editorial) cbEditorial.getSelectedItem();
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una editorial.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double porcentaje = ((Number) spPorcentaje.getValue()).doubleValue();
        if (porcentaje == 0.0) {
            JOptionPane.showMessageDialog(this, "El porcentaje de ajuste no puede ser 0%.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String signo = porcentaje > 0 ? "+" : "";
        String confirmMsg = String.format(
                "¿Está seguro de aplicar un ajuste de %s%.1f%% a todos los libros de la editorial:\n\n'%s'?",
                signo, porcentaje, seleccionada.getNombre()
        );

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirmar Actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modificados = libroController.actualizarPreciosPorcentaje(seleccionada.getIdEditorial(), porcentaje);
                JOptionPane.showMessageDialog(this,
                        "Actualización completada exitosamente.\nSe modificó el precio de " + modificados + " título(s).",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar precios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
