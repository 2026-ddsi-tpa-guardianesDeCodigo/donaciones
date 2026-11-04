package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.services.SubcategoriaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subcategorias")
public class SubcategoriaController {

    private final Fachada fachada;

    public SubcategoriaController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<SubcategoriaDTO> agregarSubcategoria(@RequestBody SubcategoriaDTO dto) {
        return ResponseEntity.ok(fachada.agregarSubcategoria(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubcategoriaDTO>> listarSubcategorias() {
        return ResponseEntity.ok(fachada.listarSubcategorias());
    }
}
