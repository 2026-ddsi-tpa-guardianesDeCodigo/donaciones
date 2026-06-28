package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String donadorID;
    private String depositoID;
    private String descripcion;

    @Column(name = "producto_id")
    private String productoID;

    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    private EstadoDonacionEnum estado;

    private LocalDate fecha;

    public Donacion() {
    }

    public Donacion(Long id, String donadorID, String depositoID,
                    String descripcion, String productoID,
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

    public String getProductoID() {
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

    public void setEstado(EstadoDonacionEnum estado) {
        this.estado = estado;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}