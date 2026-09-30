package ar.edu.utn.dds.k3003.exceptions;

/** Se intentó borrar un producto que todavía tiene donaciones registradas a su nombre. */
public class ProductoEnUsoException extends RuntimeException {
    public ProductoEnUsoException(String mensaje) {
        super(mensaje);
    }
}
