package ar.edu.utn.dds.k3003.exceptions;

import ar.edu.utn.dds.k3003.exceptions.*;
import ar.edu.utn.dds.k3003.repositories.DonacionesMetrics;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ExcepcionHandler {

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
            NoPuedeDonarException.class,
            DonadorYaExistenteException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {

        metrics.incrementarError("bad_request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
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
    // 500 - ERROR INTERNO
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        metrics.incrementarError("internal_error");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}



