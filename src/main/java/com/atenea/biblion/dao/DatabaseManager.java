package com.atenea.biblion.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Gestor centralizado del EntityManagerFactory y ciclo de vida de la conexión
 * a la base de datos persistente.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private EntityManagerFactory emf;
    private final String dbPath;

    private DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
        inicializar();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            String biblionDocsDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Biblion";
            File dir = new File(biblionDocsDir, "db");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            instance = new DatabaseManager(new File(dir, "biblion.odb").getAbsolutePath());
        }
        return instance;
    }

    public static synchronized DatabaseManager getInstance(String customDbPath) {
        if (instance == null) {
            instance = new DatabaseManager(customDbPath);
        }
        return instance;
    }

    private void inicializar() {
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        this.emf = Persistence.createEntityManagerFactory(dbPath);

        // Hook para cierre ordenado de la base de datos
        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));
    }

    public EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            inicializar();
        }
        return emf.createEntityManager();
    }

    /**
     * Ejecuta una acción dentro de una transacción JPA asegurando commit/rollback y cierre.
     */
    public void executeInTransaction(Consumer<EntityManager> action) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error en transacción: " + ex.getMessage(), ex);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Ejecuta una consulta o acción transaccional retornando un resultado.
     */
    public <T> T executeInTransactionResult(Function<EntityManager, T> action) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error en transacción con resultado: " + ex.getMessage(), ex);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Genera un respaldo formal del archivo persistente en la carpeta de Documentos.
     */
    public synchronized File crearRespaldo(String carpetaDestino) throws IOException {
        File dir;
        if (carpetaDestino == null || carpetaDestino.trim().isEmpty() || carpetaDestino.equals("backups")) {
            dir = new File(getCarpetaRespaldosPorDefecto());
        } else {
            dir = new File(carpetaDestino);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File origen = new File(dbPath);
        File destino = new File(dir, "biblion_backup_" + timestamp + ".odb");

        if (origen.exists()) {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destino;
        } else {
            throw new IOException("El archivo de base de datos origen no existe: " + origen.getAbsolutePath());
        }
    }

    public static String getCarpetaRespaldosPorDefecto() {
        return System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Biblion" + File.separator + "backups";
    }

    public String getDbPath() {
        return dbPath;
    }

    public synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
