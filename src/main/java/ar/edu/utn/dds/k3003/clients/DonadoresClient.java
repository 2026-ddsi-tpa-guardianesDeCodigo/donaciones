package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DonadoresClient {

    private final RestClient restClient;

    public DonadoresClient(@Value("${donadores.client}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
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
                .uri("/quejas")
                .body(quejaDTO)
                .retrieve()
                .toBodilessEntity();
    }
    public record PuedeDonarResponse(Boolean puedeDonar) {
    }
}