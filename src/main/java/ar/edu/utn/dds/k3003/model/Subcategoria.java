package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subcategorias")
public class Subcategoria {

    @Id
    private String id;

    private String nombre;

    public Subcategoria() {
    }

    public Subcategoria(String id, String nombre,Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}