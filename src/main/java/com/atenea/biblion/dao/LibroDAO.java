package com.atenea.biblion.dao;

import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Libro.
 * Implementa operaciones de catálogo, filtros avanzados, consultas de inventario
 * crítico y la actualización porcentual de precios por editorial.
 */
public class LibroDAO {

    private final DatabaseManager dbManager;

    public LibroDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void guardar(Libro libro) {
        dbManager.executeInTransaction(em -> {
            Editorial edManaged = em.find(Editorial.class, libro.getEditorial().getIdEditorial());
            if (edManaged != null) {
                libro.setEditorial(edManaged);
                edManaged.addLibro(libro);
            }
            em.persist(libro);
        });
    }

    public void actualizar(Libro libro) {
        dbManager.executeInTransaction(em -> {
            Editorial edManaged = em.find(Editorial.class, libro.getEditorial().getIdEditorial());
            if (edManaged != null) {
                libro.setEditorial(edManaged);
            }
            em.merge(libro);
        });
    }

    public void eliminar(long idLibro) {
        dbManager.executeInTransaction(em -> {
            Libro l = em.find(Libro.class, idLibro);
            if (l != null) {
                Editorial ed = l.getEditorial();
                if (ed != null) {
                    ed.getLibros().remove(l);
                }
                em.remove(l);
            }
        });
    }

    public Libro buscarPorId(long idLibro) {
        EntityManager em = dbManager.getEntityManager();
        try {
            Libro libro = em.find(Libro.class, idLibro);
            if (libro != null && libro.getEditorial() != null) {
                libro.getEditorial().getNombre();
            }
            return libro;
        } finally {
            em.close();
        }
    }

    public Libro buscarPorIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Libro> q = em.createQuery(
                    "SELECT l FROM Libro l WHERE LOWER(TRIM(l.isbn)) = LOWER(:isbn)", Libro.class);
            q.setParameter("isbn", isbn.trim());
            List<Libro> resultados = q.getResultList();
            if (!resultados.isEmpty()) {
                Libro libro = resultados.get(0);
                if (libro.getEditorial() != null) {
                    libro.getEditorial().getNombre();
                }
                return libro;
            }
            return null;
        } finally {
            em.close();
        }
    }

    public List<Libro> listarTodos() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Libro> q = em.createQuery("SELECT l FROM Libro l ORDER BY l.titulo ASC", Libro.class);
            List<Libro> lista = q.getResultList();
            for (Libro l : lista) {
                if (l.getEditorial() != null) {
                    l.getEditorial().getNombre();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    /**
     * Búsqueda flexible filtrando por texto (título, autor o ISBN) y/o por editorial.
     */
    public List<Libro> buscarPorFiltros(String textoBusqueda, Long idEditorial) {
        EntityManager em = dbManager.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT l FROM Libro l WHERE 1=1");

            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                jpql.append(" AND (LOWER(l.titulo) LIKE :txt OR LOWER(l.autor) LIKE :txt OR LOWER(l.isbn) LIKE :txt)");
            }

            if (idEditorial != null && idEditorial > 0) {
                jpql.append(" AND l.editorial.idEditorial = :idEd");
            }

            jpql.append(" ORDER BY l.titulo ASC");

            TypedQuery<Libro> q = em.createQuery(jpql.toString(), Libro.class);

            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                q.setParameter("txt", "%" + textoBusqueda.trim().toLowerCase() + "%");
            }
            if (idEditorial != null && idEditorial > 0) {
                q.setParameter("idEd", idEditorial);
            }

            List<Libro> lista = q.getResultList();
            for (Libro l : lista) {
                if (l.getEditorial() != null) {
                    l.getEditorial().getNombre();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    public List<Libro> listarPorEditorial(long idEditorial) {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Libro> q = em.createQuery(
                    "SELECT l FROM Libro l WHERE l.editorial.idEditorial = :idEd ORDER BY l.titulo ASC", Libro.class);
            q.setParameter("idEd", idEditorial);
            List<Libro> lista = q.getResultList();
            for (Libro l : lista) {
                if (l.getEditorial() != null) {
                    l.getEditorial().getNombre();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    /**
     * Genera listados de títulos con inventario crítico (stock <= umbral)
     * para solicitar reposición a la editorial (Requisito 3 de Consultas y Reportes).
     */
    public List<Libro> listarInventarioCritico(int umbralMinimo) {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Libro> q = em.createQuery(
                    "SELECT l FROM Libro l WHERE l.stock <= :umbral ORDER BY l.stock ASC, l.titulo ASC", Libro.class);
            q.setParameter("umbral", umbralMinimo);
            List<Libro> lista = q.getResultList();
            for (Libro l : lista) {
                if (l.getEditorial() != null) {
                    l.getEditorial().getNombre();
                    l.getEditorial().getCorreo();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    /**
     * Ajuste Funcional #3 (Documento Diagnóstico Estratégico):
     * Actualiza en bloque los precios de todos los libros vinculados a una Editorial
     * en un porcentaje determinado (+10%, -5%, etc.), evitando ajustes manuales título por título.
     *
     * @param idEditorial Identificador de la editorial
     * @param porcentaje Porcentaje de ajuste (ejemplo: 15.0 para +15%, -10.0 para -10%)
     * @return Número de libros actualizados
     */
    public int actualizarPreciosPorcentajePorEditorial(long idEditorial, double porcentaje) {
        return dbManager.executeInTransactionResult(em -> {
            Editorial ed = em.find(Editorial.class, idEditorial);
            if (ed == null) {
                throw new IllegalArgumentException("No se encontró la editorial con ID: " + idEditorial);
            }

            TypedQuery<Libro> q = em.createQuery(
                    "SELECT l FROM Libro l WHERE l.editorial.idEditorial = :idEd", Libro.class);
            q.setParameter("idEd", idEditorial);
            List<Libro> libros = q.getResultList();

            double factor = 1.0 + (porcentaje / 100.0);
            for (Libro l : libros) {
                double nuevoPrecio = Math.round((l.getPrecio() * factor) * 100.0) / 100.0;
                if (nuevoPrecio < 0.0) {
                    nuevoPrecio = 0.0;
                }
                l.setPrecio(nuevoPrecio);
            }
            return libros.size();
        });
    }

    public long contar() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(l) FROM Libro l", Long.class);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }
}

