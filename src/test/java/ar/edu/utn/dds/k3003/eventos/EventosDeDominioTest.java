package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.model.Categoria;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.Producto;
import ar.edu.utn.dds.k3003.model.Subcategoria;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventosDeDominioTest {

    private final ConstructorDeEventos constructor = new ConstructorDeEventos();

    private Donacion donacion() {
        return new Donacion(57L, "12", "3", "fideos", 9L, 30, EstadoDonacionEnum.INGRESADA, LocalDate.now());
    }

    @Test
    @DisplayName("el snapshot de una donación lleva estado, depósito y los dos timestamps")
    void snapshotDeDonacion() {
        EventoDominio e = constructor.construir(donacion(), false).orElseThrow();

        assertEquals("DonacionActualizada", e.tipo());
        assertEquals("donaciones.donacion.actualizada", e.routingKey());
        assertEquals("57", e.entidadId());
        assertEquals("INGRESADA", e.payload().get("estado"));
        assertEquals("3", e.payload().get("depositoId"));
        assertNotNull(e.payload().get("fechaIngreso"));
        assertNotNull(e.payload().get("fechaUltimoCambio"));
    }

    @Test
    @DisplayName("cambiar el estado actualiza fechaUltimoCambio pero no fechaIngreso")
    void cambioDeEstadoMueveSoloElUltimoCambio() throws InterruptedException {
        Donacion d = donacion();
        var ingreso = d.getFechaIngreso();
        Thread.sleep(5);

        d.setEstado(EstadoDonacionEnum.ACEPTADA);

        assertEquals(ingreso, d.getFechaIngreso());
        assertTrue(d.getFechaUltimoCambio().isAfter(ingreso));
    }

    @Test
    @DisplayName("una donación anterior a E6 (sin timestamps) cae al día guardado en fecha")
    void donacionLegacySinTimestamps() {
        Donacion legacy = donacion();
        ReflectionTestUtils.setField(legacy, "fechaIngreso", null);
        ReflectionTestUtils.setField(legacy, "fechaUltimoCambio", null);
        ReflectionTestUtils.setField(legacy, "fecha", LocalDate.of(2026, 10, 1));

        EventoDominio e = constructor.construir(legacy, false).orElseThrow();

        assertEquals("2026-10-01T00:00:00Z", e.payload().get("fechaIngreso"));
        assertEquals("2026-10-01T00:00:00Z", e.payload().get("fechaUltimoCambio"));
    }

    @Test
    @DisplayName("el producto y la categoría exponen la subcategoría")
    void subcategoriaEnProductoYCategoria() {
        Categoria cat = new Categoria(2L, "Alimentos", "desc", new Subcategoria(5L, "Secos", null));
        Producto prod = new Producto(9L, "Fideos", "d", cat, null);

        EventoDominio ep = constructor.construir(prod, false).orElseThrow();
        EventoDominio ec = constructor.construir(cat, false).orElseThrow();

        assertEquals("2", ep.payload().get("categoriaId"));
        assertEquals("5", ep.payload().get("subcategoriaId"));
        assertEquals("Secos", ec.payload().get("subcategoriaNombre"));
        assertEquals("donaciones.categoria.actualizada", ec.routingKey());
    }

    @Test
    @DisplayName("un borrado se publica con version + 1 para no ser ignorado por el consumidor")
    void borradoSubeLaVersion() {
        EventoDominio e = constructor.construir(donacion(), true).orElseThrow();

        assertTrue(e.eliminado());
        assertEquals("donaciones.donacion.eliminada", e.routingKey());
        assertEquals(1L, e.version());
    }

    @Test
    @DisplayName("si el broker no responde, publicar no lanza: cuenta la falla y sigue")
    void brokerCaidoNoRompeElComando() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        doThrow(new AmqpConnectException(new RuntimeException("caido")))
                .when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        PublicadorEventos publicador = new PublicadorEventos(rabbit, constructor, registry, "donatrack.eventos", true);

        assertDoesNotThrow(() -> publicador.alCambiarEntidad(new CambioDeEntidad(donacion(), false)));
        assertEquals(1.0, registry.counter("donaciones.eventos.publicacion_fallida", "tipo", "DonacionActualizada").count());
    }

    @Test
    @DisplayName("publicación deshabilitada: no toca el broker")
    void deshabilitado() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        PublicadorEventos publicador = new PublicadorEventos(rabbit, constructor, new SimpleMeterRegistry(), "x", false);

        publicador.alCambiarEntidad(new CambioDeEntidad(donacion(), false));

        verifyNoInteractions(rabbit);
    }
}
