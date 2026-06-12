package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.GestionDonacionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LogisticaClient{

    private final RestClient restClient;

    public LogisticaClient(@Value("${logistica.client}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void gestionarDonacion(
            String depositoID,
            String donacionID,
            String productoID,
            Integer cantidad
    ) {
        GestionDonacionRequest request = new GestionDonacionRequest(
                depositoID,
                donacionID,
                productoID,
                cantidad
        );

        restClient.post()
                .uri("/depositos/{id}/donacion", depositoID)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}