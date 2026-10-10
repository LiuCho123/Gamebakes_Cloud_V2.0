package com.example.apigateway.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class PedidoClient {

    private final WebClient webClient;

    public PedidoClient(@Qualifier("pedidosWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Map<String, Object>> obtenerMisPedidos(String clienteId, String authHeader) {
        return webClient.get()
                .uri("/mis-pedidos")
                .header("X-User-Id", clienteId) // Ya es String directamente
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Flux<Map<String, Object>> obtenerPedidosVendedor(String vendedorId, String authHeader) {
        return webClient.get()
                .uri("/vendedor/{vendedorId}", vendedorId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> validarCompra(String clienteId, Long productoId, String authHeader) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{id}/estado")
                        .queryParam("productoId", productoId)
                        .build(clienteId))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Void> cambiarEstado(Long id, String nuevoEstado, String authHeader) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{id}/estado")
                        .queryParam("nuevoEstado", nuevoEstado)
                        .build(id))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Void.class);
    }
}