package ar.edu.utn.dds.k3003.exceptions;

public class NoPuedeDonarException extends RuntimeException {
    public NoPuedeDonarException(String mensaje) {
        super(mensaje);
    }
}