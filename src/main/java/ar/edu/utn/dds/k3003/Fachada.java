package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.services.DonacionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.LocalDate;
import java.util.List;

@Component
public class Fachada implements FachadaDonaciones {

  private final DonacionesService donacionesService;

  @Autowired
  private MeterRegistry meterRegistry;

  public Fachada(DonacionesService donacionesService) {
    this.donacionesService = donacionesService;
  }

  @Override
  public DonacionDTO registrarDonacion(DonacionDTO dto) {
    return donacionesService.registrarDonacion(dto);
  }

  @Override
  public DonacionDTO buscarDonacionPorID(Long id) {
    return donacionesService.buscarDonacionPorID(Long.valueOf(id));
  }

  @Override
  public DonacionDTO cambiarEstadoDeDonacion(Long donacionID, EstadoDonacionEnum estado) {
    return donacionesService.cambiarEstadoDeDonacion(donacionID, estado);
  }

  @Override
  public List<DonacionDTO> buscarPorDonadorYFechaInicio(Long donadorID, LocalDate fecha) {
    return donacionesService.buscarPorDonadorYFechaInicio(String.valueOf(donadorID), fecha);
  }

  @Override
  public DonacionDTO registrarQuejaEnDonacion(Long donacionID, String descripcion) {
    return donacionesService.registrarQuejaEnDonacion(donacionID, descripcion);
  }

  public ProductoDTO agregarProducto(ProductoDTO dto) {
    return donacionesService.agregarProducto(dto);
  }

  public ProductoDTO buscarProductoPorID(Long productoID) {
    return donacionesService.buscarProductoPorID(productoID);
  }

  public List<ProductoDTO> listarProductos() {
    return donacionesService.listarProductos();
  }

  public CategoriaDTO agregarCategoria(CategoriaDTO dto) {
    meterRegistry.counter("donaciones.categorias.agregadas").increment();
    return donacionesService.agregarCategoria(dto);
  }

  public CategoriaDTO buscarCategoriaPorID(Long categoriaID) {
    return donacionesService.buscarCategoriaPorID(categoriaID);
  }

  public List<CategoriaDTO> listarCategorias() {
    return donacionesService.listarCategorias();
  }

  public IdentificadorDTO agregarIdentificador(IdentificadorDTO dto) {
    return donacionesService.agregarIdentificador(dto);
  }

  public IdentificadorDTO buscarIdentificadorPorID(Long identificadorID) {
    return donacionesService.buscarIdentificadorPorID(Long.valueOf(identificadorID));
  }

  public List<IdentificadorDTO> listarIdentificadores() {
    return donacionesService.listarIdentificadores();
  }

  public List<DonacionDTO> listarDonaciones() {
    return donacionesService.listarDonaciones();
  }

  public void limpiarBase() {
    donacionesService.limpiarBase();
  }

  public List<DonacionDTO> buscarPorDonador(Long donadorID) {
    return donacionesService.buscarPorDonador(donadorID);
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    // Ahora se usa DonadoresClient.
  }

  @Override
  public void setFachadaLogistica(FachadaLogistica fachadaLogistica) {
    // Ahora se usa LogisticaClient.
  }
}