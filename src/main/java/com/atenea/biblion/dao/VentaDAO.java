package com.atenea.biblion.dao;

import com.atenea.biblion.model.Libro;
import com.atenea.biblion.model.Venta;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object para la entidad Venta.
 * Ejecuta de forma transaccional el descuento automático de inventario,
 * las validaciones de stock y consultas para historial y arqueo de caja.
 */
public class VentaDAO {

    private final DatabaseManager dbManager;

    public VentaDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Procesa una venta descontando atómicamente las unidades vendidas del stock del libro.
     * Si no hay existencias suficientes o el stock es <= 0, aborta la transacción
     * lanzando una excepción clara.
     */
    public Venta registrarVentaTransaccional(long idLibro, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser al menos 1 unidad.");
        }

        return dbManager.executeInTransactionResult(em -> {
            Libro libro = em.find(Libro.class, idLibro);
            if (libro == null) {
                throw new IllegalStateException("El libro con ID " + idLibro + " no existe en el catálogo.");
            }

            // Regla de bloqueo estricto con stock <= 0
            if (libro.getStock() <= 0) {
                throw new IllegalStateException("BLOQUEO DE VENTA: El libro '" + libro.getTitulo() +
                        "' se encuentra agotado (stock: " + libro.getStock() + "). No se puede despachar.");
            }

            // Regla de existencias suficientes
            if (libro.getStock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente: Disponibles en tienda: " +
                        libro.getStock() + " unidad(es). Cantidad requerida: " + cantidad);
            }

            // Descuento automático de stock
            libro.descontarStock(cantidad);

            // Creación y persistencia de la Venta
            Venta venta = new Venta(libro, cantidad, LocalDateTime.now());
            libro.addVenta(venta);

            em.persist(venta);
            return venta;
        });
    }

    public List<Venta> listarTodas() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Venta> q = em.createQuery(
                    "SELECT v FROM Venta v ORDER BY v.fechaHora DESC", Venta.class);
            List<Venta> lista = q.getResultList();
            for (Venta v : lista) {
                if (v.getLibro() != null) {
                    v.getLibro().getTitulo();
                    if (v.getLibro().getEditorial() != null) {
                        v.getLibro().getEditorial().getNombre();
                    }
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    public List<Venta> listarPorLibro(long idLibro) {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Venta> q = em.createQuery(
                    "SELECT v FROM Venta v WHERE v.libro.idLibro = :idLibro ORDER BY v.fechaHora DESC", Venta.class);
            q.setParameter("idLibro", idLibro);
            List<Venta> lista = q.getResultList();
            for (Venta v : lista) {
                if (v.getLibro() != null) {
                    v.getLibro().getTitulo();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    public List<Venta> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Venta> q = em.createQuery(
                    "SELECT v FROM Venta v WHERE v.fechaHora >= :inicio AND v.fechaHora <= :fin ORDER BY v.fechaHora DESC",
                    Venta.class);
            q.setParameter("inicio", inicio);
            q.setParameter("fin", fin);
            List<Venta> lista = q.getResultList();
            for (Venta v : lista) {
                if (v.getLibro() != null) {
                    v.getLibro().getTitulo();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    public double calcularTotalAcumulado() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Double> q = em.createQuery("SELECT SUM(v.total) FROM Venta v", Double.class);
            Double total = q.getSingleResult();
            return total != null ? total : 0.0;
        } finally {
            em.close();
        }
    }

    public long contar() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(v) FROM Venta v", Long.class);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }
}

