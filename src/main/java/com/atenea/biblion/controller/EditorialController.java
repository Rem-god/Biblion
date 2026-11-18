package com.atenea.biblion.controller;

import com.atenea.biblion.dao.EditorialDAO;
import com.atenea.biblion.model.Editorial;

import java.util.List;

/**
 * Controlador para la gestión y reglas de negocio de las Editoriales asociadas.
 */
public class EditorialController {

    private final EditorialDAO editorialDAO;

    public EditorialController() {
        this.editorialDAO = new EditorialDAO();
    }

    public EditorialController(EditorialDAO dao) {
        this.editorialDAO = dao;
    }

    public void registrarEditorial(String nombre, String pais, String correo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre o razón social de la editorial es obligatorio.");
        }
        Editorial existente = editorialDAO.buscarPorNombre(nombre.trim());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una editorial registrada con el nombre: " + nombre);
        }
        Editorial editorial = new Editorial(nombre.trim(), pais != null ? pais.trim() : "", correo != null ? correo.trim() : "");
        editorialDAO.guardar(editorial);
    }

    public void actualizarEditorial(long idEditorial, String nombre, String pais, String correo) {
        Editorial editorial = editorialDAO.buscarPorId(idEditorial);
        if (editorial == null) {
            throw new IllegalArgumentException("No se encontró la editorial con ID: " + idEditorial);
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la editorial no puede quedar vacío.");
        }
        editorial.setNombre(nombre.trim());
        editorial.setPais(pais != null ? pais.trim() : "");
        editorial.setCorreo(correo != null ? correo.trim() : "");
        editorialDAO.actualizar(editorial);
    }

    public void eliminarEditorial(long idEditorial) {
        Editorial editorial = editorialDAO.buscarPorId(idEditorial);
        if (editorial == null) {
            throw new IllegalArgumentException("No se encontró la editorial a eliminar.");
        }
        if (editorial.getLibros() != null && !editorial.getLibros().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la editorial '" + editorial.getNombre() +
                    "' porque tiene " + editorial.getLibros().size() + " libros asociados en el catálogo activo.");
        }
        editorialDAO.eliminar(idEditorial);
    }

    public List<Editorial> obtenerTodas() {
        return editorialDAO.listarTodas();
    }

    public Editorial obtenerPorId(long idEditorial) {
        return editorialDAO.buscarPorId(idEditorial);
    }

    public long totalEditoriales() {
        return editorialDAO.contar();
    }
}

