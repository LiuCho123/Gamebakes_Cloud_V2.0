package com.gamebakes.servicio_resenas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "resenas")
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productoId;
    private String productoNombre;

    //Identificación del Cliente (sub de Cognito, es un UUID)
    @Column(length = 64)
    private String clienteId;
    private String clienteNombre;

    @Column(length = 1000)
    private String comentario;

    private int estrellas; //Del 1 al 5

    //Interacción del Vendedor
    private String respuestaVendedor;
    private String vendedorId; //ID del vendedor dueño del producto (GUID de Azure AD Entra ID)
}