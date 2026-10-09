package com.gamebakes.servicio_pedidos.Config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ de servicio-pedidos (EP2).
 *
 * Consume : pedidos.pago-exitoso.queue  (routing key pago.exitoso, la publica servicio-pagos)
 * Publica : pedido.seguimiento          (cambios de estado; la consume pedidos.seguimiento.queue)
 * Cada cola tiene su DLQ en gamebakes.dlx.
 *
 * El exchange, el DLX y la cola de pago exitoso también se declaran en servicio-pagos con
 * los mismos argumentos; en RabbitMQ eso es idempotente. Los nombres deben coincidir
 * EXACTAMENTE entre ambos servicios.
 */
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "gamebakes.exchange";
    public static final String DLX = "gamebakes.dlx";

    public static final String RK_PAGO_EXITOSO = "pago.exitoso";
    public static final String RK_PEDIDO_SEGUIMIENTO = "pedido.seguimiento";

    public static final String Q_PAGO_EXITOSO = "pedidos.pago-exitoso.queue";
    public static final String DLQ_PAGO_EXITOSO = "pedidos.pago-exitoso.dlq";
    public static final String Q_SEGUIMIENTO = "pedidos.seguimiento.queue";
    public static final String DLQ_SEGUIMIENTO = "pedidos.seguimiento.dlq";

    @Bean
    public TopicExchange gamebakesExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ---------- Pago exitoso ----------
    @Bean
    public Queue pagoExitosoQueue() {
        return QueueBuilder.durable(Q_PAGO_EXITOSO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PAGO_EXITOSO)
                .build();
    }

    @Bean
    public Queue pagoExitosoDlq() {
        return QueueBuilder.durable(DLQ_PAGO_EXITOSO).build();
    }

    @Bean
    public Binding pagoExitosoBinding() {
        return BindingBuilder.bind(pagoExitosoQueue()).to(gamebakesExchange()).with(RK_PAGO_EXITOSO);
    }

    @Bean
    public Binding pagoExitosoDlqBinding() {
        return BindingBuilder.bind(pagoExitosoDlq()).to(deadLetterExchange()).with(DLQ_PAGO_EXITOSO);
    }

    // ---------- Seguimiento de pedidos ----------
    @Bean
    public Queue seguimientoQueue() {
        return QueueBuilder.durable(Q_SEGUIMIENTO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_SEGUIMIENTO)
                .build();
    }

    @Bean
    public Queue seguimientoDlq() {
        return QueueBuilder.durable(DLQ_SEGUIMIENTO).build();
    }

    @Bean
    public Binding seguimientoBinding() {
        return BindingBuilder.bind(seguimientoQueue()).to(gamebakesExchange()).with(RK_PEDIDO_SEGUIMIENTO);
    }

    @Bean
    public Binding seguimientoDlqBinding() {
        return BindingBuilder.bind(seguimientoDlq()).to(deadLetterExchange()).with(DLQ_SEGUIMIENTO);
    }
}
