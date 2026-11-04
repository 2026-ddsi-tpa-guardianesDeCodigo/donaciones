package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.model.Categoria;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.Producto;
import ar.edu.utn.dds.k3003.model.Subcategoria;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Arma el sobre y el snapshot de cada entidad del catálogo de eventos de Donaciones. */
@Component
public class ConstructorDeEventos {
    static final String ORIGEN = "donaciones";

    public Optional<EventoDominio> construir(Object entidad, boolean eliminada) {
        return switch (entidad) {
            case Donacion d -> Optional.of(donacion(d, eliminada));
            case Producto p -> Optional.of(producto(p, eliminada));
            case Categoria c -> Optional.of(categoria(c, eliminada));
            default -> Optional.empty();
        };
    }

    private EventoDominio donacion(Donacion d, boolean eliminada) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", String.valueOf(d.getId()));
        payload.put("donadorId", d.getDonadorID());
        payload.put("productoId", d.getProductoID() == null ? null : String.valueOf(d.getProductoID()));
        payload.put("depositoId", d.getDepositoID());
        payload.put("cantidad", d.getCantidad());
        payload.put("estado", d.getEstado() == null ? null : d.getEstado().name());
        // Las donaciones anteriores a E6 no tienen los timestamps: se cae al día guardado en `fecha`.
        Instant ingreso = d.getFechaIngreso() != null ? d.getFechaIngreso() : desdeFecha(d.getFecha());
        Instant cambio = d.getFechaUltimoCambio() != null ? d.getFechaUltimoCambio() : ingreso;
        payload.put("fechaIngreso", ingreso == null ? null : ingreso.toString());
        payload.put("fechaUltimoCambio", cambio == null ? null : cambio.toString());
        return sobre("DonacionActualizada", "donacion", d.getId(), d.getVersion(), eliminada, payload);
    }

    private EventoDominio producto(Producto p, boolean eliminada) {
        Categoria c = p.getCategoria();
        Subcategoria s = c == null ? null : c.getSubcategoria();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", String.valueOf(p.getId()));
        payload.put("nombre", p.getNombre());
        payload.put("categoriaId", c == null ? null : String.valueOf(c.getId()));
        // Derivado de la categoría: si la categoría cambia de subcategoría este valor queda viejo
        // hasta el próximo evento del producto. Consultas debe resolverlo con el snapshot de la
        // categoría, que sí se actualiza.
        payload.put("subcategoriaId", s == null ? null : String.valueOf(s.getId()));
        return sobre("ProductoActualizado", "producto", p.getId(), p.getVersion(), eliminada, payload);
    }

    private EventoDominio categoria(Categoria c, boolean eliminada) {
        Subcategoria s = c.getSubcategoria();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", String.valueOf(c.getId()));
        payload.put("nombre", c.getNombre());
        payload.put("subcategoriaId", s == null ? null : String.valueOf(s.getId()));
        payload.put("subcategoriaNombre", s == null ? null : s.getNombre());
        return sobre("CategoriaActualizada", "categoria", c.getId(), c.getVersion(), eliminada, payload);
    }

    private static Instant desdeFecha(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private EventoDominio sobre(String tipo, String entidad, Long id, Long version, boolean eliminada,
                                Map<String, Object> payload) {
        // Un borrado no incrementa @Version (la fila deja de existir), pero el consumidor solo aplica
        // versiones mayores a la que tiene: se publica version + 1 para que la baja no sea ignorada.
        long v = (version == null ? 0L : version) + (eliminada ? 1 : 0);
        return new EventoDominio(UUID.randomUUID().toString(), tipo, ORIGEN, entidad, String.valueOf(id), v,
                Instant.now().toString(), MDC.get("trace.id"), eliminada, payload);
    }
}
