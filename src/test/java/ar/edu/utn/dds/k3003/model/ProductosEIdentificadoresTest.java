package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductosEIdentificadoresTest {

    private Fachada fachada;

    @BeforeEach
    void setUp() {
        fachada = new Fachada();
    }

    @Test
    void testAgregarYBuscarIdentificadorCodigoBarras() {
        IdentificadorDTO dto =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-test", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        IdentificadorDTO encontrado = fachada.buscarIdentificadorPorID(dto.id());

        Assertions.assertEquals(dto.id(), encontrado.id());
        Assertions.assertEquals(TipoIdentificadorEnum.CODIGODEBARRAS, encontrado.tipo());
    }

    @Test
    void testAgregarYBuscarIdentificadorQR() {
        IdentificadorDTO dto =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-qr-test", TipoIdentificadorEnum.QR, "codigo qr"));

        IdentificadorDTO encontrado = fachada.buscarIdentificadorPorID(dto.id());

        Assertions.assertEquals(dto.id(), encontrado.id());
        Assertions.assertEquals(TipoIdentificadorEnum.QR, encontrado.tipo());
    }

    @Test
    void testAgregarProductoCodigoBarrasValido() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-prod", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        ProductoDTO producto =
                fachada.agregarProducto(
                        new ProductoDTO(
                                "prod-barra-valido",
                                "producto",
                                "remera roja grande",
                                null,
                                identificador.id()));

        Assertions.assertEquals("prod-barra-valido", producto.id());
        Assertions.assertEquals("producto", producto.nombre());
        Assertions.assertEquals("remera roja grande", producto.descripcion());
    }

    @Test
    void testAgregarProductoCodigoBarrasInvalido() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-invalido", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        fachada.agregarProducto(
                                new ProductoDTO(
                                        "prod-barra-invalido",
                                        "producto",
                                        "remera roja",
                                        null,
                                        identificador.id())));
    }

    @Test
    void testAgregarProductoQRValido() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-qr-prod", TipoIdentificadorEnum.QR, "codigo qr"));

        ProductoDTO producto =
                fachada.agregarProducto(
                        new ProductoDTO(
                                "prod-qr-valido",
                                "Mesa",
                                "descripcion cualquiera",
                                null,
                                identificador.id()));

        Assertions.assertEquals("prod-qr-valido", producto.id());
        Assertions.assertEquals("Mesa", producto.nombre());
    }

    @Test
    void testAgregarProductoQRInvalido() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-qr-invalido", TipoIdentificadorEnum.QR, "codigo qr"));

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        fachada.agregarProducto(
                                new ProductoDTO(
                                        "prod-qr-invalido",
                                        "Silla",
                                        "descripcion cualquiera",
                                        null,
                                        identificador.id())));
    }

    @Test
    void testBuscarProductoPorID() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-buscar", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        ProductoDTO producto =
                fachada.agregarProducto(
                        new ProductoDTO(
                                "prod-buscar",
                                "producto",
                                "remera roja grande",
                                null,
                                identificador.id()));

        ProductoDTO encontrado = fachada.buscarProductoPorID(producto.id());

        Assertions.assertEquals(producto.id(), encontrado.id());
        Assertions.assertEquals(producto.nombre(), encontrado.nombre());
    }

    @Test
    void testBuscarProductoPorIDInexistente() {
        Assertions.assertThrows(
                RuntimeException.class,
                () -> fachada.buscarProductoPorID("producto-inexistente"));
    }

    @Test
    void testAgregarProductoSinIdentificadorPrevioFalla() {
        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        fachada.agregarProducto(
                                new ProductoDTO(
                                        "prod-sin-identificador",
                                        "producto",
                                        "remera roja grande",
                                        null,
                                        "identificador-inexistente")));
    }

    @Test
    void testAgregarProductoConCategoriaInexistenteFalla() {
        IdentificadorDTO identificador =
                fachada.agregarIdentificador(
                        new IdentificadorDTO("id-barra-cat-inexistente", TipoIdentificadorEnum.CODIGODEBARRAS, "codigo barras"));

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        fachada.agregarProducto(
                                new ProductoDTO(
                                        "prod-cat-inexistente",
                                        "producto",
                                        "remera roja grande",
                                        "categoria-inexistente",
                                        identificador.id())));
    }
}