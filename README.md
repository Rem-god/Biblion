# Biblion — Sistema de Gestión para Librería y Distribuidora Cultural "Atenea"

**Proyecto Académico Integrador:**
- **Institución:** Tecnológico Superior de Jalisco
- **Carrera:** Ingeniería Informática (Séptimo Semestre)
- **Alumno:** Román Emmanuel Castañeda Barboza (Código: `230110841`)
- **Materias:** Tópicos de Bases de Datos & Fundamentos de Gestión de TI
- **Instructor:** José Luis Cobián Hermosillo
- **Tecnologías:** Java 21, ObjectDB 2.8.9 (JPA 2.2), Maven 3.9, FlatLaf Swing, JUnit 4

---

## 📖 Descripción del Problema y Caso de Estudio

En una librería independiente y distribuidora cultural de tamaño mediano (**Librería y Distribuidora Cultural "Atenea"**), el control del catálogo presentaba desorganización operativa debido al manejo disperso de fichas de proveedores editoriales, existencias físicas de libros y el registro de ventas diarias.

Al carecer de una estructura unificada, resultaba complejo:
1. Determinar qué títulos pertenecen a cada editorial asociada.
2. Saber si existen existencias suficientes en almacén para satisfacer la demanda sin desabasto.
3. Rastrear con exactitud qué libros generan el mayor flujo de ingresos y formalizar el arqueo de caja diario.

---

## 🏛️ Arquitectura y Paradigma Tecnológico

### 1. Base de Datos Orientada a Objetos (ObjectDB + JPA)
En lugar de descomponer la información en tablas rígidas y normalizadas unidas mediante claves foráneas (*foreign keys*) que generan el desajuste de impedancia objeto-relacional (*object-relational impedance mismatch*), **Biblion** almacena directamente las instancias de clases en un único archivo persistente `.odb` (`db/biblion.odb`).
- Las relaciones entre entidades se resuelven mediante **punteros y referencias directas en memoria**.
- Consultas mediante **JPQL** orientadas a clases y atributos.
- Operaciones atómicas y transaccionales con `EntityManager` y `EntityTransaction`.

### 2. Patrón Arquitectónico MVC (Modelo - Vista - Controlador)
- **Modelo (`com.atenea.biblion.model`, `dao`):**
  - Entidades JPA: [`Editorial`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/model/Editorial.java), [`Libro`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/model/Libro.java), [`Venta`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/model/Venta.java).
  - Capa de Acceso a Datos: [`DatabaseManager`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/dao/DatabaseManager.java), [`EditorialDAO`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/dao/EditorialDAO.java), [`LibroDAO`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/dao/LibroDAO.java), [`VentaDAO`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/dao/VentaDAO.java).
- **Controlador (`com.atenea.biblion.controller`):**
  - Mediador que valida reglas de negocio, disponibilidad de inventario, bloqueos y transacciones: [`EditorialController`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/controller/EditorialController.java), [`LibroController`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/controller/LibroController.java), [`VentaController`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/controller/VentaController.java), [`ReporteController`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/controller/ReporteController.java).
- **Vista (`com.atenea.biblion.view`):**
  - Interfaz gráfica moderna de escritorio en Java Swing asistida por **FlatLaf**, diseñada para minimizar la brecha digital y acelerar el trabajo de los dependientes y cajeros.

---

## 🔗 Relaciones entre Entidades

```
[ Editorial ] 1 <===============> N [ Libro ] 1 <===============> N [ Venta ]
```

1. **`Editorial <--------> Libro` (1 a Muchos / Bidireccional):**
   - Una `Editorial` mantiene una colección directa `List<Libro> libros`.
   - Cada `Libro` contiene una referencia directa `@ManyToOne Editorial editorial`.
   - Permite consultar inmediatamente el catálogo anidado de cualquier editorial sin sentencias SQL `JOIN`.
2. **`Libro <--------> Venta` (1 a Muchos / Bidireccional):**
   - Cada `Libro` conserva una colección cronológica `List<Venta> ventas`.
   - Cada objeto `Venta` referencia directamente al `Libro` vendido para extraer precio y descontar transaccionalmente existencias de inventario.

---

## ⚡ Ajustes Funcionales del Diagnóstico Estratégico

El sistema incorpora al 100% los ajustes funcionales identificados en el documento de Gestión de TI:

1. **Entrada ágil por código de barras (Pistoleo USB):**
   - El foco permanece de manera predeterminada y automática en el campo del ISBN.
   - Soporte para pistolear libros consecutivamente con escáner óptico (o pulsar Enter) sin necesidad de tocar el ratón.
2. **Bloqueo visual inmediato de venta con stock en cero:**
   - Si se selecciona o pistolea un ejemplar agotado (`stock <= 0`), la interfaz despliega una tarjeta de alerta en rojo y desactiva el botón de cobro.
   - El controlador prohíbe y aborta cualquier transacción con unidades insuficientes.
3. **Módulo de actualización porcentual de precios en bloque:**
   - Permite a la gerencia seleccionar una editorial e ingresar un porcentaje de ajuste (+10%, +5%, -10%, etc.).
   - Actualiza de forma atómica en ObjectDB los precios de todos los libros vinculados a dicha editorial, eliminando la necesidad de editarlos título por título.
4. **Respaldo formal de la base de datos ObjectDB (Riesgo P6):**
   - Diálogo dedicado para generar copias instantáneas de `db/biblion.odb` hacia la carpeta `backups/` con sello de tiempo.
5. **Recepción formal de paquetería y reposición a proveedores (Riesgo P3):**
   - Panel de inventario crítico con alertas de desabasto, cálculo de pedido sugerido a editoriales y botón de recepción rápida de mercancía (+stock).

---

## 🚀 Instrucciones de Ejecución

### Opción A: Ejecución Rápida con Doble Clic (Windows)
Haga doble clic en el archivo [run.bat](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/run.bat) o ejecute en PowerShell:
```powershell
.\run.ps1
```

### Opción B: Ejecución directa del JAR ejecutable
```bash
java -jar target/biblion-1.0.0.jar
```

### Opción C: Compilar y Ejecutar con Maven
```bash
mvn compile exec:java
```

### Opción D: Ejecutar Suite de Pruebas Unitarias (JUnit)
```bash
mvn test
```

---

## 📁 Estructura del Código Fuente

- [`pom.xml`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/pom.xml): Coordenadas del proyecto, dependencias (ObjectDB, JPA, FlatLaf, JUnit) y plugins de compilación/empaquetado.
- [`src/main/java/com/atenea/biblion/Main.java`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/Main.java): Arranque del sistema y activación de FlatLaf.
- [`src/main/java/com/atenea/biblion/model/`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/model/): Clases persistentes `Editorial`, `Libro`, `Venta`.
- [`src/main/java/com/atenea/biblion/dao/`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/dao/): Conexión a `db/biblion.odb` y DAOs con transacciones JPA.
- [`src/main/java/com/atenea/biblion/controller/`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/controller/): Controladores de lógica de negocio y validación.
- [`src/main/java/com/atenea/biblion/view/`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/main/java/com/atenea/biblion/view/): Vistas Swing modernas (Punto de Venta, Catálogo, Editoriales, Inventario Crítico, Arqueo, Respaldos).
- [`src/test/java/com/atenea/biblion/BiblionTest.java`](file:///C:/Users/Rom%C3%A1n%20Emmanuel/.gemini/antigravity/scratch/biblion/src/test/java/com/atenea/biblion/BiblionTest.java): Pruebas automatizadas de persistencia, navegación de grafos, ventas transaccionales, bloqueo de stock 0 y actualización porcentual.

