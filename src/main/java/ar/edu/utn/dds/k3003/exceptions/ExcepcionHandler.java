package ar.edu.utn.dds.k3003.exceptions;

import ar.edu.utn.dds.k3003.exceptions.*;
import ar.edu.utn.dds.k3003.infra.logging.EventLogger;
import ar.edu.utn.dds.k3003.infra.logging.EventoLog;
import ar.edu.utn.dds.k3003.infra.logging.Outcome;
import ar.edu.utn.dds.k3003.repositories.DonacionesMetrics;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ExcepcionHandler {

    private static final EventLogger LOG = EventLogger.of(ExcepcionHandler.class);

    private final DonacionesMetrics metrics;

    public ExcepcionHandler(DonacionesMetrics metrics) {
        this.metrics = metrics;
    }


    // =========================
    // 404 - NO ENCONTRADO
    // =========================

    @ExceptionHandler({
            DonacionNoEncontradaException.class,
            ProductoNoEncontradoException.class,
            CategoriaNoEncontradaException.class,
            IdentificadorNoEncontradoException.class,
            DonadorNoEncontradoException.class
    })
    public ResponseEntity<String> handleNotFound(RuntimeException e) {

        metrics.incrementarError("not_found");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }


    // =========================
    // 400 - ERRORES DEL NEGOCIO
    // =========================

    @ExceptionHandler({
            DonacionInvalidaException.class,
            ProductoInvalidoException.class,
            CategoriaInvalidaException.class,
            IdentificadorInvalidoException.class,
            TransicionEstadoInvalidaException.class,
            DonadorYaExistenteException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {

        metrics.incrementarError("bad_request");

        LOG.evento(EventoLog.VALIDATION_FAILED, "Validación fallida")
           .dato("error.type", e.getClass().getSimpleName())
           .outcome(Outcome.FAILURE).warn().emitir();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }


    // Rechazos que ya se logearon en el servicio (donacion.rechazada / producto.validacion.fallida):
    // acá no se vuelve a logear el mismo hecho.
    @ExceptionHandler({NoPuedeDonarException.class, ProductoInvalidoSegunIdentificadorException.class})
    public ResponseEntity<String> handleRechazoYaLogeado(RuntimeException e) {

        metrics.incrementarError("bad_request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    // =========================
    // 409 - CONFLICTO (el recurso existe, pero borrarlo dejaría referencias colgando)
    // =========================

    @ExceptionHandler({ProductoEnUsoException.class, CategoriaEnUsoException.class,
            IdentificadorEnUsoException.class})
    public ResponseEntity<String> handleConflicto(RuntimeException e) {

        metrics.incrementarError("conflicto");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    // =========================
    // 400 - REQUEST MAL FORMADO
    // =========================

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<String> handleRequestInvalido(Exception e) {

        metrics.incrementarError("bad_request");

        // Solo el tipo de error: el mensaje de Jackson incluye fragmentos del body recibido,
        // que pueden ser datos personales (logging-spec_v1.md §7).
        LOG.evento(EventoLog.VALIDATION_FAILED, "Validación fallida")
           .dato("error.type", e.getClass().getSimpleName())
           .outcome(Outcome.FAILURE).warn().emitir();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Request invalido");
    }
    // =========================
    // ERROR DE OTRO MICROSERVICIO
    // =========================

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleMicroservicio(
            RestClientResponseException e) {

        int status = e.getStatusCode().value();

        if (status == 400) {
            metrics.incrementarError("bad_request");
        } else if (status == 404) {
            metrics.incrementarError("not_found");
        } else {
            metrics.incrementarError("microservicio");
        }

        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getResponseBodyAsString());
    }


    // =========================
    // 404 / 405 / 415 - ERRORES DEL FRAMEWORK
    // =========================

    // Ruta inexistente, método o media type no soportado: el access log (WARN) ya deja constancia.
    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<String> handleErrorDelFramework(Exception e) {

        metrics.incrementarError("bad_request");

        ErrorResponse respuesta = (ErrorResponse) e;
        return ResponseEntity
                .status(respuesta.getStatusCode())
                .body(respuesta.getBody().getDetail());
    }

    // =========================
    // 500 - ERROR INTERNO
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        metrics.incrementarError("internal_error");

        LOG.evento(EventoLog.UNHANDLED_EXCEPTION, "Excepción no manejada")
           .error(e).emitir();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}



