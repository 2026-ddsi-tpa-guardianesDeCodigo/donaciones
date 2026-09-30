package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.*;
import ar.edu.utn.dds.k3003.infra.logging.EventLogger;
import ar.edu.utn.dds.k3003.infra.logging.EventoLog;
import ar.edu.utn.dds.k3003.infra.logging.LogFields;
import ar.edu.utn.dds.k3003.infra.logging.Outcome;
import ar.edu.utn.dds.k3003.repositories.DonacionesMetrics;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonacionesService {

    private static final EventLogger LOG = EventLogger.of(DonacionesService.class);

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

        if (dto.productoID() == null || dto.productoID() <= 0) {
            throw new DonacionInvalidaException("Producto invalido");
        }

        if (dto.cantidad() == null || dto.cantidad() <= 0) {
            throw new DonacionInvalidaException("La cantidad debe ser mayor a cero");
        }

        try {
            donadoresClient.buscarDonadorPorID(dto.donadorID());
        } catch (HttpClientErrorException.NotFound e) {
            // El WARN de la llamada ya lo emitió el interceptor; acá el evento de negocio.
            rechazarDonacion("no_existe", dto);
            throw e;
        }

        Boolean puedeDonar = donadoresClient.puedeDonar(dto.donadorID());
        if (puedeDonar == null || !puedeDonar) {
            rechazarDonacion("no_puede_donar", dto);
            throw new NoPuedeDonarException("No puede donar");
        }

        try {
            buscarProductoInternoPorID(dto.productoID());
        } catch (ProductoNoEncontradoException e) {
            rechazarDonacion("producto_no_existe", dto);
            throw e;
        }

        // Se persiste ANTES de avisarle a Logística, para poder mandarle el id real. Antes se
        // usaba nuevoId = findAll().size()+1, calculado sin guardar nada: casi nunca coincidía
        // con el id real que iba a asignar la base (las secuencias de Postgres tampoco se
        // reinician con /debug/reset), así que reportarEntrega (PATCH /donaciones/estado) nunca
        // encontraba la donación y quedaba INGRESADA para siempre, o peor, pisaba otra.
        Donacion donacion = new Donacion(
                null,
                dto.donadorID(),
                dto.depositoID(),
                dto.descripcion(),
                dto.productoID(),
                dto.cantidad(),
                EstadoDonacionEnum.INGRESADA,
                LocalDate.now()
        );
        donacionesRepository.save(donacion);

        try {
            logisticaClient.gestionarDonacion(
                    dto.depositoID(),
                    String.valueOf(donacion.getId()),
                    String.valueOf(dto.productoID()),
                    dto.cantidad()
            );
        } catch (HttpClientErrorException e) {
            // Logística rechazó la donación (depósito lleno, cantidad inválida, depósito
            // inexistente...): se revierte lo persistido para no dejar una donación INGRESADA
            // que Logística nunca va a procesar.
            donacionesRepository.deleteById(donacion.getId());
            rechazarDonacion("logistica_rechazo", dto);
            throw e;
        }

        if (metrics != null) {
            metrics.incrementarEnviosALogistica();
            metrics.incrementarDonacionesRegistradas();
        }

        LOG.evento(EventoLog.DONACION_REGISTRADA, "Donación registrada")
                .id(LogFields.DONACION, donacion.getId())
                .id(LogFields.DONADOR, dto.donadorID())
                .id(LogFields.DEPOSITO, dto.depositoID())
                .id(LogFields.PRODUCTO, dto.productoID())
                .dato(LogFields.CANTIDAD, dto.cantidad())
                .emitir();

        return donacionMapper.toDonacionDTO(donacion);
    }

    private void rechazarDonacion(String motivo, DonacionDTO dto) {
        LOG.evento(EventoLog.DONACION_RECHAZADA, "Donación rechazada")
                .id(LogFields.DONADOR, dto.donadorID())
                .id(LogFields.PRODUCTO, dto.productoID())
                .dato(LogFields.MOTIVO, motivo)
                .outcome(Outcome.FAILURE).warn().emitir();
    }

    public DonacionDTO buscarDonacionPorID(Long id) {
        if (id == null ) {
            throw new DonacionInvalidaException("Donacion invalida");
        }

        Donacion donacion = donacionesRepository.findById(id)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

        return donacionMapper.toDonacionDTO(donacion);
    }

    public DonacionDTO cambiarEstadoDeDonacion(Long donacionID, EstadoDonacionEnum estado) {
        if (donacionID == null ) {
            throw new DonacionInvalidaException("Donacion invalida");
        }

        if (estado == null) {
            throw new DonacionInvalidaException("Estado invalido");
        }

        Donacion donacion = donacionesRepository.findById(donacionID)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

        EstadoDonacionEnum estadoActual = donacion.getEstado();
        validarTransicion(estadoActual, estado);

        donacion.setEstado(estado);
        donacionesRepository.save(donacion);

        LOG.evento(EventoLog.DONACION_ESTADO_CAMBIADO, "Estado de donación cambiado")
                .id(LogFields.DONACION, donacionID)
                .dato(LogFields.EST_ANT, estadoActual)
                .dato(LogFields.EST_NUE, estado)
                .emitir();

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

    /** Compartida entre cambiarEstadoDeDonacion y registrarQuejaEnDonacion (validar antes de tocar Donadores). */
    private void validarTransicion(EstadoDonacionEnum actual, EstadoDonacionEnum nuevo) {
        // La máquina de estados es lineal (INGRESADA → ACEPTADA → CONQUEJA, sin vuelta atrás):
        // INGRESADA solo se asigna al crear la donación (registrarDonacion), nunca via este
        // endpoint. Antes no había ningún guard para este caso: cualquier estado podía volver a
        // INGRESADA con un PATCH /donaciones/estado directo.
        if (nuevo == EstadoDonacionEnum.INGRESADA) {
            throw new TransicionEstadoInvalidaException(
                    "Transicion invalida: no se puede volver a INGRESADA una vez creada la donacion"
            );
        }

        if (nuevo == EstadoDonacionEnum.ACEPTADA && actual != EstadoDonacionEnum.INGRESADA) {
            throw new TransicionEstadoInvalidaException(
                    "Transicion invalida: para aceptar, la donacion debe estar INGRESADA"
            );
        }

        if (nuevo == EstadoDonacionEnum.CONQUEJA && actual != EstadoDonacionEnum.ACEPTADA) {
            throw new TransicionEstadoInvalidaException(
                    "Transicion invalida: para registrar queja, la donacion debe estar ACEPTADA"
            );
        }
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

    public DonacionDTO registrarQuejaEnDonacion(Long donacionID, String descripcion) {
        if (donacionID == null ) {
            throw new DonacionInvalidaException("Donacion invalida");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new DonacionInvalidaException("Descripcion invalida");
        }

        Donacion donacion = donacionesRepository.findById(donacionID)
                .orElseThrow(() -> new DonacionNoEncontradaException("Donacion no encontrada"));

        // Se valida ANTES de registrar la queja en Donadores (efecto con otro componente) y de
        // tocar la donación acá. Antes esto se validaba recién en cambiarEstadoDeDonacion, al
        // final: si la donación no estaba ACEPTADA, la queja ya había quedado creada en Donadores
        // igual, sin forma de deshacerla, y encima se pisaba la descripción original de la
        // donación con el texto de la queja antes de fallar.
        validarTransicion(donacion.getEstado(), EstadoDonacionEnum.CONQUEJA);

        QuejaDTO queja = new QuejaDTO(
                null,
                donacionID,
                donacion.getDonadorID(),
                null,
                descripcion
        );

        donadoresClient.agregarQueja(queja);

        if (metrics != null) {
            metrics.incrementarQuejasRegistradas();
        }

        LOG.evento(EventoLog.DONACION_QUEJA_REGISTRADA, "Queja registrada en la donación")
                .id(LogFields.DONACION, donacionID)
                .emitir();

        return cambiarEstadoDeDonacion(donacionID, EstadoDonacionEnum.CONQUEJA);
    }

    public List<DonacionDTO> buscarPorDonador(Long donadorID) {

        if (donadorID == null) {
            throw new DonacionInvalidaException("Donador invalido");
        }

        // Primero verifico que el donador exista
        donadoresClient.buscarDonadorPorID(String.valueOf(donadorID));

        return donacionesRepository.findAll().stream()
                .filter(d -> d.getDonadorID() != null)
                .filter(d -> d.getDonadorID().equals(String.valueOf(donadorID)))
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

        if (dto.categoriaID() == null || dto.categoriaID() <= 0) {
            throw new ProductoInvalidoException("Categoria invalida");
        }

        if (dto.identificadorID() == null || dto.identificadorID() <= 0) {
            throw new ProductoInvalidoException("Identificador invalido");
        }

        String nuevoId = String.valueOf(productoRepository.findAll().size() + 1);

        Categoria categoria = categoriaRepository.findById(dto.categoriaID())
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoria no encontrada"));

        Identificador identificador = identificadorRepository.findById(dto.identificadorID())
                .orElseThrow(() -> new IdentificadorNoEncontradoException("Identificador no encontrado"));

        if (!esValidoSegunIdentificador(dto.nombre(), dto.descripcion(), identificador)) {
            LOG.evento(EventoLog.PRODUCTO_VALIDACION_FALLIDA, "Producto rechazado por la regla de su identificador")
                    .id(LogFields.IDENTIFICADOR, identificador.getId())
                    .dato(LogFields.MOTIVO, identificador.getTipo() == TipoIdentificador.CODIGO_BARRAS
                            ? "barras_descripcion_corta" : "qr_nombre_impar")
                    .outcome(Outcome.FAILURE).warn().emitir();
            throw new ProductoInvalidoSegunIdentificadorException("Producto invalido segun identificador");
        }

        Producto producto = new Producto(
                null,
                dto.nombre(),
                dto.descripcion(),
                categoria,
                identificador
        );

        productoRepository.save(producto);

        if (metrics != null) {
            metrics.incrementarProductosRegistrados();
        }

        LOG.evento(EventoLog.PRODUCTO_CREADO, "Producto creado")
                .id(LogFields.PRODUCTO, producto.getId())
                .dato("tipo_identificador", identificador.getTipo())
                .emitir();

        return productoDataMapper.toDTO(producto);
    }

    public ProductoDTO buscarProductoPorID(Long productoID) {
        if (productoID == null ) {
            throw new ProductoInvalidoException("Producto invalido");
        }

        if (metrics != null) {
            metrics.incrementarConsultasProductoPorId();
        }

        Producto producto = productoRepository.findById(productoID)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        return productoDataMapper.toDTO(producto);
    }

    private Producto buscarProductoInternoPorID(Long productoID) {
        if (productoID == null ) {
            throw new ProductoInvalidoException("Producto invalido");
        }

        return productoRepository.findById(Long.valueOf(productoID))
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

        Categoria categoria = new Categoria(
                null,
                dto.nombre(),
                dto.descripcion(),
                null
        );

        Categoria guardada = categoriaRepository.save(categoria);

        if (metrics != null) {
            metrics.incrementarCategoriasRegistradas();
        }

        LOG.evento(EventoLog.CATEGORIA_CREADA, "Categoría creada")
                .id(LogFields.CATEGORIA, guardada.getId())
                .emitir();

        return new CategoriaDTO(
                guardada.getId(),
                guardada.getNombre(),
                guardada.getDescripcion(),
                null
        );
    }

    public CategoriaDTO buscarCategoriaPorID(Long categoriaID) {
        if (categoriaID == null ) {
            throw new CategoriaInvalidaException("Categoria invalida");
        }

        Categoria categoria = categoriaRepository.findById(categoriaID)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoria no encontrada"));

        String subcategoriaID = String.valueOf(categoria.getSubcategoria() != null
                ? categoria.getSubcategoria().getId()
                : null);

        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                null
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

        String nuevoId = String.valueOf(identificadorRepository.findAll().size() + 1);

        Identificador identificador = new Identificador(
                null,
                dto.tipo() == TipoIdentificadorEnum.QR
                        ? TipoIdentificador.CODIGO_QR
                        : TipoIdentificador.CODIGO_BARRAS,
                dto.descripcion()
        );

        identificadorRepository.save(identificador);

        if (metrics != null) {
            metrics.incrementarIdentificadoresRegistrados();
        }

        LOG.evento(EventoLog.IDENTIFICADOR_CREADO, "Identificador creado")
                .id(LogFields.IDENTIFICADOR, identificador.getId())
                .emitir();

        return buscarIdentificadorPorID(identificador.getId());
    }

    public IdentificadorDTO buscarIdentificadorPorID(Long identificadorID) {
        if (identificadorID == null || identificadorID <= 0) {
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

        LOG.evento(EventoLog.DEBUG_RESET_EJECUTADO, "Base de datos limpiada")
                .warn().emitir();
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