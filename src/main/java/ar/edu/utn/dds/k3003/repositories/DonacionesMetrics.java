package ar.edu.utn.dds.k3003.repositories;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DonacionesMetrics {

    private final Counter donacionesRegistradas;
    private final Counter productosRegistrados;
    private final Counter identificadoresRegistrados;
    private final Counter categoriasRegistradas;
    private final Counter errores;
    private final Counter donacionesAceptadas;
    private final Counter donacionesConQueja;
    private final Counter consultasPorDonador;
    private final Counter consultasProductoPorId;
    private final Counter cambiosEstado;
    private final Counter quejasRegistradas;
    private final Counter enviosALogistica;

    public DonacionesMetrics(MeterRegistry registry) {
        this.donacionesRegistradas = Counter.builder("donaciones.registradas")
                .description("Cantidad de donaciones registradas")
                .register(registry);

        this.productosRegistrados = Counter.builder("productos.registrados")
                .description("Cantidad de productos registrados")
                .register(registry);

        this.identificadoresRegistrados = Counter.builder("identificadores.registrados")
                .description("Cantidad de identificadores registrados")
                .register(registry);

        this.categoriasRegistradas = Counter.builder("categorias.registradas")
                .description("Cantidad de categorias registradas")
                .register(registry);

        this.errores = Counter.builder("donaciones.errores")
                .description("Cantidad de errores en el modulo de donaciones")
                .register(registry);

        this.donacionesAceptadas = Counter.builder("donaciones.aceptadas")
                .description("Cantidad de donaciones aceptadas")
                .register(registry);

        this.donacionesConQueja = Counter.builder("donaciones.con_queja")
                .description("Cantidad de donaciones con queja")
                .register(registry);

        this.consultasPorDonador = Counter.builder("donaciones.consultas_por_donador")
                .description("Consultas de donaciones por donador")
                .register(registry);

        this.consultasProductoPorId = Counter.builder("productos.consultas_por_id")
                .description("Consultas de productos por ID")
                .register(registry);

        this.cambiosEstado = Counter.builder("donaciones.cambios_estado")
                .description("Cambios de estado de donaciones")
                .register(registry);

        this.quejasRegistradas = Counter.builder("donaciones.quejas")
                .description("Quejas registradas")
                .register(registry);

        this.enviosALogistica = Counter.builder("donaciones.envios_logistica")
                .description("Donaciones enviadas a logistica")
                .register(registry);
    }

    public void incrementarDonacionesRegistradas() {
        donacionesRegistradas.increment();
    }

    public void incrementarProductosRegistrados() {
        productosRegistrados.increment();
    }

    public void incrementarIdentificadoresRegistrados() {
        identificadoresRegistrados.increment();
    }

    public void incrementarCategoriasRegistradas() {
        categoriasRegistradas.increment();
    }

    public void incrementarErrores() {
        errores.increment();
    }

    public void incrementarDonacionesAceptadas() {
        donacionesAceptadas.increment();
    }

    public void incrementarDonacionesConQueja() {
        donacionesConQueja.increment();
    }

    public void incrementarConsultasPorDonador() {
        consultasPorDonador.increment();
    }

    public void incrementarConsultasProductoPorId() {
        consultasProductoPorId.increment();
    }

    public void incrementarCambiosEstado() {
        cambiosEstado.increment();
    }

    public void incrementarQuejasRegistradas() {
        quejasRegistradas.increment();
    }

    public void incrementarEnviosALogistica() {
        enviosALogistica.increment();
    }
}