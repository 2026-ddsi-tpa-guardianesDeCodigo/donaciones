package ar.edu.utn.dds.k3003.eventos;

/**
 * Evento interno (Spring) que se emite cuando JPA guarda o borra una entidad dueña de este módulo.
 * {@link PublicadorEventos} lo traduce a un {@link EventoDominio} recién después del commit.
 */
public record CambioDeEntidad(Object entidad, boolean eliminada) {
}
