package com.gamebakes.servicio_pedidos.service;

import com.gamebakes.servicio_pedidos.model.Pedido;
import com.gamebakes.servicio_pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.gamebakes.servicio_pedidos.Config.RabbitMQConfig;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //VISTA CLIENTE: Obtener sus pedidos personales
    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    //VISTA VENDEDOR: Obtener pedidos dirigidos a sus productos
    public List<Pedido> obtenerPedidosPorVendedor(String vendedorId) {
        return pedidoRepository.findByVendedorId(vendedorId);
    }

    public Pedido crearPedido(Pedido pedido) {
        pedido.setEstado("PENDIENTE");
        Pedido nuevoPedido = pedidoRepository.save(pedido);

        //RabbitMQ: Notificar creación
        notificarSeguimiento("NUEVO_PEDIDO: El cliente " + pedido.getClienteNombre() + " compró " + pedido.getProductoNombre());

        return nuevoPedido;
    }

    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);

        //RabbitMQ: Notificar cambio de estado para seguimiento en tiempo real
        notificarSeguimiento("ESTADO_ACTUALIZADO: Pedido #" + id + " ahora está en " + nuevoEstado);

        return actualizado;
    }

    public boolean validarCompra(Long clienteId, Long productoId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdAndProductoId(clienteId, productoId);
        System.out.println("=== VALIDAR COMPRA ===");
        System.out.println("Cliente: " + clienteId + ", Producto: " + productoId);
        System.out.println("Pedidos encontrados: " + pedidos.size());
        return !pedidos.isEmpty();
    }

    public boolean validarEntregado(Long clienteId, Long productoId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdAndProductoId(clienteId, productoId);
        System.out.println("=== VALIDAR ENTREGADO ===");
        System.out.println("Cliente: " + clienteId + ", Producto: " + productoId);
        System.out.println("Pedidos encontrados: " + pedidos.size());
        for (Pedido pedido : pedidos) {
            System.out.println("  - Pedido ID: " + pedido.getId() + ", Estado: " + pedido.getEstado());
        }
        boolean entregado = pedidos.stream()
                .anyMatch(pedido -> "ENTREGADO".equals(pedido.getEstado()));
        System.out.println("Resultado: " + entregado);
        return entregado;
    }

    // Publica en RabbitMQ sin romper el flujo principal si el broker no responde
    private void notificarSeguimiento(String mensaje) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PEDIDO_SEGUIMIENTO, mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo publicar seguimiento en RabbitMQ: " + e.getMessage());
        }
    }
}