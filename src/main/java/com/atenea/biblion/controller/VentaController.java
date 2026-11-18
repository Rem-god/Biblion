package com.atenea.biblion.controller;

import com.atenea.biblion.dao.LibroDAO;
import com.atenea.biblion.dao.VentaDAO;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.model.Venta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de transacciones comerciales y punto de venta.
 * Implementa la validación de inventario y la regla de bloqueo de stock en cero.
 */
public class VentaController {

    private final VentaDAO ventaDAO;
    private final LibroDAO libroDAO;

    public VentaController() {
        this.ventaDAO = new VentaDAO();
        this.libroDAO = new LibroDAO();
    }

    public VentaController(VentaDAO ventaDAO, LibroDAO libroDAO) {
        this.ventaDAO = ventaDAO;
        this.libroDAO = libroDAO;
    }

    /**
     * Búsqueda rápida por ISBN para pistoleo por escáner de código de barras.
     * Retorna el libro y valida su disponibilidad.
     */
    public Libro buscarPorIsbnParaVenta(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese o escanee un código ISBN.");
        }
        Libro libro = libroDAO.buscarPorIsbn(isbn.trim());
        if (libro == null) {
            throw new IllegalArgumentException("No se encontró ningún ejemplar registrado con el ISBN: " + isbn);
        }
        return libro;
    }

    /**
     * Valida de manera preventiva las condiciones de stock antes de procesar la venta.
     * Atiende el Ajuste Funcional #2: Bloqueo de venta con stock en cero.
     */
    public void validarDisponibilidad(Libro libro, int cantidad) {
        if (libro == null) {
            throw new IllegalArgumentException("Debe seleccionar un libro para la venta.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (libro.getStock() <= 0) {
            throw new IllegalStateException("BLOQUEO INMEDIATO: El libro '" + libro.getTitulo() +
                    "' se encuentra AGOTADO (stock actual: 0). No es posible realizar la venta.");
        }
        if (libro.getStock() < cantidad) {
            throw new IllegalStateException("BLOQUEO POR STOCK INSUFICIENTE: Sólo quedan " +
                    libro.getStock() + " unidad(es) de '" + libro.getTitulo() + "'. Se solicitaron " + cantidad + ".");
        }
    }

    /**
     * Procesa la venta de forma transaccional, actualizando el stock y generando la factura.
     */
    public Venta procesarVenta(long idLibro, int cantidad) {
        Libro libro = libroDAO.buscarPorId(idLibro);
        validarDisponibilidad(libro, cantidad);
        return ventaDAO.registrarVentaTransaccional(idLibro, cantidad);
    }

    public List<Venta> obtenerTodasLasVentas() {
        return ventaDAO.listarTodas();
    }

    public List<Venta> obtenerVentasPorLibro(long idLibro) {
        return ventaDAO.listarPorLibro(idLibro);
    }

    public List<Venta> obtenerVentasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaDAO.listarPorRangoFechas(inicio, fin);
    }

    public double obtenerTotalArqueoCaja() {
        return ventaDAO.calcularTotalAcumulado();
    }

    public long totalTransacciones() {
        return ventaDAO.contar();
    }
}

