package ar.edu.utn.dds.k3003.exceptions;

/** Se intentó borrar una categoría que todavía tiene productos registrados. */
public class CategoriaEnUsoException extends RuntimeException {
    public CategoriaEnUsoException(String mensaje) {
        super(mensaje);
    }
}
