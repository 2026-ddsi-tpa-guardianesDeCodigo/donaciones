package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.*;
import org.springframework.stereotype.Component;

import java.util.List;

//package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;

import java.time.LocalDate;
@Component
public class Fachada implements FachadaDonaciones {

  private DonacionesRepository donacionesRepository;
  private ProductoRepository productoRepository;
  private DonadoresRepository donadoresRepository;
  private IdentificadorRepository identificadorRepository;
  private CategoriaRepository categoriaRepository;



  private final DonacionMapper donacionMapper = new DonacionMapper();
  private final ProductoDataMapper productoDataMapper = new ProductoDataMapper();

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaLogistica fachadaLogistica;


  public Fachada() {
    this.donacionesRepository = new InMemoryDonacionesRepo();
    this.productoRepository = new InMemoryProductoRepo();
    this.donadoresRepository = new InMemoryDonadoresRepo();
    this.identificadorRepository = new InMemoryIdentificadorRepo();
    this.categoriaRepository = new InMemoryCategoriaRepo();
    this.categoriaRepository.save(new Categoria("1", "Mobiliario",new Subcategoria("1", "Sillas", null)));
  }

  @Override
  public DonacionDTO registrarDonacion(DonacionDTO dto) {
    if (dto == null) {
      throw new RuntimeException("Donacion invalida");
    }


    if (dto.id() != null && donacionesRepository.findById(dto.id()).isPresent()) {
      throw new RuntimeException("La donación ya fue registrada");
    }

    

    // para satisfacer ese test
    if (fachadaLogistica != null) {
      fachadaLogistica.gestionarDonacion(
              dto.depositoID(),
              dto.id(),
              dto.productoID(),
              dto.cantidad()
      );
    }
    int siguienteIdDonacion = 1;

    if (fachadaDonadoresYEntidades != null) {
      fachadaDonadoresYEntidades.buscarDonadorPorID(dto.donadorID());

      Boolean puedeDonar = fachadaDonadoresYEntidades.puedeDonar(dto.donadorID());
      if (puedeDonar == null || !puedeDonar) {
        throw new RuntimeException("No puede donar");
      }
    }

    String idDonacion = (dto.id() != null) ? dto.id() : "donacion" + siguienteIdDonacion++;

    Producto producto = buscarProductoInternoPorID(dto.productoID());

    Donacion donacion = new Donacion(
            idDonacion,
            dto.donadorID(),
            dto.depositoID(),
            dto.descripcion(),
            producto,
            dto.cantidad(),
            EstadoDonacionEnum.INGRESADA,
            LocalDate.now()
    );

    donacionesRepository.save(donacion);

    return donacionMapper.toDonacionDTO(donacion);
  }

  public ProductoDTO buscarProductoPorID(String productoID) {
    Producto producto = productoRepository.findById(productoID)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    return productoDataMapper.toDTO(producto);
  }

  private Producto buscarProductoInternoPorID(String productoID) {
    return productoRepository.findById(productoID)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
  }

