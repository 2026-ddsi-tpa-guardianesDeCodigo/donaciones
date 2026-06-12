package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.CategoriaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private Fachada fachada;

    public CategoriaController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> agregarCategoria(@RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaAgregada = fachada.agregarCategoria(categoriaDTO);
        return ResponseEntity.ok(categoriaAgregada);
    }

    @GetMapping
    public ResponseEntity<CategoriaDTO> buscarCategoriaPorID(@RequestParam String categoriaID) {
        CategoriaDTO categoria = fachada.buscarCategoriaPorID(categoriaID);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping()
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(fachada.listarCategorias());
    }
}