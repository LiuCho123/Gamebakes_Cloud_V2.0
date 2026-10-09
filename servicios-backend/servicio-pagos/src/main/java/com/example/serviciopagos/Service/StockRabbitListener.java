package com.example.serviciopagos.Service;

import com.example.serviciopagos.Config.RabbitMQConfig;
import com.example.serviciopagos.Model.ProductoStockCache;
import com.example.serviciopagos.Repository.ProductoStockCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mantiene el cache de stock de pagos a partir del evento stock.actualizado que
 * publica servicio-productos. Si el mensaje es inválido se manda directo a la DLQ.
 * Los errores transitorios (por ejemplo la BD caída) se reintentan según
 * spring.rabbitmq.listener.simple.retry.* y, al agotar los intentos, también van a la DLQ.
 */
@Component
public class StockRabbitListener {
    @Autowired
    private ProductoStockCacheRepository stockCacheRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.Q_STOCK_PRODUCTOS)
    public void escucharCambiosStock(String mensajeJson) {
        System.out.println("[RabbitMQ] stock.actualizado recibido en pagos: " + mensajeJson);

        Long productoId;
        Integer stock;
        try {
            JsonNode evento = mapper.readTree(mensajeJson);
            productoId = evento.get("productoId").asLong();
            stock = evento.get("stock").asInt();
        } catch (Exception e) {
            // Mensaje malformado: no sirve reintentar, va directo a la DLQ
            throw new AmqpRejectAndDontRequeueException("Mensaje de stock inválido: " + mensajeJson, e);
        }

        stockCacheRepository.save(new ProductoStockCache(productoId, stock));
        System.out.println("Stock guardado en la BD de Pagos: ID " + productoId + " -> " + stock);
    }
}
