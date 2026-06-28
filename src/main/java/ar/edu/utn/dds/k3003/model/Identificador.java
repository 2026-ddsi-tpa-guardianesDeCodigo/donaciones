package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
@Table(name = "identificadores")
public class Identificador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoIdentificador tipo;

    private String descripcion;

    public Identificador() {
    }

    public Identificador(Long id,
                         TipoIdentificador tipo,
                         String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public TipoIdentificador getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}