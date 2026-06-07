package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.metrics.DonacionesMetrics;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.repositories.DonacionMapper;
import ar.edu.utn.dds.k3003.repositories.DonacionesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonacionesService {

    private final DonacionesRepository donacionesRepository;
    private final DonadoresClient donadoresClient;
    private final LogisticaClient logisticaClient;
    private final DonacionesMetrics metrics;

    private final DonacionMapper donacionMapper = new DonacionMapper();

    public DonacionesService(
            DonacionesRepository donacionesRepository,
            DonadoresClient donadoresClient,
            LogisticaClient logisticaClient,
            DonacionesMetrics metrics
    ) {
        this.donacionesRepository = donacionesRepository;
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

        if (estado == EstadoDonacionEnum.ACEPTADA
                && estadoActual != EstadoDonacionEnum.INGRESADA) {
            throw new RuntimeException("Transicion invalida: para aceptar, la donacion debe estar INGRESADA");
        }

        if (estado == EstadoDonacionEnum.CONQUEJA
                && estadoActual != EstadoDonacionEnum.ACEPTADA) {
            throw new RuntimeException("Transicion invalida: para registrar queja, la donacion debe estar ACEPTADA");
        }

        donacion.setEstado(estado);
        donacionesRepository.save(donacion);

        return donacionMapper.toDonacionDTO(donacion);
    }

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

    public List<DonacionDTO> listarDonaciones() {
        return donacionesRepository.findAll().stream()
                .map(donacionMapper::toDonacionDTO)
                .toList();
    }

    public void limpiarBase() {
        donacionesRepository.deleteAll();
    }
}