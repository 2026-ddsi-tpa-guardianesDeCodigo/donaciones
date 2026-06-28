package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Donador;
import java.util.Optional;

public interface DonadoresRepository {
  Optional<Donador> findById(String id);

  Optional<Donador> findById(Long id);

  Donador save(Donador donador);

  Donador deleteById(Long id);

  Donador deleteById(String id);
}
