package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.services.DonacionesService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class Fachada implements FachadaDonaciones {

  private final DonacionesService donacionesService;

  public Fachada(DonacionesService donacionesService) {
    this.donacionesService = donacionesService;
  }

  @Override
  public DonacionDTO registrarDonacion(DonacionDTO dto) {
    return donacionesService.registrarDonacion(dto);
  }

  @Override
  public DonacionDTO buscarDonacionPorID(String id) {
    return donacionesService.buscarDonacionPorID(id);
  }

  @Override
  public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado) {
    return donacionesService.cambiarEstadoDeDonacion(donacionID, estado);
  }

  @Override
  public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) {
    return donacionesService.buscarPorDonadorYFechaInicio(donadorID, fecha);
  }

  @Override
  public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
    return donacionesService.registrarQuejaEnDonacion(donacionID, descripcion);
  }

  public List<DonacionDTO> listarDonaciones() {
    return donacionesService.listarDonaciones();
  }

  public void limpiarBase() {
    donacionesService.limpiarBase();
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    // Ya no se usa porque ahora se comunica por HTTP usando DonadoresClient.
  }

  @Override
  public void setFachadaLogistica(FachadaLogistica fachadaLogistica) {
    // Ya no se usa porque ahora se comunica por HTTP usando LogisticaClient.
  }
}