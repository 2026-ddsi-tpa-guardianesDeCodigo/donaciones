package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    public Categoria() {
    }

    public Categoria(Long id,
                     String nombre,
                     String descripcion,
                     Subcategoria subcategoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
    }

    public Long getId() {
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