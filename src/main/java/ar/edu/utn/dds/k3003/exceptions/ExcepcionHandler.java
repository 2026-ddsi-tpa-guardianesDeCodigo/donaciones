package ar.edu.utn.dds.k3003.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExcepcionHandler {

    @ExceptionHandler({
            DonacionNoEncontradaException.class,
            ProductoNoEncontradoException.class,
            CategoriaNoEncontradaException.class,
            IdentificadorNoEncontradoException.class,
            DonadorNoEncontradoException.class
    })
    public ResponseEntity<String> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}