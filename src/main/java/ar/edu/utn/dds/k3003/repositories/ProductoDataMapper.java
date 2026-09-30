package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.model.Producto;

public class ProductoDataMapper {

    // toProducto() se borró: código muerto, nada lo llamaba (agregarProducto en DonacionesService
    // construye el Producto directo con `new Producto(...)`, no pasa por este mapper).

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) {
            return null;
        }

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getId(),
                producto.getIdentificador().getId()
        );
    }
}