package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
@Table(name = "identificadores")
public class Identificador {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private TipoIdentificador tipo;

    private String descripcion;

    public Identificador() {
    }

    public Identificador(String id,
                         TipoIdentificador tipo,
                         String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public TipoIdentificador getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}