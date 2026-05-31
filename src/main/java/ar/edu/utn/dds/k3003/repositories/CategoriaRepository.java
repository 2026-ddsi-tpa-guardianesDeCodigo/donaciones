package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {
}