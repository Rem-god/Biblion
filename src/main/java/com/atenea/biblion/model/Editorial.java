package com.atenea.biblion.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa la casa publicadora que suministra los títulos al establecimiento.
 * Entidad persistente gestionada directamente por ObjectDB.
 */
@Entity
public class Editorial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idEditorial;

    @Column(nullable = false)
    private String nombre;

    private String pais;

    private String correo;

    @OneToMany(mappedBy = "editorial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Libro> libros = new ArrayList<>();

    public Editorial() {
    }

    public Editorial(String nombre, String pais, String correo) {
        this.nombre = nombre;
        this.pais = pais;
        this.correo = correo;
        this.libros = new ArrayList<>();
    }

    public long getIdEditorial() {
        return idEditorial;
    }

    public void setIdEditorial(long idEditorial) {
        this.idEditorial = idEditorial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<Libro> getLibros() {
        if (libros == null) {
            libros = new ArrayList<>();
        }
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public void addLibro(Libro libro) {
        if (this.libros == null) {
            this.libros = new ArrayList<>();
        }
        this.libros.add(libro);
        libro.setEditorial(this);
    }

    public void removeLibro(Libro libro) {
        if (this.libros != null) {
            this.libros.remove(libro);
            libro.setEditorial(null);
        }
    }

    public int getCantidadLibros() {
        return libros != null ? libros.size() : 0;
    }

    @Override
    public String toString() {
        return nombre + (pais != null && !pais.isEmpty() ? " (" + pais + ")" : "");
    }
}

