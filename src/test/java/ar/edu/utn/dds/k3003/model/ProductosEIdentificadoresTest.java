package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductosEIdentificadoresTest {

    @Autowired
    private Fachada fachada;

    @Test
    void testAgregarYBuscarIdentificadorCodigoBarras() {
        IdentificadorDTO dto =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-test", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        IdentificadorDTO encontrado = fachada.buscarIdentificadorPorID(dto.id());

        Assertions.assertEquals(dto.id(), encontrado.id());
        Assertions.assertEquals(TipoIdentificadorEnum.CODIGODEBARRAS, encontrado.tipo());
    }
}