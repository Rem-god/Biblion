package com.atenea.biblion;

import com.atenea.biblion.controller.EditorialController;
import com.atenea.biblion.controller.LibroController;
import com.atenea.biblion.controller.VentaController;
import com.atenea.biblion.dao.DatabaseManager;
import com.atenea.biblion.model.Editorial;
import com.atenea.biblion.model.Libro;
import com.atenea.biblion.model.Venta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias y de integración para el sistema Biblion.
 * Valida el funcionamiento de ObjectDB, relaciones de objetos,
 * descuento de existencias, bloqueo con stock 0 y actualización porcentual en bloque.
 */
public class BiblionTest {

    private static final String TEST_DB = "target/test_biblion.odb";
    private static EditorialController editorialCtrl;
    private static LibroController libroCtrl;
    private static VentaController ventaCtrl;

    @BeforeClass
    public static void setUp() {
        new File(TEST_DB).delete();
        DatabaseManager.getInstance(TEST_DB);
        editorialCtrl = new EditorialController();
        libroCtrl = new LibroController();
        ventaCtrl = new VentaController();
    }

    @AfterClass
    public static void tearDown() {
        DatabaseManager.getInstance().cerrar();
    }

    @Test
    public void testCrudEditorialYNavegacionObjetos() {
        editorialCtrl.registrarEditorial("Editorial Siglo XXI", "México", "contacto@sigloxxieditores.com");
        List<Editorial> editoriales = editorialCtrl.obtenerTodas();
        assertFalse(editoriales.isEmpty());

        Editorial ed = editoriales.stream()
                .filter(e -> e.getNombre().equals("Editorial Siglo XXI"))
                .findFirst()
                .orElse(null);

        assertNotNull("La editorial debe ser persistida", ed);
        assertEquals("México", ed.getPais());

        // Registrar libros asociados a esta editorial
        libroCtrl.registrarLibro("978-607-03-0100-1", "Las venas abiertas de América Latina", "Eduardo Galeano", 299.0, 10, ed.getIdEditorial());

        // Comprobar navegación directa del grafo en memoria
        Editorial edConLibros = editorialCtrl.obtenerPorId(ed.getIdEditorial());
        assertNotNull(edConLibros);
        assertEquals("Debe navegar directamente a los libros sin sentencias SQL complejas",
                1, edConLibros.getLibros().size());
        assertEquals("Las venas abiertas de América Latina", edConLibros.getLibros().get(0).getTitulo());
    }

    @Test
    public void testVentaTransaccionalYDescuentoStock() {
        Editorial ed = editorialCtrl.obtenerTodas().get(0);
        String isbn = "978-111-222-333-4";
        libroCtrl.registrarLibro(isbn, "Libro de Prueba Transaccional", "Autor Test", 150.0, 5, ed.getIdEditorial());

        Libro libro = libroCtrl.buscarPorIsbn(isbn);
        assertEquals(5, libro.getStock());

        // Procesar venta de 2 ejemplares
        Venta v = ventaCtrl.procesarVenta(libro.getIdLibro(), 2);
        assertNotNull(v);
        assertEquals(300.0, v.getTotal(), 0.001);

        // Verificar que el stock se descontó en la base de datos
        Libro libroActualizado = libroCtrl.buscarPorId(libro.getIdLibro());
        assertEquals("El stock debe reducirse de 5 a 3", 3, libroActualizado.getStock());
    }

    @Test
    public void testBloqueoVentaConStockCero() {
        Editorial ed = editorialCtrl.obtenerTodas().get(0);
        String isbn = "978-999-888-777-6";
        // Registrar con stock 0 (agotado)
        libroCtrl.registrarLibro(isbn, "Libro Agotado", "Autor Desconocido", 100.0, 0, ed.getIdEditorial());

        Libro libroAgotado = libroCtrl.buscarPorIsbn(isbn);
        assertEquals(0, libroAgotado.getStock());

        // Debe lanzar excepción al intentar vender
        try {
            ventaCtrl.procesarVenta(libroAgotado.getIdLibro(), 1);
            fail("Debe lanzar excepción por bloqueo de stock en cero (Ajuste Funcional #2)");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("AGOTADO") || ex.getMessage().contains("BLOQUEO"));
        }
    }

    @Test
    public void testActualizacionPorcentualPrecios() {
        editorialCtrl.registrarEditorial("Editorial De Prueba Ajuste", "Colombia", "test@ajuste.com");
        Editorial ed = editorialCtrl.obtenerTodas().stream()
                .filter(e -> e.getNombre().equals("Editorial De Prueba Ajuste"))
                .findFirst().orElseThrow();

        libroCtrl.registrarLibro("978-000-001", "Libro A", "Autor A", 100.0, 10, ed.getIdEditorial());
        libroCtrl.registrarLibro("978-000-002", "Libro B", "Autor B", 200.0, 10, ed.getIdEditorial());

        // Incrementar +10% en bloque
        int actualizados = libroCtrl.actualizarPreciosPorcentaje(ed.getIdEditorial(), 10.0);
        assertEquals(2, actualizados);

        Libro libroA = libroCtrl.buscarPorIsbn("978-000-001");
        Libro libroB = libroCtrl.buscarPorIsbn("978-000-002");

        assertEquals(110.0, libroA.getPrecio(), 0.01);
        assertEquals(220.0, libroB.getPrecio(), 0.01);
    }
}

