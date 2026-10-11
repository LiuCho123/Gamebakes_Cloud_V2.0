package com.example.serviciopagos.DTO;

import lombok.Data;

@Data
public class SolicitudPagoDTO {
    private Long pedidoId;
    private String clienteId;
    private Double monto;
    private String metodo;
    private Long productoId;
    private Integer cantidad;
}
