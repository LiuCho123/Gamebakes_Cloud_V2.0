package com.example.serviciopagos.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "carrito_items")
@Data
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del usuario: UUID de Cognito (sub) u oid de Entra. Por eso es texto, no numero.
    @Column(length = 64)
    private String clienteId;

    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
}