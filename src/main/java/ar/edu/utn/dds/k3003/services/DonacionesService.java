package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.*;
import ar.edu.utn.dds.k3003.metrics.DonacionesMetrics;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.*;
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
            throw new DonacionInvalidaException("Donacion invalida");
        }

        if (dto.donadorID() == null || dto.donadorID().isBlank()) {
            throw new DonacionInvalidaException("Donador invalido");
        }

        if (dto.depositoID() == null || dto.depositoID().isBlank()) {
            throw new DonacionInvalidaException("Deposito invalido");
        }

        if (dto.productoID() == null || dto.productoID().isBlank()) {
            throw new DonacionInvalidaException("Producto invalido");
        }

        if (dto.cantidad() <= 0) {
            throw new DonacionInvalidaException("La cantidad debe ser mayor a cero");
        }

        String nuevoId = String.valueOf(donacionesRepository.findAll().size() + 1);

        donadoresClient.buscarDonadorPorID(dto.donadorID());

        Boolean puedeDonar = donadoresClient.puedeDonar(dto.donadorID());
        if (puedeDonar == null || !puedeDonar) {
            throw new NoPuedeDonarException("No puede donar");
        }

        buscarProductoInternoPorID(dto.productoID());

        logisticaClient.gestionarDonacion(
                dto.depositoID(),
                nuevoId,
                dto.productoID(),
                dto.cantidad()
        );

        if (metrics != null) {
            metrics.incrementarEnviosALogistica();
        }

        Donacion donacion = new Donacion(
                nuevoId,
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
        if (id == null || id.isBlank()) {
            throw new DonacionInvalidaException("Donacion invalida");
        }

        Donacion donacion = donacionesRepository.findById(id)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

        return donacionMapper.toDonacionDTO(donacion);
    }

    public DonacionDTO cambiarEstadoDeDonacion(String donacionID, EstadoDonacionEnum estado) {
        if (donacionID == null || donacionID.isBlank()) {
            throw new DonacionInvalidaException("Donacion invalida");
        }

        if (estado == null) {
            throw new DonacionInvalidaException("Estado invalido");
        }

        Donacion donacion = donacionesRepository.findById(donacionID)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

        EstadoDonacionEnum estadoActual = donacion.getEstado();

        if (estado == EstadoDonacionEnum.ACEPTADA && estadoActual != EstadoDonacionEnum.INGRESADA) {
            throw new TransicionEstadoInvalidaException(
                    "Transicion invalida: para aceptar, la donacion debe estar INGRESADA"
            );
        }

        if (estado == EstadoDonacionEnum.CONQUEJA && estadoActual != EstadoDonacionEnum.ACEPTADA) {
            throw new TransicionEstadoInvalidaException(
                    "Transicion invalida: para registrar queja, la donacion debe estar ACEPTADA"
            );
        }

        donacion.setEstado(estado);
        donacionesRepository.save(donacion);

        if (metrics != null) {
            metrics.incrementarCambiosEstado();

            if (estado == EstadoDonacionEnum.ACEPTADA) {
                metrics.incrementarDonacionesAceptadas();
            }

            if (estado == EstadoDonacionEnum.CONQUEJA) {
                metrics.incrementarDonacionesConQueja();
            }
        }

        return donacionMapper.toDonacionDTO(donacion);
    }

    public List<DonacionDTO> buscarPorDonadorYFechaInicio(String donadorID, LocalDate fecha) {
        if (donadorID == null || donadorID.isBlank()) {
            throw new DonacionInvalidaException("Donador invalido");
        }

        if (fecha == null) {
            throw new DonacionInvalidaException("Fecha invalida");
        }

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
            throw new DonacionInvalidaException("Donacion invalida");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new DonacionInvalidaException("Descripcion invalida");
        }

        Donacion donacion = donacionesRepository.findById(donacionID)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

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

        if (metrics != null) {
            metrics.incrementarQuejasRegistradas();
        }

        return cambiarEstadoDeDonacion(donacionID, EstadoDonacionEnum.CONQUEJA);
    }

    public List<DonacionDTO> buscarPorDonador(String donadorID) {
        if (donadorID == null || donadorID.isBlank()) {
            throw new DonacionInvalidaException("Donador invalido");
        }

        return donacionesRepository.findAll().stream()
                .filter(d -> d.getDonadorID() != null)
                .filter(d -> d.getDonadorID().trim().equals(donadorID.trim()))
                .map(donacionMapper::toDonacionDTO)
                .toList();
    }

    public ProductoDTO agregarProducto(ProductoDTO dto) {
        if (dto == null) {
            throw new ProductoInvalidoException("Producto invalido");
        }

        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new ProductoInvalidoException("Nombre de producto invalido");
        }

        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new ProductoInvalidoException("Descripcion de producto invalida");
        }

        if (dto.categoriaID() == null || dto.categoriaID().isBlank()) {
            throw new ProductoInvalidoException("Categoria invalida");
        }

        if (dto.identificadorID() == null || dto.identificadorID().isBlank()) {
            throw new ProductoInvalidoException("Identificador invalido");
        }

        String nuevoId = String.valueOf(productoRepository.findAll().size() + 1);

        Categoria categoria = categoriaRepository.findById(dto.categoriaID())
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoria no encontrada"));

        Identificador identificador = identificadorRepository.findById(dto.identificadorID())
                .orElseThrow(() -> new IdentificadorNoEncontradoException("Identificador no encontrado"));

        if (!esValidoSegunIdentificador(dto.nombre(), dto.descripcion(), identificador)) {
            throw new ProductoInvalidoException("Producto invalido segun identificador");
        }

        Producto producto = new Producto(
                nuevoId,
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
        if (productoID == null || productoID.isBlank()) {
            throw new ProductoInvalidoException("Producto invalido");
        }

        if (metrics != null) {
            metrics.incrementarConsultasProductoPorId();
        }

        Producto producto = productoRepository.findById(productoID)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        return productoDataMapper.toDTO(producto);
    }

    private Producto buscarProductoInternoPorID(String productoID) {
        if (productoID == null || productoID.isBlank()) {
            throw new ProductoInvalidoException("Producto invalido");
        }

        return productoRepository.findById(productoID)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
    }

    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(productoDataMapper::toDTO)
                .toList();
    }

    public CategoriaDTO agregarCategoria(CategoriaDTO dto) {
        if (dto == null) {
            throw new CategoriaInvalidaException("Categoria invalida");
        }

        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new CategoriaInvalidaException("Nombre de categoria invalido");
        }

        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new CategoriaInvalidaException("Descripcion de categoria invalida");
        }

        String nuevoId = String.valueOf(categoriaRepository.findAll().size() + 1);

        Categoria categoria = new Categoria(
                nuevoId,
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
        if (categoriaID == null || categoriaID.isBlank()) {
            throw new CategoriaInvalidaException("Categoria invalida");
        }

        Categoria categoria = categoriaRepository.findById(categoriaID)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoria no encontrada"));

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
            throw new IdentificadorInvalidoException("Identificador invalido");
        }

        if (dto.tipo() == null) {
            throw new IdentificadorInvalidoException("Tipo de identificador invalido");
        }

        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new IdentificadorInvalidoException("Descripcion de identificador invalida");
        }

        if (dto.id() != null && identificadorRepository.findById(dto.id()).isPresent()) {
            throw new IdentificadorInvalidoException("El identificador ya existe");
        }

        String nuevoId = String.valueOf(identificadorRepository.findAll().size() + 1);

        Identificador identificador = new Identificador(
                nuevoId,
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
        if (identificadorID == null || identificadorID.isBlank()) {
            throw new IdentificadorInvalidoException("Identificador invalido");
        }

        Identificador identificador = identificadorRepository.findById(identificadorID)
                .orElseThrow(() -> new IdentificadorNoEncontradoException("Identificador no encontrado"));

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