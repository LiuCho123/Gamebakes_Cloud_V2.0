package com.gamebakes.servicio_pedidos.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebakes.servicio_pedidos.Config.RabbitMQConfig;
import com.gamebakes.servicio_pedidos.model.Pedido;
import com.gamebakes.servicio_pedidos.repository.PedidoRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Consume pago.exitoso (publicado por servicio-pagos) y crea los pedidos.
 *
 * Manejo de errores (para que la DLQ tenga sentido):
 *  - mensaje malformado o sin clienteId -> se rechaza sin reencolar, va directo a la DLQ
 *  - fallo al consultar servicio-productos (u otro error) -> se lanza la excepción, Spring
 *    reintenta (spring.rabbitmq.listener.simple.retry.*) y al agotar intentos va a la DLQ
 */
@Component
public class PedidoListener {
    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @RabbitListener(queues = RabbitMQConfig.Q_PAGO_EXITOSO)
    public void escucharPago(String message) {
        System.out.println("[RabbitMQ] pago.exitoso recibido en pedidos: " + message);

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("JSON inválido en pago.exitoso", e);
        }

        if (!jsonNode.has("clienteId")) {
            throw new AmqpRejectAndDontRequeueException("pago.exitoso sin clienteId: " + message);
        }

        Long clienteId = jsonNode.get("clienteId").asLong();
        String clienteNombre = jsonNode.has("clienteNombre") ? jsonNode.get("clienteNombre").asText() : "Cliente";

        // Carrito: llega un arreglo "items"
        if (jsonNode.has("items") && jsonNode.get("items").isArray() && !jsonNode.get("items").isEmpty()) {
            for (JsonNode item : jsonNode.get("items")) {
                if (item.has("productoId") && item.has("cantidad")) {
                    Long productoId = item.get("productoId").asLong();
                    Integer cantidad = item.get("cantidad").asInt();
                    System.out.println("Procesando item -> productoId: " + productoId + ", cantidad: " + cantidad);
                    crearPedido(clienteId, clienteNombre, productoId, cantidad);
                }
            }
        }
        // Compra directa de un solo producto
        else if (jsonNode.has("productoId") && jsonNode.has("cantidad")) {
            Long productoId = jsonNode.get("productoId").asLong();
            Integer cantidad = jsonNode.get("cantidad").asInt();
            crearPedido(clienteId, clienteNombre, productoId, cantidad);
        }
        // Fallback: pago sin detalle de productos
        else {
            Pedido nuevoPedido = new Pedido();
            nuevoPedido.setClienteId(clienteId);
            nuevoPedido.setClienteNombre(clienteNombre);
            nuevoPedido.setVendedorId("1");
            nuevoPedido.setProductoNombre("Producto Comprado");
            nuevoPedido.setEstado("PENDIENTE");
            nuevoPedido.setCantidad(1);

            pedidoRepository.save(nuevoPedido);
            System.out.println("Pedido creado de emergencia para el cliente: " + nuevoPedido.getClienteId());
        }
    }

    @SuppressWarnings("unchecked")
    private void crearPedido(Long clienteId, String clienteNombre, Long productoId, Integer cantidad) {
        String productoUrl = "http://servicio-productos:8085/api/productos/" + productoId;
        Map<String, Object> productoData = restTemplate.getForObject(productoUrl, Map.class);

        if (productoData == null) {
            throw new IllegalStateException("servicio-productos no devolvió datos para el producto " + productoId);
        }

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setClienteId(clienteId);
        nuevoPedido.setClienteNombre(clienteNombre != null ? clienteNombre : "Cliente");
        nuevoPedido.setProductoId(productoId);
        nuevoPedido.setProductoNombre(productoData.get("nombre") != null ? productoData.get("nombre").toString() : "Producto");
        nuevoPedido.setVendedorId(productoData.get("vendedorId") != null ? productoData.get("vendedorId").toString() : "1");
        nuevoPedido.setCantidad(cantidad);
        nuevoPedido.setEstado("PENDIENTE");

        pedidoRepository.save(nuevoPedido);
        System.out.println("Pedido creado para cliente " + nuevoPedido.getClienteId() + ", producto: " + nuevoPedido.getProductoNombre());
    }
}
