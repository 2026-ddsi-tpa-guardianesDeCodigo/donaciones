package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Identificador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryIdentificadorRepo implements IdentificadorRepository {

    private Map<String, Identificador> identificadores = new HashMap<>();

    @Override
    public Identificador save(Identificador identificador) {
        identificadores.put(identificador.getId(), identificador);
        return identificador;
    }

    @Override
    public Optional<Identificador> findById(String id) {
        return Optional.ofNullable(identificadores.get(id));
    }

    @Override
    public List<Identificador> findAll() {
        return List.of();
    }
}