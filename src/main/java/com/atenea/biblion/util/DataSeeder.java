package com.atenea.biblion.util;

import com.atenea.biblion.dao.DatabaseManager;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.model.Venta;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

/**
 * Generador y sembrador de datos iniciales depurados y verídicos.
 * Garantiza la consistencia inicial del catálogo de la Librería Cultural Atenea,
 * abordando la estrategia definida para el Problema P1 (Discrepancia sustancial de inventario).
 */
public class DataSeeder {

    public static void sembrarDatosSiEsNecesario() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        EntityManager checkEm = dbManager.getEntityManager();
        try {
            Long countEditoriales = checkEm.createQuery("SELECT COUNT(e) FROM com.atenea.biblion.model.Editorial e", Long.class).getSingleResult();
            if (countEditoriales != null && countEditoriales > 0) {
                // Ya cuenta con datos
                return;
            }
        } catch (Exception ex) {
            // En ObjectDB nuevo, el tipo Editorial se registra tras persistir la primera instancia
        } finally {
            checkEm.close();
        }

        dbManager.executeInTransaction(txEm -> {
            // 1. Editoriales
            Editorial planeta = new Editorial("Grupo Planeta", "España / México", "pedidos@planetadelibros.com.mx");
            Editorial prh = new Editorial("Penguin Random House", "España / México", "comercial@penguinrandomhouse.com");
            Editorial fce = new Editorial("Fondo de Cultura Económica", "México", "ventas@fondodeculturaeconomica.com");
            Editorial alianza = new Editorial("Alianza Editorial", "España", "atencion@alianzaeditorial.es");
            Editorial anagrama = new Editorial("Editorial Anagrama", "España", "distribucion@anagrama-ed.es");

            txEm.persist(planeta);
            txEm.persist(prh);
            txEm.persist(fce);
            txEm.persist(alianza);
            txEm.persist(anagrama);

            // 2. Libros
            Libro l1 = new Libro("978-607-07-2879-2", "Cien años de soledad", "Gabriel García Márquez", 389.00, 15, prh);
            Libro l2 = new Libro("978-968-16-0301-4", "El laberinto de la soledad", "Octavio Paz", 220.00, 4, fce); // Crítico
            Libro l3 = new Libro("978-84-206-8562-6", "Don Quijote de la Mancha", "Miguel de Cervantes", 450.00, 8, alianza);
            Libro l4 = new Libro("978-607-31-1522-3", "Rayuela", "Julio Cortázar", 340.00, 2, prh); // Crítico
            Libro l5 = new Libro("978-968-16-0302-1", "Pedro Páramo", "Juan Rulfo", 185.00, 25, fce);
            Libro l6 = new Libro("978-607-07-3622-3", "La sombra del viento", "Carlos Ruiz Zafón", 395.00, 0, planeta); // Agotado (Stock 0)
            Libro l7 = new Libro("978-607-31-4197-0", "1984", "George Orwell", 199.00, 14, prh);
            Libro l8 = new Libro("978-607-31-1188-1", "Ensayo sobre la ceguera", "José Saramago", 310.00, 3, prh); // Crítico
            Libro l9 = new Libro("978-84-206-5120-1", "Crimen y castigo", "Fiódor Dostoyevski", 280.00, 7, alianza);
            Libro l10 = new Libro("978-84-339-7313-9", "Tokio Blues (Norwegian Wood)", "Haruki Murakami", 360.00, 10, anagrama);
            Libro l11 = new Libro("978-607-07-4487-7", "Sapiens: De animales a dioses", "Yuval Noah Harari", 420.00, 12, planeta);

            txEm.persist(l1);
            txEm.persist(l2);
            txEm.persist(l3);
            txEm.persist(l4);
            txEm.persist(l5);
            txEm.persist(l6);
            txEm.persist(l7);
            txEm.persist(l8);
            txEm.persist(l9);
            txEm.persist(l10);
            txEm.persist(l11);

            planeta.addLibro(l6);
            planeta.addLibro(l11);

            prh.addLibro(l1);
            prh.addLibro(l4);
            prh.addLibro(l7);
            prh.addLibro(l8);

            fce.addLibro(l2);
            fce.addLibro(l5);

            alianza.addLibro(l3);
            alianza.addLibro(l9);

            anagrama.addLibro(l10);

            // 3. Ventas de demostración previas (jornada actual y anterior)
            LocalDateTime ahora = LocalDateTime.now();
            Venta v1 = new Venta(l1, 2, ahora.minusHours(4));
            Venta v2 = new Venta(l5, 3, ahora.minusHours(3));
            Venta v3 = new Venta(l7, 1, ahora.minusHours(1).minusMinutes(30));
            Venta v4 = new Venta(l10, 2, ahora.minusMinutes(20));

            l1.addVenta(v1);
            l5.addVenta(v2);
            l7.addVenta(v3);
            l10.addVenta(v4);

            txEm.persist(v1);
            txEm.persist(v2);
            txEm.persist(v3);
            txEm.persist(v4);
        });
    }
}
