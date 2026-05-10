package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Categoria;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCategoriaRepo implements CategoriaRepository {
    private final Map<String, Categoria> categorias = new HashMap<>();

    @Override
    public Optional<Categoria> findById(String id) {
        return Optional.ofNullable(categorias.get(id));
    }

    @Override
    public Categoria save(Categoria categoria) {
        categorias.put(categoria.getId(), categoria);
        return categoria;
    }
}