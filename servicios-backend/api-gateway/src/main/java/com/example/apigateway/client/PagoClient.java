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
public class PagoClient {

    private final WebClient webClient;

    public PagoClient(@Qualifier("pagosWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // CAMBIO: clienteId ahora es String y recibe el authHeader
    public Flux<Map<String, Object>> obtenerCarrito(String clienteId, String authHeader) {
        return webClient.get()
                .uri("/carrito/{clienteId}", clienteId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> agregarAlCarrito(Map<String, Object> item, String authHeader) {
        return webClient.post()
                .uri("/carrito/agregar")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(item)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> iniciarPago(Map<String, Object> solicitud, String authHeader) {
        return webClient.post()
                .uri("/iniciar")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(solicitud)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> iniciarPagoDesdeCarrito(String clienteId, String authHeader) {
        return webClient.post()
                .uri("/iniciar-desde-carrito/{clienteId}", clienteId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> confirmarPago(Long idPago, String authHeader) {
        return webClient.post()
                .uri("/confirmar/{idPago}", idPago)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}