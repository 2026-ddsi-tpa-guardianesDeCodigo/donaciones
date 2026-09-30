package ar.edu.utn.dds.k3003.exceptions;

/** Se intentó borrar un identificador que todavía tiene productos registrados. */
public class IdentificadorEnUsoException extends RuntimeException {
    public IdentificadorEnUsoException(String mensaje) {
        super(mensaje);
    }
}
