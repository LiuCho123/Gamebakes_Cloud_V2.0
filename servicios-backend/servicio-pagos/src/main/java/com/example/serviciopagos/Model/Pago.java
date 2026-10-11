package com.example.serviciopagos.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    private Long pedidoId;

    // Id del usuario: UUID de Cognito (sub) u oid de Entra. Por eso es texto, no numero.
    @Column(length = 64)
    private String clienteId;

    private Double monto;
    private String metodoPago;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaPago;
    private Long productoId;
    private Integer cantidad;
}
