package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.model.Categoria;
import ar.edu.utn.dds.k3003.model.Identificador;
import ar.edu.utn.dds.k3003.model.Producto;

public class ProductoDataMapper {

    public Producto toProducto(ProductoDTO dto,
                               Categoria categoria,
                               Long subcategoria,
                               Identificador identificador) {
        if (dto == null) {
            return null;
        }

        return new Producto(
                dto.id(),
                dto.nombre(),
                dto.descripcion(),
                categoria,
                identificador
        );
    }

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