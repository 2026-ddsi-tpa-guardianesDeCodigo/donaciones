package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.eventos.PublicadorDeCambios;
import jakarta.persistence.*;

@Entity
@EntityListeners(PublicadorDeCambios.class)
@Table(name = "productos")
public class Producto {

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
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "identificador_id")
    private Identificador identificador;

    public Producto() {
    }

    public Producto(Long id,
                    String nombre,
                    String descripcion,
                    Categoria categoria,
                    Identificador identificador) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.identificador = identificador;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public Identificador getIdentificador() {
        return identificador;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setIdentificador(Identificador identificador) {
        this.identificador = identificador;
    }
}