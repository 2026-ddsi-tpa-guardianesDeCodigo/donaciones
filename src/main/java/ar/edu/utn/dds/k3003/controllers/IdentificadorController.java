package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/identificadores")
public class IdentificadorController {

    private Fachada fachada;

    public IdentificadorController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<IdentificadorDTO> agregarIdentificador(@RequestBody IdentificadorDTO identificadorDTO) {
        IdentificadorDTO identificadorAgregado = fachada.agregarIdentificador(identificadorDTO);
        return ResponseEntity.ok(identificadorAgregado);
    }

    @GetMapping
    public ResponseEntity<IdentificadorDTO> buscarIdentificadorPorID(@RequestParam String identificadorID) {
        IdentificadorDTO identificador = fachada.buscarIdentificadorPorID(identificadorID);
        return ResponseEntity.ok(identificador);
    }

    @GetMapping("/all")
    public ResponseEntity<List<IdentificadorDTO>> listarIdentificadores() {
        return ResponseEntity.ok(fachada.listarIdentificadores());
    }
}