  public ProductoDTO agregarProducto(ProductoDTO dto) {
    if (dto == null) {
      throw new RuntimeException("Producto invalido");
    }

    if (dto.id() != null && productoRepository.findById(dto.id()).isPresent()) {
      throw new RuntimeException("El producto ya existe");
    }

    Categoria categoria = categoriaRepository.findById(dto.categoriaID())
            .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));

    Identificador identificador = identificadorRepository.findById(dto.identificadorID())
            .orElseThrow(() -> new RuntimeException("Identificador no encontrado"));

    if (!esValidoSegunIdentificador(dto.nombre(), dto.descripcion(), identificador)) {
      throw new RuntimeException("Producto invalido segun identificador");
    }

    Producto producto = new Producto(
            dto.id(),
            dto.nombre(),
            dto.descripcion(),
            categoria,
            identificador
    );

    productoRepository.save(producto);

    return productoDataMapper.toDTO(producto);
  }

  private boolean esValidoSegunIdentificador(String nombre, String descripcion, Identificador identificador) {
    if (identificador.getTipo() == TipoIdentificador.CODIGO_BARRAS) {
      return contarPalabras(descripcion) >= 3;
    }

    if (identificador.getTipo() == TipoIdentificador.CODIGO_QR) {
      return contarLetras(nombre) % 2 == 0;
    }

    return false;
  }

  private int contarPalabras(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
      return 0;
    }
    return texto.trim().split("\\s+").length;
  }

  private int contarLetras(String texto) {
    if (texto == null) {
      return 0;
    }

    int cantidad = 0;
    for (char c : texto.toCharArray()) {
      if (Character.isLetter(c)) {
        cantidad++;
      }
    }
    return cantidad;
  }

  @Override
  public DonacionDTO buscarDonacionPorID(String id) {
    Donacion donacion = donacionesRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Donacion no encontrada"));

    return donacionMapper.toDonacionDTO(donacion);
  }

  @Override
  public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado) {
    if (donacionID == null || donacionID.isBlank()) {
      throw new RuntimeException("Donacion invalida");
    }

    if (estado == null) {
      throw new RuntimeException("Estado invalido");
    }

    Donacion donacion = donacionesRepository.findById(donacionID)
            .orElseThrow(() -> new RuntimeException("Donacion no encontrada"));

    EstadoDonacionEnum estadoActual = donacion.getEstado();

    if (estado == EstadoDonacionEnum.ACEPTADA && estadoActual != EstadoDonacionEnum.INGRESADA) {
      throw new RuntimeException("Transicion invalida: para aceptar, la donacion debe estar INGRESADA");
    }

    if (estado == EstadoDonacionEnum.CONQUEJA && estadoActual != EstadoDonacionEnum.ACEPTADA) {
      throw new RuntimeException("Transicion invalida: para registrar queja, la donacion debe estar ACEPTADA");
    }

    donacion.setEstado(estado);
    donacionesRepository.save(donacion);

    return donacionMapper.toDonacionDTO(donacion);
  }

  @Override
  public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) {
    List<DonacionDTO> resultado = donacionesRepository.findAll().stream()
            .filter(d -> d.getDonadorID().equals(donadorID))
            .filter(d -> !d.getFecha().isBefore(fecha))
            .map(donacionMapper::toDonacionDTO)
            .toList();

    if (resultado.isEmpty()) {
      throw new RuntimeException("No se encontraron donaciones");
    }

    return resultado;
  }

  @Override
  public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
    if (donacionID == null || donacionID.isBlank()) {
      throw new RuntimeException("Donacion invalida");
    }

    Donacion donacion = donacionesRepository.findById(donacionID)
            .orElseThrow(() -> new RuntimeException("Donacion no encontrada"));

    if (fachadaDonadoresYEntidades != null) {
      QuejaDTO queja = new QuejaDTO(
              null,
              donacionID,
              donacion.getDonadorID(),
              null,
              descripcion
      );

      fachadaDonadoresYEntidades.agregarQueja(queja);
    }

    donacion.setDescripcion(descripcion);
    donacionesRepository.save(donacion);

    return cambiarEstadoDeDonacion(donacionID, EstadoDonacionEnum.CONQUEJA);
  }

  public IdentificadorDTO buscarIdentificadorPorID(String identificadorID) {
    Identificador identificador = identificadorRepository.findById(identificadorID)
            .orElseThrow(() -> new RuntimeException("Identificador no encontrado"));

    TipoIdentificadorEnum tipoDTO;

    if (identificador.getTipo() == TipoIdentificador.CODIGO_QR) {
      tipoDTO = TipoIdentificadorEnum.QR;
    } else if (identificador.getTipo() == TipoIdentificador.CODIGO_BARRAS) {
      tipoDTO = TipoIdentificadorEnum.CODIGODEBARRAS;
    } else {
      throw new RuntimeException("Tipo de identificador invalido");
    }

    return new IdentificadorDTO(
            identificador.getId(),
            tipoDTO,
            identificador.getDescripcion()
    );
  }

  public IdentificadorDTO agregarIdentificador(IdentificadorDTO dto) {
    if (dto == null) {
      throw new RuntimeException("Identificador invalido");
    }

    if (dto.id() != null && identificadorRepository.findById(dto.id()).isPresent()) {
      throw new RuntimeException("El identificador ya existe");
    }

    Identificador identificador = new Identificador(
            dto.id(),
            dto.tipo() == TipoIdentificadorEnum.QR
                    ? TipoIdentificador.CODIGO_QR
                    : TipoIdentificador.CODIGO_BARRAS,
            dto.descripcion()
    );

    identificadorRepository.save(identificador);

    return new IdentificadorDTO(
            identificador.getId(),
            identificador.getTipo() == TipoIdentificador.CODIGO_QR
                    ? TipoIdentificadorEnum.QR
                    : TipoIdentificadorEnum.CODIGODEBARRAS,
            identificador.getDescripcion()
    );
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

  @Override
  public void setFachadaLogistica(FachadaLogistica fachadaLogistica) {
    this.fachadaLogistica = fachadaLogistica;
  }
}