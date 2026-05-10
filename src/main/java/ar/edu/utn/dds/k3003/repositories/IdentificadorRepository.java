package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Identificador;

import java.util.List;
import java.util.Optional;

public interface IdentificadorRepository {

    Identificador save(Identificador identificador);

    Optional<Identificador> findById(String id);

    List<Identificador> findAll();
}