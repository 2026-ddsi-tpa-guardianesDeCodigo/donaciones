package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.eventos.PublicadorDeCambios;
import jakarta.persistence.*;

@Entity
@EntityListeners(PublicadorDeCambios.class)
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Versión optimista (JPA). Viaja en cada evento del modelo de lectura (E6): el consumidor solo
    // aplica snapshots con versión mayor a la que ya tiene. El default de columna cubre las filas
    // existentes cuando ddl-auto=update agrega la columna.
    @Version
    @Column(name = "version", columnDefinition = "bigint default 0")
    private Long version;

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

    public Long getVersion() {
        return version;
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

    public void setSubcategoria(Subcategoria subcategoria) {
        this.subcategoria = subcategoria;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}