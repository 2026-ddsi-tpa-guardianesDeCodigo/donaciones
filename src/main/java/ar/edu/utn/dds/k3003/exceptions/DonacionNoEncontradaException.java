package ar.edu.utn.dds.k3003.exceptions;

public class DonacionNoEncontradaException extends RuntimeException {
    public DonacionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}