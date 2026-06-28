package ar.edu.utn.dds.k3003.catedra.fachadas;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public interface FachadaDonaciones {

  DonacionDTO registrarDonacion(DonacionDTO donacionDTO);

  DonacionDTO buscarDonacionPorID(Long donacionID) throws NoSuchElementException;

  DonacionDTO cambiarEstadoDeDonacion(Long donacionID, EstadoDonacionEnum estado)
      throws NoSuchElementException;

  List<DonacionDTO> buscarPorDonadorYFechaInicio(Long donadorID, LocalDate fecha)
      throws NoSuchElementException;

  DonacionDTO registrarQuejaEnDonacion(Long donacionID, String descripcion);

  ProductoDTO agregarProducto(ProductoDTO productoDTO);

  ProductoDTO buscarProductoPorID(Long productoID) throws NoSuchElementException;

  IdentificadorDTO agregarIdentificador(IdentificadorDTO identificadorDTO);

  IdentificadorDTO buscarIdentificadorPorID(Long identificadorID) throws NoSuchElementException;

  void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades);

  void setFachadaLogistica(FachadaLogistica fachadaLogistica);
}
