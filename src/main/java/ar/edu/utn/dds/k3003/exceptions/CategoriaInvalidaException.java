package ar.edu.utn.dds.k3003.exceptions;

public class CategoriaInvalidaException extends RuntimeException {
    public CategoriaInvalidaException (String mensaje) {
        super(mensaje);
    }
}