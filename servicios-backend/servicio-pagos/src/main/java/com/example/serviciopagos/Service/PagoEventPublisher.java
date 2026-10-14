package com.example.serviciopagos.Service;

import com.example.serviciopagos.Config.RabbitMQConfig;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Único punto por donde servicio-pagos publica el evento "pago exitoso".
 *
 * RabbitMQ -> servicio-pedidos crea el pedido.
 * AWS SQS  -> servicio-productos baja el stock (parte de Franco).
 */
@Component
public class PagoEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Value("${gamebakes.sqs.queue-pago-exitoso}")
    private String sqsQueueUrl;

    public void publicarPagoExitoso(String jsonPago) {
        // Publicar a RabbitMQ (mantener sin cambios)
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAGO_EXITOSO, jsonPago);
        System.out.println("[RabbitMQ] pago.exitoso publicado -> " + jsonPago);

        // Publicar a SQS (nuevo)
        try {
            sqsTemplate.send(sqsQueueUrl, jsonPago);
            System.out.println("[SQS] pago exitoso enviado a gamebakes-pago-exitoso -> " + jsonPago);
        } catch (Exception e) {
            System.err.println("[SQS] Error al enviar pago exitoso: " + e.getMessage());
            // No fallar el flujo principal si SQS falla
        }
    }
}
