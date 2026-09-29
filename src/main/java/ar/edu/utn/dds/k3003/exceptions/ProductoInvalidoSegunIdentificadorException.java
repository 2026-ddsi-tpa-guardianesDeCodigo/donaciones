package ar.edu.utn.dds.k3003.exceptions;

/**
 * El producto no cumple la regla de su identificador (barras: descripción de al menos 3 palabras;
 * QR: nombre con cantidad par de letras). Ya se logeó como producto.validacion.fallida en el
 * servicio: el handler responde 400 sin volver a logear el mismo hecho.
 */
public class ProductoInvalidoSegunIdentificadorException extends ProductoInvalidoException {
  public ProductoInvalidoSegunIdentificadorException(String mensaje) {
    super(mensaje);
  }
}
