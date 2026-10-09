package com.example.serviciopagos.Config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ de servicio-pagos (EP2).
 *
 * Exchange principal  : gamebakes.exchange (topic)  -> compartido por todos los servicios
 * Dead letter exchange: gamebakes.dlx (direct)      -> recibe los mensajes que fallan
 */
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "gamebakes.exchange";
    public static final String DLX = "gamebakes.dlx";

    // Routing keys
    public static final String RK_PAGO_EXITOSO = "pago.exitoso";
    public static final String RK_STOCK_ACTUALIZADO = "stock.actualizado";

    // Colas
    public static final String Q_STOCK_PRODUCTOS = "pagos.stock-productos.queue";
    public static final String DLQ_STOCK_PRODUCTOS = "pagos.stock-productos.dlq";
    public static final String Q_PEDIDOS_PAGO_EXITOSO = "pedidos.pago-exitoso.queue";
    public static final String DLQ_PEDIDOS_PAGO_EXITOSO = "pedidos.pago-exitoso.dlq";

    @Bean
    public TopicExchange gamebakesExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ---------- Cola de stock (la consume pagos) ----------
    @Bean
    public Queue stockProductosQueue() {
        return QueueBuilder.durable(Q_STOCK_PRODUCTOS)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_STOCK_PRODUCTOS)
                .build();
    }

    @Bean
    public Queue stockProductosDlq() {
        return QueueBuilder.durable(DLQ_STOCK_PRODUCTOS).build();
    }

    @Bean
    public Binding stockProductosBinding() {
        return BindingBuilder.bind(stockProductosQueue()).to(gamebakesExchange()).with(RK_STOCK_ACTUALIZADO);
    }

    @Bean
    public Binding stockProductosDlqBinding() {
        return BindingBuilder.bind(stockProductosDlq()).to(deadLetterExchange()).with(DLQ_STOCK_PRODUCTOS);
    }

    // ---------- Cola de pago exitoso (publica pagos, consume pedidos) ----------
    // Se declara también aquí para que el mensaje no se pierda si pagos publica
    // antes de que pedidos haya arrancado. Los nombres deben ser idénticos en ambos servicios.
    @Bean
    public Queue pedidosPagoExitosoQueue() {
        return QueueBuilder.durable(Q_PEDIDOS_PAGO_EXITOSO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PEDIDOS_PAGO_EXITOSO)
                .build();
    }

    @Bean
    public Queue pedidosPagoExitosoDlq() {
        return QueueBuilder.durable(DLQ_PEDIDOS_PAGO_EXITOSO).build();
    }

    @Bean
    public Binding pedidosPagoExitosoBinding() {
        return BindingBuilder.bind(pedidosPagoExitosoQueue()).to(gamebakesExchange()).with(RK_PAGO_EXITOSO);
    }

    @Bean
    public Binding pedidosPagoExitosoDlqBinding() {
        return BindingBuilder.bind(pedidosPagoExitosoDlq()).to(deadLetterExchange()).with(DLQ_PEDIDOS_PAGO_EXITOSO);
    }
}
