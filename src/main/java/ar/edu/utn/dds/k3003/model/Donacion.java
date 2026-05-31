package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    private String id;

    private String donadorID;
    private String depositoID;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    private EstadoDonacionEnum estado;

    private LocalDate fecha;

    public Donacion() {
    }

    public Donacion(String id, String donadorID, String depositoID,
                    String descripcion, Producto producto,
                    Integer cantidad, EstadoDonacionEnum estado,
                    LocalDate fecha) {
        this.id = id;
        this.donadorID = donadorID;
        this.depositoID = depositoID;
        this.descripcion = descripcion;
        this.producto = producto;
        this.cantidad = cantidad;
        this.estado = estado;
        this.fecha = fecha;
    }

    public String getId() {
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

    public Producto getProducto() {
        return producto;
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