package ar.edu.utn.dds.k3003.model;

public class Identificador {
    private String id;
    private TipoIdentificador tipo;
    private String descripcion;

    public Identificador(String id, TipoIdentificador tipo, String descripcion) {
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