package ar.edu.utn.dds.k3003.model;

public class Subcategoria {
    private String id;
    private String nombre;
    private Categoria categoria;

    public Subcategoria(String id, String nombre, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}