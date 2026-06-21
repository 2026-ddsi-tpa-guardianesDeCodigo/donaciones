package ar.edu.utn.dds.k3003.exceptions;

public class CategoriaNoEncontradaException extends RuntimeException {
    public CategoriaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}