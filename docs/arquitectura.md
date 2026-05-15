flowchart LR

subgraph Cliente
A["App / Usuario"]
end

subgraph Servidor_Aplicacion
B["Fachada"]
C["DonacionMapper"]
D["InMemoryDonacionesRepo"]
E["InMemoryProductoRepo"]
F["InMemoryDonadoresRepo"]
end

subgraph Sistema_Donadores
G["FachadaDonadoresYEntidades"]
end

subgraph Sistema_Logistica
H["FachadaLogistica"]
end

A --> B

B --> C
B --> D
B --> E
B --> F

B --> G
B --> H