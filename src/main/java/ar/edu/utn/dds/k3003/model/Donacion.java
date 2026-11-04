package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import ar.edu.utn.dds.k3003.eventos.PublicadorDeCambios;

@Entity
@EntityListeners(PublicadorDeCambios.class)
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Versión optimista (JPA). Viaja en cada evento del modelo de lectura (E6): el consumidor solo
    // aplica snapshots con versión mayor a la que ya tiene. El default de columna cubre las filas
    // existentes cuando ddl-auto=update agrega la columna.
    @Version
    @Column(name = "version", columnDefinition = "bigint default 0")
    private Long version;

    private String donadorID;
    private String depositoID;
    private String descripcion;

    @Column(name = "producto_id")
    private Long productoID;

    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    private EstadoDonacionEnum estado;

    private LocalDate fecha;

    // Momento exacto del alta y del último cambio de estado (fecha, de arriba, solo guarda el día).
    // Los necesita el modelo de lectura para medir tiempos entre el ingreso y la entrega.
    @Column(name = "fecha_ingreso")
    private Instant fechaIngreso;

    @Column(name = "fecha_ultimo_cambio")
    private Instant fechaUltimoCambio;

    public Donacion() {
    }

    public Donacion(Long id, String donadorID, String depositoID,
                    String descripcion, Long productoID,
                    Integer cantidad, EstadoDonacionEnum estado,
                    LocalDate fecha) {
        this.id = id;
        this.donadorID = donadorID;
        this.depositoID = depositoID;
        this.descripcion = descripcion;
        this.productoID = productoID;
        this.cantidad = cantidad;
        this.estado = estado;
        this.fecha = fecha;
        this.fechaIngreso = Instant.now();
        this.fechaUltimoCambio = this.fechaIngreso;
    }

    public Long getId() {
        return id;
    }

    public String getDonadorID() {
        return donadorID;
    }

    public String getDepositoID() {
        return depositoID;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Long getProductoID() {
        return productoID;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public EstadoDonacionEnum getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getFechaIngreso() {
        return fechaIngreso;
    }

    public Instant getFechaUltimoCambio() {
        return fechaUltimoCambio;
    }

    public void setEstado(EstadoDonacionEnum estado) {
        this.estado = estado;
        this.fechaUltimoCambio = Instant.now();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}