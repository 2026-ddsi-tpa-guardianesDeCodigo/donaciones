package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonacionesRepository extends JpaRepository<Donacion, Long> {
}