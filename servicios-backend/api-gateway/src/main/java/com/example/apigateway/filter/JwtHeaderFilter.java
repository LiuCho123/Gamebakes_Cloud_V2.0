package com.example.apigateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtHeaderFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");

            if (parts.length == 3) {
                try {
                    String payloadJson = new String(
                            Base64.getUrlDecoder().decode(parts[1]),
                            StandardCharsets.UTF_8
                    );
                    JsonNode claims = objectMapper.readTree(payloadJson);

                    // AQUÍ ESTÁ LA MAGIA: Busca 'oid' (Azure) y si no, 'sub' (Cognito)
                    String userId = null;
                    if (claims.has("oid")) {
                        userId = claims.get("oid").asText();
                    } else if (claims.has("sub")) {
                        userId = claims.get("sub").asText();
                    }

                    String rol = "SIN_ROL";

                    // Mantiene la lógica de los roles si es que existen
                    if (claims.has("roles") && claims.get("roles").isArray()
                            && !claims.get("roles").isEmpty()) {
                        rol = claims.get("roles").get(0).asText();
                    }

                    if (userId != null) {
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-User-Id", userId)
                                .header("X-User-Role", rol)
                                .build();

                        exchange = exchange.mutate().request(mutatedRequest).build();
                    }
                } catch (Exception e) {
                    // Token malformado, el destino se encarga de rechazarlo.
                }
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}