package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.DonacionInvalidaException;
import ar.edu.utn.dds.k3003.exceptions.TransicionEstadoInvalidaException;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.Producto;
import ar.edu.utn.dds.k3003.repositories.CategoriaRepository;
import ar.edu.utn.dds.k3003.repositories.DonacionesRepository;
import ar.edu.utn.dds.k3003.repositories.IdentificadorRepository;
import ar.edu.utn.dds.k3003.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Regresión de bugs del core corregidos para la Entrega 5 (ver CONTEXTO_E5.md §5 y §8-A, F1/F3/A3):
 *  - F1: registrarDonacion le avisaba a Logística un id calculado (findAll().size()+1), no el
 *    id real que iba a asignar la base.
 *  - F3: registrarQuejaEnDonacion registraba la queja en Donadores (y pisaba la descripción de
 *    la donación) ANTES de validar que la donación estuviera ACEPTADA.
 *  - A3: PATCH /donaciones/estado no tenía ningún guard para volver a INGRESADA.
 *  - Orden: los listados salían en el orden físico de la tabla, y la misión Ascendentes lo usa.
 */
class DonacionesServiceTest {

    @Mock DonacionesRepository donacionesRepository;
    @Mock ProductoRepository productoRepository;
    @Mock IdentificadorRepository identificadorRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock DonadoresClient donadoresClient;
    @Mock LogisticaClient logisticaClient;

    DonacionesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DonacionesService(donacionesRepository, productoRepository,
                identificadorRepository, categoriaRepository, donadoresClient, logisticaClient, null);
    }

    /** Simula lo que hace JPA con @GeneratedValue(IDENTITY): asigna el id real al guardar. */
    private void stubGuardarConId(long id) {
        when(donacionesRepository.save(any(Donacion.class))).thenAnswer(inv -> {
            Donacion d = inv.getArgument(0);
            Field idField = Donacion.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(d, id);
            return d;
        });
    }

    private void stubProductoExiste() {
        when(productoRepository.findById(3L)).thenReturn(Optional.of(mock(Producto.class)));
    }

    private DonacionDTO dtoValido(Integer cantidad) {
        return new DonacionDTO(null, "7", "1", "una donación", 3L, cantidad, null);
    }

    // ---------------------------------------------------------------- F1: id real hacia Logística

    @Test
    void registrarDonacion_avisaALogisticaConElIdRealDeLaDonacion_noConUnContador() {
        stubGuardarConId(42L);
        stubProductoExiste();
        when(donadoresClient.puedeDonar("7")).thenReturn(true);

        service.registrarDonacion(dtoValido(5));

        verify(logisticaClient).gestionarDonacion(eq("1"), eq("42"), eq("3"), eq(5));
    }

    @Test
    void registrarDonacion_siLogisticaRechaza_revierteLaDonacionYaGuardada() {
        stubGuardarConId(42L);
        stubProductoExiste();
        when(donadoresClient.puedeDonar("7")).thenReturn(true);
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], null))
                .when(logisticaClient).gestionarDonacion(anyString(), anyString(), anyString(), any());

        assertThrows(HttpClientErrorException.class, () -> service.registrarDonacion(dtoValido(5)));

        verify(donacionesRepository).deleteById(42L);
    }

    @Test
    void registrarDonacion_conCantidadNula_rechazaAntesDeLlamarAOtroComponente() {
        assertThrows(DonacionInvalidaException.class, () -> service.registrarDonacion(dtoValido(null)));

        verifyNoInteractions(donadoresClient, logisticaClient, donacionesRepository);
    }

    // ---------------------------------------------------------------- F3: validar antes de efectos

    @Test
    void registrarQueja_siLaDonacionNoEstaAceptada_noRegistraNadaEnDonadores() {
        Donacion donacionIngresada = new Donacion(9L, "7", "1", "una donación", 3L, 5,
                EstadoDonacionEnum.INGRESADA, LocalDate.now());
        when(donacionesRepository.findById(9L)).thenReturn(Optional.of(donacionIngresada));

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> service.registrarQuejaEnDonacion(9L, "llegó roto"));

        verifyNoInteractions(donadoresClient);
        verify(donacionesRepository, never()).save(any());
    }

    @Test
    void registrarQueja_aceptada_registraLaQuejaYCambiaElEstado() {
        Donacion donacionAceptada = new Donacion(9L, "7", "1", "una donación", 3L, 5,
                EstadoDonacionEnum.ACEPTADA, LocalDate.now());
        when(donacionesRepository.findById(9L)).thenReturn(Optional.of(donacionAceptada));

        DonacionDTO resultado = service.registrarQuejaEnDonacion(9L, "llegó roto");

        verify(donadoresClient).agregarQueja(any());
        assertEquals(EstadoDonacionEnum.CONQUEJA, resultado.estado());
        // La descripción de la donación no se pisa con el texto de la queja.
        assertEquals("una donación", resultado.descripcion());
    }

    // ---------------------------------------------------------------- A3: guard →INGRESADA

    @Test
    void cambiarEstado_aIngresada_siempreRechazado() {
        Donacion donacionAceptada = new Donacion(9L, "7", "1", "una donación", 3L, 5,
                EstadoDonacionEnum.ACEPTADA, LocalDate.now());
        when(donacionesRepository.findById(9L)).thenReturn(Optional.of(donacionAceptada));

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> service.cambiarEstadoDeDonacion(9L, EstadoDonacionEnum.INGRESADA));

        verify(donacionesRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- Orden de ingreso

    /** Postgres trae último lo que se actualizó último: entregar al revés invierte la lista. */
    private void stubDonacionesDesordenadas() {
        when(donacionesRepository.findAll()).thenReturn(List.of(
                new Donacion(3L, "7", "1", "tercera", 3L, 3, EstadoDonacionEnum.ACEPTADA, LocalDate.of(2026, 11, 3)),
                new Donacion(1L, "7", "1", "primera", 3L, 1, EstadoDonacionEnum.ACEPTADA, LocalDate.of(2026, 11, 1)),
                new Donacion(2L, "7", "1", "segunda", 3L, 2, EstadoDonacionEnum.INGRESADA, LocalDate.of(2026, 11, 2))));
    }

    private static List<Long> ids(List<DonacionDTO> donaciones) {
        return donaciones.stream().map(DonacionDTO::id).toList();
    }

    @Test
    void buscarPorDonador_devuelveLasDonacionesEnOrdenDeIngreso() {
        stubDonacionesDesordenadas();

        assertEquals(List.of(1L, 2L, 3L), ids(service.buscarPorDonador(7L)));
    }

    @Test
    void buscarPorDonadorYFechaInicio_devuelveLasDonacionesEnOrdenDeIngreso() {
        stubDonacionesDesordenadas();

        assertEquals(List.of(1L, 2L, 3L), ids(service.buscarPorDonadorYFechaInicio("7", LocalDate.of(2026, 1, 1))));
    }

    @Test
    void listarDonaciones_devuelveLasDonacionesEnOrdenDeIngreso() {
        stubDonacionesDesordenadas();

        assertEquals(List.of(1L, 2L, 3L), ids(service.listarDonaciones()));
    }
}
