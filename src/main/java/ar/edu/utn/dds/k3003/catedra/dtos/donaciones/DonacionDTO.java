package ar.edu.utn.dds.k3003.catedra.dtos.donaciones;

public record DonacionDTO(
        Long id,
        String donadorID,
        String depositoID,
        String descripcion,
        Long productoID,
        Integer cantidad,
        EstadoDonacionEnum estado) {}
