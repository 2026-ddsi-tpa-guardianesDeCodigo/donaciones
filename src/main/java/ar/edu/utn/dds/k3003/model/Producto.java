package ar.edu.utn.dds.k3003.model;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private Categoria categoria;
    private Identificador identificador;

    public Producto(String id,
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

    public String getId() {
        return id;
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
}