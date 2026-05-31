package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    private String id;

    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    public Categoria() {
    }

    public Categoria(String id,
                     String nombre,
                     String descripcion,
                     Subcategoria subcategoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Subcategoria getSubcategoria() {
        return subcategoria;
    }
}