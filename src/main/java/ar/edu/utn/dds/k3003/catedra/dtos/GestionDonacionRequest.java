package ar.edu.utn.dds.k3003.catedra.dtos;

public record GestionDonacionRequest(
        String donacionID,
        String depositoID,
        String productoID,
        Integer cantidad) {}

