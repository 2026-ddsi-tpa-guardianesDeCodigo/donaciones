package ar.edu.utn.dds.k3003.eventos;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Listener de entidades JPA (@EntityListeners). Los repositorios de este módulo son interfaces de
 * Spring Data, sin clase propia donde enganchar la publicación, así que se hace al nivel de JPA: un
 * save() que no cambia nada no dispara @PostUpdate, y los borrados (incluidos los de deleteAll)
 * llegan acá igual. Spring Boot le pide este listener al contexto, por eso puede ser un bean.
 */
@Component
public class PublicadorDeCambios {
    private final ApplicationEventPublisher eventos;

    public PublicadorDeCambios(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @PostPersist
    @PostUpdate
    public void alGuardar(Object entidad) {
        eventos.publishEvent(new CambioDeEntidad(entidad, false));
    }

    @PostRemove
    public void alBorrar(Object entidad) {
        eventos.publishEvent(new CambioDeEntidad(entidad, true));
    }
}
