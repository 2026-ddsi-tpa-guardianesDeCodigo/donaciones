package ar.edu.utn.dds.k3003.exceptions;

public class IdentificadorNoEncontradoException extends RuntimeException {
    public IdentificadorNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}