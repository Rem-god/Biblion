package com.atenea.biblion.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa cada título registrado en el catálogo del comercio.
 * Mantiene relación directa con Editorial y con su historial de Ventas.
 */
@Entity
public class Libro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idLibro;

    @Column(nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    private double precio;

    private int stock;

    @ManyToOne(optional = false)
    private Editorial editorial;

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL)
    private List<Venta> ventas = new ArrayList<>();

    public Libro() {
    }

    public Libro(String isbn, String titulo, String autor, double precio, int stock, Editorial editorial) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.precio = precio;
        this.stock = stock;
        this.editorial = editorial;
        this.ventas = new ArrayList<>();
    }

    public long getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(long idLibro) {
        this.idLibro = idLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public void setEditorial(Editorial editorial) {
        this.editorial = editorial;
    }

    public List<Venta> getVentas() {
        if (ventas == null) {
            ventas = new ArrayList<>();
        }
        return ventas;
    }

    public void setVentas(List<Venta> ventas) {
        this.ventas = ventas;
    }

    public void addVenta(Venta venta) {
        if (this.ventas == null) {
            this.ventas = new ArrayList<>();
        }
        this.ventas.add(venta);
        venta.setLibro(this);
    }

    /**
     * Valida si el libro tiene existencias disponibles suficientes.
     */
    public boolean tieneStockSuficiente(int cantidadSolicitada) {
        return this.stock >= cantidadSolicitada && this.stock > 0;
    }

    /**
     * Descuenta del inventario físico la cantidad vendida.
     */
    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (this.stock < cantidad) {
            throw new IllegalStateException("Stock insuficiente: disponibles " + this.stock + ", requeridos " + cantidad);
        }
        this.stock -= cantidad;
    }

    /**
     * Incrementa existencias por recepción de paquetes o devolución.
     */
    public void incrementarStock(int cantidad) {
        if (cantidad > 0) {
            this.stock += cantidad;
        }
    }

    /**
     * Comprueba si el libro se encuentra con inventario crítico o agotado.
     */
    public boolean esInventarioCritico(int umbralMinimo) {
        return this.stock <= umbralMinimo;
    }

    @Override
    public String toString() {
        return titulo + " - " + autor + " [ISBN: " + isbn + "] ($" + String.format("%.2f", precio) + ", Stock: " + stock + ")";
    }
}

