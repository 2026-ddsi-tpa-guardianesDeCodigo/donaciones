package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Producto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductoRepo implements ProductoRepository {

    private Map<String, Producto> productos = new HashMap<>();

    @Override
    public Producto save(Producto producto) {
        productos.put(producto.getId(), producto);
        return producto;
    }

    @Override
    public Optional<Producto> findById(String id) {
        return Optional.ofNullable(productos.get(id));
    }
}