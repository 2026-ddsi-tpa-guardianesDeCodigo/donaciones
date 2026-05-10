package ar.edu.utn.dds.k3003.model;

public class Categoria {
    private String id;
    private String nombre;
    private Subcategoria subcategoria;

    public Categoria(String id, String nombre, Subcategoria subcategoria) {
        this.id = id;
        this.nombre = nombre;
        this.subcategoria = subcategoria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Subcategoria getSubcategoria() {
        return subcategoria;
    }
}