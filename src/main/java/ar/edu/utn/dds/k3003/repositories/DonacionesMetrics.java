package ar.edu.utn.dds.k3003.metrics;

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
}