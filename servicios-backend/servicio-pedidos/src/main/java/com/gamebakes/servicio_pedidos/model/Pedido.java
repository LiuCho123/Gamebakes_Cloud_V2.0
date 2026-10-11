package com.gamebakes.servicio_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID del usuario que compra: UUID de Cognito (sub), por eso es texto y no numero
    @Column(length = 64)
    private String clienteId;

    private String clienteNombre; 
    private Long productoId; //ID del producto
    private String productoNombre;
    private String vendedorId; //ID del vendedor que debe preparar el pedido (GUID de Azure AD Entra ID)
    private String estado; //PENDIENTE, PREPARACION, EN_CAMINO, ENTREGADO
    private Integer cantidad;
}