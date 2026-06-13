package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.*;
import ar.edu.utn.dds.k3003.metrics.DonacionesMetrics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonacionesService {

    private final DonacionesRepository donacionesRepository;
    private final ProductoRepository productoRepository;
    private final IdentificadorRepository identificadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final DonadoresClient donadoresClient;
    private final LogisticaClient logisticaClient;
    private final DonacionesMetrics metrics;

    private final DonacionMapper donacionMapper = new DonacionMapper();
    private final ProductoDataMapper productoDataMapper = new ProductoDataMapper();

    public DonacionesService(
            DonacionesRepository donacionesRepository,
            ProductoRepository productoRepository,
            IdentificadorRepository identificadorRepository,
            CategoriaRepository categoriaRepository,
            DonadoresClient donadoresClient,
            LogisticaClient logisticaClient,
            DonacionesMetrics metrics
    ) {
        this.donacionesRepository = donacionesRepository;
        this.productoRepository = productoRepository;
        this.identificadorRepository = identificadorRepository;
        this.categoriaRepository = categoriaRepository;
        this.donadoresClient = donadoresClient;
        this.logisticaClient = logisticaClient;
        this.metrics = metrics;
    }

    public DonacionDTO registrarDonacion(DonacionDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Donacion invalida");
        }

        if (dto.id() != null && donacionesRepository.findById(dto.id()).isPresent()) {
            throw new RuntimeException("La donación ya fue registrada");
        }

        donadoresClient.buscarDonadorPorID(dto.donadorID());

        Boolean puedeDonar = donadoresClient.puedeDonar(dto.donadorID());
        if (puedeDonar == null || !puedeDonar) {
            throw new RuntimeException("No puede donar");
        }

        logisticaClient.gestionarDonacion(
                dto.depositoID(),
                dto.id(),
                dto.productoID(),
                dto.cantidad()
        );

        buscarProductoInternoPorID(dto.productoID());

        Donacion donacion = new Donacion(
                dto.id(),
                dto.donadorID(),
                dto.depositoID(),
                dto.descripcion(),
                dto.productoID(),
                dto.cantidad(),
                EstadoDonacionEnum.INGRESADA,
                LocalDate.now()
        );

        donacionesRepository.save(donacion);

        if (metrics != null) {
            metrics.incrementarDonacionesRegistradas();
        }

        return donacionMapper.toDonacionDTO(donacion);
    }

    public DonacionDTO buscarDonacionPorID(String id) {
        Donacion donacion = donacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donacion no encontrada"));

        return donacionMapper.toDonacionDTO(donacion);
    }

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

    public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) {
        return donacionesRepository.findAll().stream()
                .filter(d -> d.getDonadorID() != null)
                .filter(d -> d.getDonadorID().trim().equals(donadorID.trim()))
                .filter(d -> d.getFecha() != null)
                .filter(d -> !d.getFecha().isBefore(fecha))
                .map(donacionMapper::toDonacionDTO)
                .toList();
    }

    public DonacionDTO registrarQuejaEnDonacion(String donacionID, String descripcion) {
        if (donacionID == null || donacionID.isBlank()) {
            throw new RuntimeException("Donacion invalida");
        }

        Donacion donacion = donacionesRepository.findById(donacionID)
                .orElseThrow(() -> new RuntimeException("Donacion no encontrada"));

        QuejaDTO queja = new QuejaDTO(
                null,
                donacionID,
                donacion.getDonadorID(),
                null,
                descripcion
        );

        donadoresClient.agregarQueja(queja);

        donacion.setDescripcion(descripcion);
        donacionesRepository.save(donacion);

        return cambiarEstadoDeDonacion(donacionID, EstadoDonacionEnum.CONQUEJA);
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

        if (metrics != null) {
            metrics.incrementarProductosRegistrados();
        }

        return productoDataMapper.toDTO(producto);
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

    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(productoDataMapper::toDTO)
                .toList();
    }

    public CategoriaDTO agregarCategoria(CategoriaDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Categoria invalida");
        }

        if (dto.id() != null && categoriaRepository.findById(dto.id()).isPresent()) {
            throw new RuntimeException("La categoria ya existe");
        }

        Categoria categoria = new Categoria(
                dto.id(),
                dto.nombre(),
                dto.descripcion(),
                null
        );

        categoriaRepository.save(categoria);

        if (metrics != null) {
            metrics.incrementarCategoriasRegistradas();
        }

        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                null
        );
    }

    public CategoriaDTO buscarCategoriaPorID(String categoriaID) {
        Categoria categoria = categoriaRepository.findById(categoriaID)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));

        String subcategoriaID = categoria.getSubcategoria() != null
                ? categoria.getSubcategoria().getId()
                : null;

        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                subcategoriaID
        );
    }

    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDTO(
                        c.getId(),
                        c.getNombre(),
                        c.getDescripcion(),
                        c.getSubcategoria() != null ? c.getSubcategoria().getId() : null
                ))
                .toList();
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

        if (metrics != null) {
            metrics.incrementarIdentificadoresRegistrados();
        }

        return buscarIdentificadorPorID(identificador.getId());
    }

    public IdentificadorDTO buscarIdentificadorPorID(String identificadorID) {
        Identificador identificador = identificadorRepository.findById(identificadorID)
                .orElseThrow(() -> new RuntimeException("Identificador no encontrado"));

        return new IdentificadorDTO(
                identificador.getId(),
                identificador.getTipo() == TipoIdentificador.CODIGO_QR
                        ? TipoIdentificadorEnum.QR
                        : TipoIdentificadorEnum.CODIGODEBARRAS,
                identificador.getDescripcion()
        );
    }

    public List<IdentificadorDTO> listarIdentificadores() {
        return identificadorRepository.findAll().stream()
                .map(i -> new IdentificadorDTO(
                        i.getId(),
                        i.getTipo() == TipoIdentificador.CODIGO_QR
                                ? TipoIdentificadorEnum.QR
                                : TipoIdentificadorEnum.CODIGODEBARRAS,
                        i.getDescripcion()
                ))
                .toList();
    }

    public List<DonacionDTO> listarDonaciones() {
        return donacionesRepository.findAll().stream()
                .map(donacionMapper::toDonacionDTO)
                .toList();
    }

    public void limpiarBase() {
        donacionesRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        identificadorRepository.deleteAll();
    }

    private boolean esValidoSegunIdentificador(
            String nombre,
            String descripcion,
            Identificador identificador
    ) {
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
}