package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Donacion;

import java.util.List;
import java.util.Optional;

public interface DonacionesRepository {

    Donacion save(Donacion donacion);

    Optional<Donacion> findById(String id);

    void deleteById(String id);

    List<Donacion> findAll();
}