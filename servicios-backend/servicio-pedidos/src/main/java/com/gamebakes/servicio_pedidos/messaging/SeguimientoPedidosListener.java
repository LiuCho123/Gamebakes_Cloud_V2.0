package com.gamebakes.servicio_pedidos.messaging;

import com.gamebakes.servicio_pedidos.Config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume las notificaciones de seguimiento que el propio servicio publica al crear o
 * cambiar el estado de un pedido. Antes nadie consumía este evento.
 */
@Component
public class SeguimientoPedidosListener {
    @RabbitListener(queues = RabbitMQConfig.Q_SEGUIMIENTO)
    public void escucharSeguimiento(String notificacion) {
        System.out.println("[RabbitMQ] seguimiento de pedido: " + notificacion);
    }
}
