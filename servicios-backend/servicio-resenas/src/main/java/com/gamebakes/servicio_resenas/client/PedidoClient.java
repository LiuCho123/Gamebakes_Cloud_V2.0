package com.gamebakes.servicio_resenas.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "servicio-pedidos",
        url = "http://servicio-pedidos:8082/api/pedidos"
)
public interface PedidoClient {

    @GetMapping("/validar-compra")
    @Retryable(maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 1000))
    @Cacheable(value = "validacionesCompra", key = "#clienteId + '-' + #productoId")
    @CircuitBreaker(name = "pedidoCB", fallbackMethod = "fallbackValidarCompra")
    boolean validarCompra(
            @RequestParam("clienteId") String clienteId,
            @RequestParam("productoId") Long productoId
    );

    @GetMapping("/validar-entregado")
    @Retryable(maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 1000))
    @Cacheable(value = "validacionesEntregado", key = "#clienteId + '-' + #productoId")
    @CircuitBreaker(name = "pedidoCB", fallbackMethod = "fallbackValidarEntregado")
    boolean validarEntregado(
            @RequestParam("clienteId") String clienteId,
            @RequestParam("productoId") Long productoId
    );

    default boolean fallbackValidarCompra(String clienteId, Long productoId, Throwable t) {
        System.out.println("Circuit Breaker: Error validando compra. Cliente: " + clienteId + ", Producto: " + productoId + ". Motivo: " + t.getMessage());
        return false;
    }

    default boolean fallbackValidarEntregado(String clienteId, Long productoId, Throwable t) {
        System.out.println("Circuit Breaker: Error validando estado entregado. Cliente: " + clienteId + ", Producto: " + productoId + ". Motivo: " + t.getMessage());
        return false;
    }

}