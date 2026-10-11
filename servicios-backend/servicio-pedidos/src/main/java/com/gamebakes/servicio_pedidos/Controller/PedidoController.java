package com.gamebakes.servicio_pedidos.controller;

import com.gamebakes.servicio_pedidos.model.Pedido;
import com.gamebakes.servicio_pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/mis-pedidos")
    public List<Pedido> listarMisPedidos(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) return List.of();
        return pedidoService.obtenerPedidosPorCliente(userId);
    }

    @GetMapping("/vendedor/{vendedorId}")
    public List<Pedido> listarPorVendedor(@PathVariable String vendedorId) {
        return pedidoService.obtenerPedidosPorVendedor(vendedorId);
    }

    @PostMapping
    public Pedido crear(
            @RequestBody Pedido pedido,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId != null) {
            pedido.setClienteId(userId);
        }
        return pedidoService.crearPedido(pedido);
    }

    @PutMapping("/{id}/estado")
    public Pedido actualizar(@PathVariable Long id, @RequestParam String nuevoEstado) {
        return pedidoService.actualizarEstado(id, nuevoEstado);
    }

    @GetMapping("/validar-compra")
    public boolean validarCompra(
            @RequestParam("clienteId") String clienteId,
            @RequestParam("productoId") Long productoId) {
        return pedidoService.validarCompra(clienteId, productoId);
    }

    @GetMapping("/validar-entregado")
    public boolean validarEntregado(
            @RequestParam("clienteId") String clienteId,
            @RequestParam("productoId") Long productoId) {
        return pedidoService.validarEntregado(clienteId, productoId);
    }
}