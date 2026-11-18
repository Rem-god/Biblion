package com.atenea.biblion.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Registra la transacción comercial efectuada en caja por la compra de un ejemplar.
 * Contiene referencia directa al Libro vendido y calcula automáticamente el total liquidado.
 */
@Entity
public class Venta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idVenta;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private double total;

    @ManyToOne(optional = false)
    private Libro libro;

    public Venta() {
    }

    public Venta(Libro libro, int cantidad) {
        if (libro == null) {
            throw new IllegalArgumentException("El libro de la venta no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        this.libro = libro;
        this.cantidad = cantidad;
        this.total = cantidad * libro.getPrecio();
        this.fechaHora = LocalDateTime.now();
    }

    public Venta(Libro libro, int cantidad, LocalDateTime fechaHora) {
        this(libro, cantidad);
        if (fechaHora != null) {
            this.fechaHora = fechaHora;
        }
    }

    public long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(long idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        if (this.libro != null) {
            this.total = this.cantidad * this.libro.getPrecio();
        }
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
        if (this.libro != null) {
            this.total = this.cantidad * this.libro.getPrecio();
        }
    }

    @Override
    public String toString() {
        return "Venta #" + idVenta + " [" + fechaHora + "] - " +
                (libro != null ? libro.getTitulo() : "N/A") + " x" + cantidad +
                " = $" + String.format("%.2f", total);
    }
}

