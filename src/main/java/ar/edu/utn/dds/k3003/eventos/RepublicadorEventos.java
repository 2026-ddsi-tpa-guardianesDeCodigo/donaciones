package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.repositories.CategoriaRepository;
import ar.edu.utn.dds.k3003.repositories.DonacionesRepository;
import ar.edu.utn.dds.k3003.repositories.ProductoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Reenvía el snapshot actual de todas las entidades del catálogo. Cubre el evento perdido entre el
 * commit y el publish y deja el modelo de lectura sincronizado antes de la demo.
 */
@Component
public class RepublicadorEventos {
    private final DonacionesRepository donaciones;
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ConstructorDeEventos constructor;
    private final PublicadorEventos publicador;

    public RepublicadorEventos(DonacionesRepository donaciones, ProductoRepository productos,
                               CategoriaRepository categorias, ConstructorDeEventos constructor,
                               PublicadorEventos publicador) {
        this.donaciones = donaciones;
        this.productos = productos;
        this.categorias = categorias;
        this.constructor = constructor;
        this.publicador = publicador;
    }

    /** @return cantidad de eventos que el broker aceptó. */
    public int republicarTodo() {
        List<Object> todas = new ArrayList<>();
        todas.addAll(categorias.findAll());
        todas.addAll(productos.findAll());
        todas.addAll(donaciones.findAll());
        int publicados = 0;
        for (Object e : todas) {
            if (constructor.construir(e, false).map(publicador::publicar).orElse(false)) {
                publicados++;
            }
        }
        return publicados;
    }
}
