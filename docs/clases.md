classDiagram

class Fachada {
- DonacionesRepository donacionesRepository
- ProductoRepository productoRepository
- DonadoresRepository donadoresRepository
- IdentificadorRepository identificadorRepository
- CategoriaRepository categoriaRepository
- DonacionMapper donacionMapper
- ProductoDataMapper productoDataMapper
- FachadaDonadoresYEntidades fachadaDonadoresYEntidades
- FachadaLogistica fachadaLogistica

+ registrarDonacion(dto: DonacionDTO) DonacionDTO
+ buscarDonacionPorID(id: String) DonacionDTO
+ cambiarEstadoDeDonacion(id: String, estado: EstadoDonacionEnum) DonacionDTO
+ buscarPorDonadorYFechaInicio(donadorID: String, fecha: LocalDate) List~DonacionDTO~
+ registrarQuejaEnDonacion(id: String, descripcion: String) DonacionDTO
+ agregarProducto(dto: ProductoDTO) ProductoDTO
+ buscarProductoPorID(productoID: String) ProductoDTO
+ agregarIdentificador(dto: IdentificadorDTO) IdentificadorDTO
+ buscarIdentificadorPorID(identificadorID: String) IdentificadorDTO
+ setFachadaDonadoresYEntidades(f: FachadaDonadoresYEntidades)
+ setFachadaLogistica(f: FachadaLogistica)
  }

class FachadaDonaciones {
<<interface>>
+ registrarDonacion(dto: DonacionDTO) DonacionDTO
+ buscarDonacionPorID(id: String) DonacionDTO
+ cambiarEstadoDeDonacion(id: String, estado: EstadoDonacionEnum) DonacionDTO
+ buscarPorDonadorYFechaInicio(donadorID: String, fecha: LocalDate) List~DonacionDTO~
+ registrarQuejaEnDonacion(id: String, descripcion: String) DonacionDTO
  }

Fachada ..|> FachadaDonaciones

class FachadaDonadoresYEntidades {
<<interface>>
+ buscarDonadorPorID(id: String) DonadorDTO
+ agregarQueja(q: QuejaDTO) QuejaDTO
+ puedeDonar(id: String) Boolean
  }

class FachadaLogistica {
<<interface>>
+ gestionarDonacion(depID: String, donID: String, prodID: String, cant: Integer) DepositoDTO
  }

Fachada --> FachadaDonadoresYEntidades
Fachada --> FachadaLogistica

class Donacion {
- String id
- String donadorID
- String depositoID
- String descripcion
- Producto producto
- Integer cantidad
- EstadoDonacionEnum estado
- LocalDate fecha

+ getId() String
+ getDonadorID() String
+ getDepositoID() String
+ getDescripcion() String
+ getProducto() Producto
+ getCantidad() Integer
+ getEstado() EstadoDonacionEnum
+ getFecha() LocalDate
+ setEstado(estado: EstadoDonacionEnum)
+ setDescripcion(desc: String)
  }

class Producto {
- String id
- String nombre
- String descripcion
- Categoria categoria
- Identificador identificador

+ getId() String
+ getNombre() String
+ getDescripcion() String
+ getCategoria() Categoria
+ getIdentificador() Identificador
  }

class Categoria {
- String id
- String nombre
- Subcategoria subcategoria

+ getId() String
+ getNombre() String
+ getSubcategoria() Subcategoria
  }

class Subcategoria {
- String id
- String nombre
- Subcategoria subcategoriaPadre

+ getId() String
+ getNombre() String
+ getSubcategoriaPadre() Subcategoria
  }

class Identificador {
- String id
- TipoIdentificador tipo
- String descripcion

+ getId() String
+ getTipo() TipoIdentificador
+ getDescripcion() String
  }

class Donador {
- String id
- String nombre
  }

Donacion --> "1" Producto
Producto --> "1" Categoria
Producto --> "1" Identificador
Categoria --> "1" Subcategoria
Subcategoria --> "0..1" Subcategoria

class DonacionDTO {
<<record>>
}

class ProductoDTO {
<<record>>
}

class IdentificadorDTO {
<<record>>
}

class QuejaDTO {
<<record>>
}

class EstadoDonacionEnum {
<<enumeration>>
INGRESADA
ACEPTADA
CONQUEJA
}

class TipoIdentificador {
<<enumeration>>
CODIGO_BARRAS
CODIGO_QR
}

class TipoIdentificadorEnum {
<<enumeration>>
CODIGODEBARRAS
QR
}

Donacion --> EstadoDonacionEnum
Identificador --> TipoIdentificador

class DonacionesRepository {
<<interface>>
+ save(d: Donacion) Donacion
+ findById(id: String) Optional~Donacion~
+ deleteById(id: String)
+ findAll() List~Donacion~
  }

class ProductoRepository {
<<interface>>
+ save(p: Producto) Producto
+ findById(id: String) Optional~Producto~
  }

class DonadoresRepository {
<<interface>>
+ findById(id: String) Optional~Donador~
+ save(d: Donador) Donador
+ deleteById(id: String) Donador
  }

class IdentificadorRepository {
<<interface>>
+ save(i: Identificador) Identificador
+ findById(id: String) Optional~Identificador~
  }

class CategoriaRepository {
<<interface>>
+ save(c: Categoria) Categoria
+ findById(id: String) Optional~Categoria~
  }

class InMemoryDonacionesRepo
class InMemoryProductoRepo
class InMemoryDonadoresRepo
class InMemoryIdentificadorRepo
class InMemoryCategoriaRepo

DonacionesRepository <|.. InMemoryDonacionesRepo
ProductoRepository <|.. InMemoryProductoRepo
DonadoresRepository <|.. InMemoryDonadoresRepo
IdentificadorRepository <|.. InMemoryIdentificadorRepo
CategoriaRepository <|.. InMemoryCategoriaRepo

class DonacionMapper {
+ toDonacionDTO(d: Donacion) DonacionDTO
  }

class ProductoDataMapper {
+ toDTO(p: Producto) ProductoDTO
  }

DonacionMapper --> Donacion
DonacionMapper --> DonacionDTO

ProductoDataMapper --> Producto
ProductoDataMapper --> ProductoDTO

Fachada --> DonacionesRepository
Fachada --> ProductoRepository
Fachada --> DonadoresRepository
Fachada --> IdentificadorRepository
Fachada --> CategoriaRepository
Fachada --> DonacionMapper
Fachada --> ProductoDataMapper
Fachada --> DonacionDTO
Fachada --> ProductoDTO
Fachada --> IdentificadorDTO
Fachada --> QuejaDTO
Fachada --> Donacion
Fachada --> Producto
Fachada --> Identificador
Fachada --> Categoria