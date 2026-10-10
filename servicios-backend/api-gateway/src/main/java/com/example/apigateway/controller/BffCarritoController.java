package com.example.apigateway.controller;

import com.example.apigateway.client.PagoClient;
import com.example.apigateway.client.ProductoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://44.218.147.127",
        "http://44.218.147.127:5173",
        "https://gamebakescloud.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/bff/carrito")
public class BffCarritoController {

    @Autowired
    private PagoClient pagoClient;

    @Autowired
    private ProductoClient productoClient;

    @GetMapping("/completo/{clienteId}")
    public Mono<Map<String, Object>> obtenerCarritoCompleto(
            @PathVariable String clienteId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        return pagoClient.obtenerCarrito(clienteId, authHeader)
                .flatMap(item -> {
                    Long productoId = item.get("productoId") instanceof Number ?
                            ((Number) item.get("productoId")).longValue() : null;

                    if (productoId != null) {
                        return productoClient.obtenerProducto(productoId)
                                .map(producto -> {
                                    item.put("productoDetalle", producto);
                                    return item;
                                })
                                .onErrorResume(e -> {
                                    item.put("productoDetalle", null);
                                    return Mono.just(item);
                                });
                    } else {
                        item.put("productoDetalle", null);
                        return Mono.just(item);
                    }
                })
                .collectList()
                .map(items -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("items", items);
                    response.put("total", calcularTotal(items));
                    return response;
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("items", new java.util.ArrayList<>());
                    errorResponse.put("total", 0.0);
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    private double calcularTotal(java.util.List<Map<String, Object>> items) {
        double total = 0.0;
        for (Map<String, Object> item : items) {
            Number cantidad = (Number) item.get("cantidad");
            Number precio = (Number) item.get("precioUnitario");
            if (cantidad != null && precio != null) {
                total += cantidad.doubleValue() * precio.doubleValue();
            }
        }
        return total;
    }
}