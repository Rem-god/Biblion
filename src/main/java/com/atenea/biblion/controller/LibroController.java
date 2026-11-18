package com.atenea.biblion.controller;

import com.atenea.biblion.dao.EditorialDAO;
import com.atenea.biblion.dao.LibroDAO;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;

import java.util.List;

/**
 * Controlador para la gestión y reglas de negocio del catálogo de Libros.
 */
public class LibroController {

    private final LibroDAO libroDAO;
    private final EditorialDAO editorialDAO;

    public LibroController() {
        this.libroDAO = new LibroDAO();
        this.editorialDAO = new EditorialDAO();
    }

    public LibroController(LibroDAO libroDAO, EditorialDAO editorialDAO) {
        this.libroDAO = libroDAO;
        this.editorialDAO = editorialDAO;
    }

    public void registrarLibro(String isbn, String titulo, String autor, double precio, int stock, long idEditorial) {
        validarCampos(isbn, titulo, autor, precio, stock);

        Libro existente = libroDAO.buscarPorIsbn(isbn);
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un libro registrado con el ISBN: " + isbn);
        }

        Editorial editorial = editorialDAO.buscarPorId(idEditorial);
        if (editorial == null) {
            throw new IllegalArgumentException("La editorial seleccionada no existe.");
        }

        Libro libro = new Libro(isbn.trim(), titulo.trim(), autor.trim(), precio, stock, editorial);
        libroDAO.guardar(libro);
    }

    public void actualizarLibro(long idLibro, String isbn, String titulo, String autor, double precio, int stock, long idEditorial) {
        validarCampos(isbn, titulo, autor, precio, stock);

        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null) {
            throw new IllegalArgumentException("No se encontró el libro con ID: " + idLibro);
        }

        Libro existenteConIsbn = libroDAO.buscarPorIsbn(isbn);
        if (existenteConIsbn != null && existenteConIsbn.getIdLibro() != idLibro) {
            throw new IllegalArgumentException("El ISBN " + isbn + " ya está asignado a otro título.");
        }

        Editorial editorial = editorialDAO.buscarPorId(idEditorial);
        if (editorial == null) {
            throw new IllegalArgumentException("La editorial seleccionada no existe.");
        }

        libro.setIsbn(isbn.trim());
        libro.setTitulo(titulo.trim());
        libro.setAutor(autor.trim());
        libro.setPrecio(precio);
        libro.setStock(stock);
        libro.setEditorial(editorial);

        libroDAO.actualizar(libro);
    }

    public void eliminarLibro(long idLibro) {
        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null) {
            throw new IllegalArgumentException("No se encontró el libro a dar de baja.");
        }
        if (libro.getVentas() != null && !libro.getVentas().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el libro '" + libro.getTitulo() +
                    "' porque tiene " + libro.getVentas().size() + " transacciones de venta asociadas en el historial.");
        }
        libroDAO.eliminar(idLibro);
    }

    public List<Libro> obtenerTodos() {
        return libroDAO.listarTodos();
    }

    public List<Libro> buscarConFiltros(String termino, Long idEditorial) {
        return libroDAO.buscarPorFiltros(termino, idEditorial);
    }

    public Libro buscarPorId(long idLibro) {
        return libroDAO.buscarPorId(idLibro);
    }

    public Libro buscarPorIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        return libroDAO.buscarPorIsbn(isbn.trim());
    }

    /**
     * Ajuste Funcional #3: Modificación porcentual de precios en bloque por editorial.
     */
    public int actualizarPreciosPorcentaje(long idEditorial, double porcentaje) {
        if (porcentaje < -90.0 || porcentaje > 500.0) {
            throw new IllegalArgumentException("El porcentaje de ajuste debe encontrarse entre -90% y +500%.");
        }
        return libroDAO.actualizarPreciosPorcentajePorEditorial(idEditorial, porcentaje);
    }

    private void validarCampos(String isbn, String titulo, String autor, double precio, int stock) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("El código ISBN es obligatorio.");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la obra es obligatorio.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del autor es obligatorio.");
        }
        if (precio < 0.0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("La cantidad en stock no puede ser negativa.");
        }
    }

    public long totalLibros() {
        return libroDAO.contar();
    }
}

