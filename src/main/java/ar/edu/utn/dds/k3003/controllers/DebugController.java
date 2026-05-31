package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final Fachada fachada;

    public DebugController(Fachada fachada) {
        this.fachada = fachada;
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> limpiarBase() {
        fachada.limpiarBase();
        return ResponseEntity.ok("Base limpiada correctamente");
    }
}