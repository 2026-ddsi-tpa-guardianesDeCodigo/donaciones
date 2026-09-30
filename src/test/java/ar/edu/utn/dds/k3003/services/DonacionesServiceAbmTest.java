package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.CategoriaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.TipoIdentificadorEnum;
import ar.edu.utn.dds.k3003.clients.DonadoresClient;
import ar.edu.utn.dds.k3003.clients.LogisticaClient;
import ar.edu.utn.dds.k3003.exceptions.CategoriaEnUsoException;
import ar.edu.utn.dds.k3003.exceptions.IdentificadorEnUsoException;
import ar.edu.utn.dds.k3003.exceptions.ProductoEnUsoException;
import ar.edu.utn.dds.k3003.exceptions.ProductoInvalidoSegunIdentificadorException;
import ar.edu.utn.dds.k3003.model.Categoria;
import ar.edu.utn.dds.k3003.model.Identificador;
import ar.edu.utn.dds.k3003.model.Producto;
import ar.edu.utn.dds.k3003.model.TipoIdentificador;
import ar.edu.utn.dds.k3003.repositories.CategoriaRepository;
import ar.edu.utn.dds.k3003.repositories.DonacionesRepository;
import ar.edu.utn.dds.k3003.repositories.IdentificadorRepository;
import ar.edu.utn.dds.k3003.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ABM (E5, A8) faltante para el bot/MCP: editar/borrar producto, categoría e identificador, con
 * el chequeo de referencias antes de borrar (mismo criterio que Donadores usó para
 * DELETE /entidades/{id}: no dejar referencias colgando).
 */
class DonacionesServiceAbmTest {

    @Mock DonacionesRepository donacionesRepository;
    @Mock ProductoRepository productoRepository;
    @Mock IdentificadorRepository identificadorRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock DonadoresClient donadoresClient;
    @Mock LogisticaClient logisticaClient;

    DonacionesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DonacionesService(donacionesRepository, productoRepository,
                identificadorRepository, categoriaRepository, donadoresClient, logisticaClient, null);
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(identificadorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Identificador identificadorBarras() {
        Identificador i = new Identificador(1L, TipoIdentificador.CODIGO_BARRAS, "código de barras");
        return i;
    }

    private Categoria categoria() {
        return new Categoria(2L, "Alimentos", "Alimentos no perecederos", null);
    }

    private Producto producto() {
        return new Producto(3L, "Arroz", "Arroz largo fino 1kg", categoria(), identificadorBarras());
    }

    // ---------------------------------------------------------------- producto

    @Test
    @DisplayName("Editar producto: solo toca los campos presentes")
    void editarProducto_soloCamposPresentes() {
        Producto producto = producto();
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));

        ProductoDTO resultado = service.editarProducto(3L,
                new ProductoDTO(null, "Arroz Gallo", null, null, null));

        assertEquals("Arroz Gallo", resultado.nombre());
        assertEquals("Arroz largo fino 1kg", resultado.descripcion(), "no se tocó, no vino en el body");
    }

    @Test
    @DisplayName("Editar producto: re-valida contra la regla del identificador con los valores finales")
    void editarProducto_reValidaLaReglaDelIdentificador() {
        Producto producto = producto();
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));

        // Código de barras exige descripción con >= 3 palabras; "corta" tiene 1.
        assertThrows(ProductoInvalidoSegunIdentificadorException.class, () -> service.editarProducto(3L,
                new ProductoDTO(null, null, "corta", null, null)));

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Editar producto: categoría inexistente")
    void editarProducto_categoriaInexistente() {
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto()));
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.editarProducto(3L,
                new ProductoDTO(null, null, null, 999L, null)));
    }

    @Test
    @DisplayName("Borrar producto sin donaciones que lo referencien")
    void borrarProducto_sinDonaciones_ok() {
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto()));
        when(donacionesRepository.existsByProductoID(3L)).thenReturn(false);

        ProductoDTO resultado = service.borrarProducto(3L);

        assertEquals("Arroz", resultado.nombre());
        verify(productoRepository).deleteById(3L);
    }

    @Test
    @DisplayName("No se borra un producto con donaciones registradas")
    void borrarProducto_conDonaciones_conflicto() {
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto()));
        when(donacionesRepository.existsByProductoID(3L)).thenReturn(true);

        assertThrows(ProductoEnUsoException.class, () -> service.borrarProducto(3L));

        verify(productoRepository, never()).deleteById(anyLong());
    }

    // ---------------------------------------------------------------- categoría

    @Test
    @DisplayName("Editar categoría")
    void editarCategoria_ok() {
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria()));

        CategoriaDTO resultado = service.editarCategoria(2L, new CategoriaDTO(null, "Perecederos", null, null));

        assertEquals("Perecederos", resultado.nombre());
        assertEquals("Alimentos no perecederos", resultado.descripcion());
    }

    @Test
    @DisplayName("Borrar categoría sin productos que la referencien")
    void borrarCategoria_sinProductos_ok() {
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria()));
        when(productoRepository.existsByCategoria_Id(2L)).thenReturn(false);

        service.borrarCategoria(2L);

        verify(categoriaRepository).deleteById(2L);
    }

    @Test
    @DisplayName("No se borra una categoría con productos registrados")
    void borrarCategoria_conProductos_conflicto() {
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria()));
        when(productoRepository.existsByCategoria_Id(2L)).thenReturn(true);

        assertThrows(CategoriaEnUsoException.class, () -> service.borrarCategoria(2L));

        verify(categoriaRepository, never()).deleteById(anyLong());
    }

    // ---------------------------------------------------------------- identificador

    @Test
    @DisplayName("Editar identificador")
    void editarIdentificador_ok() {
        when(identificadorRepository.findById(1L)).thenReturn(Optional.of(identificadorBarras()));

        IdentificadorDTO resultado = service.editarIdentificador(1L,
                new IdentificadorDTO(null, TipoIdentificadorEnum.QR, "código QR"));

        assertEquals(TipoIdentificadorEnum.QR, resultado.tipo());
        assertEquals("código QR", resultado.descripcion());
    }

    @Test
    @DisplayName("Borrar identificador sin productos que lo referencien")
    void borrarIdentificador_sinProductos_ok() {
        when(identificadorRepository.findById(1L)).thenReturn(Optional.of(identificadorBarras()));
        when(productoRepository.existsByIdentificador_Id(1L)).thenReturn(false);

        service.borrarIdentificador(1L);

        verify(identificadorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("No se borra un identificador con productos registrados")
    void borrarIdentificador_conProductos_conflicto() {
        when(identificadorRepository.findById(1L)).thenReturn(Optional.of(identificadorBarras()));
        when(productoRepository.existsByIdentificador_Id(1L)).thenReturn(true);

        assertThrows(IdentificadorEnUsoException.class, () -> service.borrarIdentificador(1L));

        verify(identificadorRepository, never()).deleteById(anyLong());
    }
}
