package com.atenea.biblion.dao;

import com.atenea.biblion.model.Editorial;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Data Access Object para la entidad Editorial.
 * Realiza operaciones CRUD utilizando JPA y ObjectDB.
 */
public class EditorialDAO {

    private final DatabaseManager dbManager;

    public EditorialDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void guardar(Editorial editorial) {
        dbManager.executeInTransaction(em -> {
            em.persist(editorial);
        });
    }

    public void actualizar(Editorial editorial) {
        dbManager.executeInTransaction(em -> {
            em.merge(editorial);
        });
    }

    public void eliminar(long idEditorial) {
        dbManager.executeInTransaction(em -> {
            Editorial e = em.find(Editorial.class, idEditorial);
            if (e != null) {
                em.remove(e);
            }
        });
    }

    public Editorial buscarPorId(long idEditorial) {
        EntityManager em = dbManager.getEntityManager();
        try {
            return em.find(Editorial.class, idEditorial);
        } finally {
            em.close();
        }
    }

    public Editorial buscarPorNombre(String nombre) {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Editorial> q = em.createQuery(
                    "SELECT e FROM Editorial e WHERE LOWER(e.nombre) = LOWER(:nombre)", Editorial.class);
            q.setParameter("nombre", nombre);
            List<Editorial> resultados = q.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            em.close();
        }
    }

    public List<Editorial> listarTodas() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Editorial> q = em.createQuery("SELECT e FROM Editorial e ORDER BY e.nombre ASC", Editorial.class);
            List<Editorial> lista = q.getResultList();
            // Forzar carga de referencias para navegación en memoria
            for (Editorial e : lista) {
                if (e.getLibros() != null) {
                    e.getLibros().size();
                }
            }
            return lista;
        } finally {
            em.close();
        }
    }

    public long contar() {
        EntityManager em = dbManager.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(e) FROM Editorial e", Long.class);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }
}

