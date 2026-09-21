package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.infra.logging.ClienteHttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DonadoresClient {

    private final RestClient restClient;

    public DonadoresClient(@Value("${donadores.client}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new ClienteHttpLoggingInterceptor("donadores"))
                .build();
    }

    public void buscarDonadorPorID(String donadorID) {
        restClient.get()
                .uri("/donadores/{id}", donadorID)
                .retrieve()
                .toBodilessEntity();
    }


    public Boolean puedeDonar(String donadorID) {
        PuedeDonarResponse response = restClient.get()
                .uri("/donadores/{id}/puede-donar", donadorID)
                .retrieve()
                .body(PuedeDonarResponse.class);

        return response != null && response.puedeDonar();
    }

    public void agregarQueja(QuejaDTO quejaDTO) {
        restClient.post()
                .uri("/donadores/{donadorID}/quejas", quejaDTO.donadorID())
                // Explícito: con jackson-dataformat-xml en el classpath RestClient serializaría el body como XML.
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "donacionID", String.valueOf(quejaDTO.donacionID()),
                        "descripcion", quejaDTO.descripcion()))
                .retrieve()
                .toBodilessEntity();
    }
    public record PuedeDonarResponse(Boolean puedeDonar) {
    }
}