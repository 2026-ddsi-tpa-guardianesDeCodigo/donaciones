package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Producto;

import java.util.Optional;

public interface ProductoRepository {

    Producto save(Producto producto);

    Optional<Producto> findById(String id);
}