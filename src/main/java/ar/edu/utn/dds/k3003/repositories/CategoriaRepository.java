package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Categoria;

import java.util.Optional;

public interface CategoriaRepository {

    Optional<Categoria> findById(String id);

    Categoria save(Categoria categoria);
}
