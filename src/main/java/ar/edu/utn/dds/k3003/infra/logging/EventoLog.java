package ar.edu.utn.dds.k3003.infra.logging;

public enum EventoLog {
    // --- transversales (idénticos en los 4 componentes) ---
    SERVICE_STARTED("service.started"),
    HTTP_REQUEST_COMPLETED("http.request.completed"),
    HTTP_CLIENT_COMPLETED("http.client.completed"),
    VALIDATION_FAILED("validation.failed"),
    UNHANDLED_EXCEPTION("unhandled.exception"),
    DB_OPERATION_FAILED("db.operation.failed"),

    // --- Donaciones (logging-spec_v1.md §5.3) ---
    DONACION_REGISTRADA("donacion.registrada"),
    DONACION_RECHAZADA("donacion.rechazada"),
    DONACION_ESTADO_CAMBIADO("donacion.estado.cambiado"),
    DONACION_QUEJA_REGISTRADA("donacion.queja.registrada"),
    PRODUCTO_CREADO("producto.creado"),
    PRODUCTO_VALIDACION_FALLIDA("producto.validacion.fallida"),
    CATEGORIA_CREADA("categoria.creada"),
    IDENTIFICADOR_CREADO("identificador.creado"),
    DEBUG_RESET_EJECUTADO("debug.reset.ejecutado");

    private final String action;

    EventoLog(String action) {
        this.action = action;
    }

    public String action() {
        return action;
    }
}
