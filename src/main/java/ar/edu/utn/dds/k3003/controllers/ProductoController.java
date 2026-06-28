package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private Fachada fachada;

    public ProductoController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> agregarProducto(@RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoAgregado = fachada.agregarProducto(productoDTO);
        return ResponseEntity.ok(productoAgregado);
    }

    @GetMapping("/{productoID}")
    public ResponseEntity<ProductoDTO> buscarProductoPorID(@PathVariable String productoID) {
        ProductoDTO producto = fachada.buscarProductoPorID(Long.valueOf(productoID));
        return ResponseEntity.ok(producto);
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(fachada.listarProductos());
    }
}