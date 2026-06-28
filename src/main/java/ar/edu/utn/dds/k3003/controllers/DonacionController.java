package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private Fachada fachada;

    public DonacionController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<DonacionDTO> registrarDonacion(@RequestBody DonacionDTO donacionDTO) {
        DonacionDTO donacionRegistrada = fachada.registrarDonacion(donacionDTO);
        return ResponseEntity.ok(donacionRegistrada);
    }


    @PatchMapping("/estado")
    public ResponseEntity<DonacionDTO> cambiarEstadoDeDonacion(
            @RequestParam Long donacionID,
            @RequestBody EstadoDonacionEnum estado
    ) {
        DonacionDTO donacionActualizada = fachada.cambiarEstadoDeDonacion(donacionID, estado);
        return ResponseEntity.ok(donacionActualizada);
    }

    @GetMapping("/buscarPorDonador/{donadorID}")
    public ResponseEntity<List<DonacionDTO>> buscarPorDonador(
            @PathVariable Long donadorID
    ) {
        List<DonacionDTO> donaciones = fachada.buscarPorDonador(donadorID);
        return ResponseEntity.ok(donaciones);
    }

    @PatchMapping("/queja")
    public ResponseEntity<DonacionDTO> registrarQuejaEnDonacion(
            @RequestParam Long donacionID,
            @RequestParam String descripcion
    ) {
        DonacionDTO donacion = fachada.registrarQuejaEnDonacion(donacionID, descripcion);
        return ResponseEntity.ok(donacion);
    }
    @GetMapping("/{donacionID}")
    public ResponseEntity<DonacionDTO> buscarDonacionPorID(@PathVariable Long donacionID) {
        DonacionDTO donacion = fachada.buscarDonacionPorID(donacionID);
        return ResponseEntity.ok(donacion);
    }

    @GetMapping
    public ResponseEntity<List<DonacionDTO>> listarDonaciones() {
        return ResponseEntity.ok(fachada.listarDonaciones());
    }
}