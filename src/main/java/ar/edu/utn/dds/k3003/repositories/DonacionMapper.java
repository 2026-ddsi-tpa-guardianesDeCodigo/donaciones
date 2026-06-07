package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.model.Donacion;

public class DonacionMapper {

    public DonacionDTO toDonacionDTO(Donacion donacion) {
        return new DonacionDTO(
                donacion.getId(),
                donacion.getDonadorID(),
                donacion.getDepositoID(),
                donacion.getDescripcion(),
                donacion.getProductoID(),
                donacion.getCantidad(),
                donacion.getEstado()
        );
    }
}