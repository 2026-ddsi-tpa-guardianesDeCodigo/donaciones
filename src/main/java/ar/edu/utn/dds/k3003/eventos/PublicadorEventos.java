package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.infra.logging.EventLogger;
import ar.edu.utn.dds.k3003.infra.logging.EventoLog;
import ar.edu.utn.dds.k3003.infra.logging.LogFields;
import ar.edu.utn.dds.k3003.infra.logging.Outcome;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica los eventos del modelo de lectura. Si el broker no responde el comando original NO falla:
 * se loguea DEGRADED y se cuenta en donaciones.eventos.publicacion_fallida; el modelo de lectura es
 * eventualmente consistente y se repara con POST /admin/republicar-eventos.
 */
@Component
public class PublicadorEventos {
    private static final EventLogger LOG = EventLogger.of(PublicadorEventos.class);

    private final RabbitTemplate rabbitTemplate;
    private final ConstructorDeEventos constructor;
    private final MeterRegistry registry;
    private final String exchange;
    private final boolean habilitado;

    public PublicadorEventos(RabbitTemplate rabbitTemplate, ConstructorDeEventos constructor,
                             MeterRegistry registry,
                             @Value("${eventos.exchange:donatrack.eventos}") String exchange,
                             @Value("${eventos.publicacion.habilitada:true}") boolean habilitado) {
        this.rabbitTemplate = rabbitTemplate;
        this.constructor = constructor;
        this.registry = registry;
        this.exchange = exchange;
        this.habilitado = habilitado;
    }

    // fallbackExecution: fuera de una transacción el evento se entrega igual (el cambio ya commiteó).
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarEntidad(CambioDeEntidad cambio) {
        constructor.construir(cambio.entidad(), cambio.eliminada()).ifPresent(this::publicar);
    }

    /** @return true si el broker aceptó el mensaje. */
    public boolean publicar(EventoDominio evento) {
        if (!habilitado) {
            return false;
        }
        try {
            rabbitTemplate.convertAndSend(exchange, evento.routingKey(), evento, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                if (evento.correlationId() != null) {
                    m.getMessageProperties().setHeader("X-Correlation-Id", evento.correlationId());
                }
                return m;
            });
            registry.counter("donaciones.eventos.publicados", "tipo", evento.tipo()).increment();
            LOG.evento(EventoLog.EVENTO_PUBLICADO, "Evento publicado")
                    .dato(LogFields.MSG_SYSTEM, "rabbitmq")
                    .dato(LogFields.MSG_DESTINATION, exchange)
                    .dato(LogFields.MSG_OPERATION, "publish")
                    .dato("evento.tipo", evento.tipo())
                    .id("evento.entidad_id", evento.entidadId())
                    .dato("evento.version", evento.version())
                    .emitir();
            return true;
        } catch (RuntimeException e) {
            registry.counter("donaciones.eventos.publicacion_fallida", "tipo", evento.tipo()).increment();
            LOG.evento(EventoLog.EVENTO_PUBLICACION_FALLIDA, "No se pudo publicar el evento; el modelo de lectura queda atrasado")
                    .dato(LogFields.MSG_SYSTEM, "rabbitmq")
                    .dato(LogFields.MSG_DESTINATION, exchange)
                    .dato("evento.tipo", evento.tipo())
                    .id("evento.entidad_id", evento.entidadId())
                    .dato("error.type", e.getClass().getSimpleName())
                    .outcome(Outcome.DEGRADED).warn().emitir();
            return false;
        }
    }
}
