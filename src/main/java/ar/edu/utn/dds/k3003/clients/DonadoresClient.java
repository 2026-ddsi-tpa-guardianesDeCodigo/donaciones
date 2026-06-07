package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DonadoresClient {

    private final RestClient restClient;

    public DonadoresClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public void buscarDonadorPorID(String donadorID) {
        restClient.get()
                .uri("/donadores/{id}", donadorID)
                .retrieve()
                .toBodilessEntity();
    }

    public Boolean puedeDonar(String donadorID) {
        return restClient.get()
                .uri("/donadores/{id}/puede-donar", donadorID)
                .retrieve()
                .body(Boolean.class);
    }

    public void agregarQueja(QuejaDTO quejaDTO) {
        restClient.post()
                .uri("/quejas")
                .body(quejaDTO)
                .retrieve()
                .toBodilessEntity();
    }
}