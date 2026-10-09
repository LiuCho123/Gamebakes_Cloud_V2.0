package com.example.serviciopagos.Service;

import com.example.serviciopagos.Config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void publicarPagoExitoso(String jsonPago) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAGO_EXITOSO, jsonPago);
        System.out.println("[RabbitMQ] pago.exitoso publicado -> " + jsonPago);

        // TODO (Franco - SQS): enviar jsonPago a la cola SQS "gamebakes-pago-exitoso"
        // para que servicio-productos descuente el stock.
    }
}
