package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Donacion;

import java.util.*;

public class InMemoryDonacionesRepo implements DonacionesRepository {

    private Map<String, Donacion> donaciones = new HashMap<>();

    @Override
    public Donacion save(Donacion donacion) {
        donaciones.put(donacion.getId(), donacion);
        return donacion;
    }

    @Override
    public Optional<Donacion> findById(String id) {
        return Optional.ofNullable(donaciones.get(id));
    }

    @Override
    public void deleteById(String id) {
        donaciones.remove(id);
    }

    @Override
    public List<Donacion> findAll() {
        return new ArrayList<>(donaciones.values());
    }
}
