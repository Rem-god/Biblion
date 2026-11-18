package com.atenea.biblion.controller;

import com.atenea.biblion.dao.DatabaseManager;
import com.atenea.biblion.dao.EditorialDAO;
import com.atenea.biblion.dao.LibroDAO;
import com.atenea.biblion.dao.VentaDAO;
import com.atenea.biblion.model.Libro;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controlador para la emisión de reportes gerenciales, inventario crítico
 * y respaldo de la base de datos orientada a objetos.
 */
public class ReporteController {

    private final LibroDAO libroDAO;
    private final EditorialDAO editorialDAO;
    private final VentaDAO ventaDAO;
    private final DatabaseManager dbManager;

    public ReporteController() {
        this.libroDAO = new LibroDAO();
        this.editorialDAO = new EditorialDAO();
        this.ventaDAO = new VentaDAO();
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Consulta y filtra el catálogo de títulos con inventario crítico (stock <= umbral).
     * Requisito de Consultas y Reportes para solicitar reposición a las editoriales.
     */
    public List<Libro> obtenerInventarioCritico(int umbralMinimo) {
        if (umbralMinimo < 0) {
            umbralMinimo = 5;
        }
        return libroDAO.listarInventarioCritico(umbralMinimo);
    }

    /**
     * Ejecuta una copia de respaldo del archivo .odb en el directorio de respaldos.
     * Mitiga el riesgo P6 de seguridad y pérdida de información.
     */
    public File ejecutarRespaldo(String directorioDestino) throws IOException {
        if (directorioDestino == null || directorioDestino.trim().isEmpty()) {
            directorioDestino = "backups";
        }
        return dbManager.crearRespaldo(directorioDestino);
    }

    public long getTotalTitulos() {
        return libroDAO.contar();
    }

    public long getTotalEditoriales() {
        return editorialDAO.contar();
    }

    public long getTotalVentasRealizadas() {
        return ventaDAO.contar();
    }

    public double getTotalRecaudado() {
        return ventaDAO.calcularTotalAcumulado();
    }
}

