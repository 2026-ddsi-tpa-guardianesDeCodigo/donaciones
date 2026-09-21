package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.GestionDonacionRequest;
import ar.edu.utn.dds.k3003.infra.logging.ClienteHttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LogisticaClient{

    private final RestClient restClient;

    public LogisticaClient(@Value("${logistica.client}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new ClienteHttpLoggingInterceptor("logistica"))
                .build();
    }

    public void gestionarDonacion(
            String depositoID,
            String donacionID,
            String productoID,
            Integer cantidad
    ) {
        GestionDonacionRequest request = new GestionDonacionRequest(
                donacionID,
                depositoID,
                productoID,
                cantidad
        );

        restClient.post()
                .uri("/depositos/{id}/donacion", depositoID)
                // Explícito: con jackson-dataformat-xml en el classpath RestClient serializaría el body como XML.
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}