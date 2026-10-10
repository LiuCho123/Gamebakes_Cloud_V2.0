package com.example.apigateway.controller;

import com.example.apigateway.client.PedidoClient;
import com.example.apigateway.client.PerfilClient;
import com.example.apigateway.client.ResenaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/dashboard")
public class BffDashboardController {

    @Autowired
    private PedidoClient pedidoClient;

    @Autowired
    private PerfilClient perfilClient;

    @Autowired
    private ResenaClient resenaClient;

    @GetMapping("/cliente/{clienteId}")
    public Mono<Map<String, Object>> obtenerDashboardCliente(
            @PathVariable Long clienteId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return Mono.zip(
                perfilClient.obtenerPerfil(clienteId),
                pedidoClient.obtenerMisPedidos(String.valueOf(clienteId), authHeader).collectList(),
                resenaClient.obtenerResenasCliente().collectList()
        )
        .map(tuple -> {
            Map<String, Object> response = new HashMap<>();
            response.put("perfil", tuple.getT1());
            response.put("pedidos", tuple.getT2());
            response.put("resenas", tuple.getT3());
            response.put("estadisticas", calcularEstadisticas(tuple.getT2(), tuple.getT3()));
            return response;
        })
        .onErrorResume(e -> {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return Mono.just(errorResponse);
        });
    }

    @GetMapping("/vendedor/{vendedorId}")
    public Mono<Map<String, Object>> obtenerDashboardVendedor(
            @PathVariable Long vendedorId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return Mono.zip(
                perfilClient.obtenerPerfil(vendedorId),
                pedidoClient.obtenerPedidosVendedor(String.valueOf(vendedorId), authHeader).collectList(),
                resenaClient.obtenerResenasVendedor(vendedorId).collectList()
        )
        .map(tuple -> {
            Map<String, Object> response = new HashMap<>();
            response.put("perfil", tuple.getT1());
            response.put("pedidos", tuple.getT2());
            response.put("resenas", tuple.getT3());
            response.put("estadisticas", calcularEstadisticas(tuple.getT2(), tuple.getT3()));
            return response;
        })
        .onErrorResume(e -> {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return Mono.just(errorResponse);
        });
    }

    private Map<String, Object> calcularEstadisticas(List<Map<String, Object>> pedidos, List<Map<String, Object>> resenas) {
        Map<String, Object> estadisticas = new HashMap<>();

        int totalPedidos = pedidos != null ? pedidos.size() : 0;
        int totalResenas = resenas != null ? resenas.size() : 0;

        int pedidosEntregados = 0;
        int pedidosEnCamino = 0;
        int pedidosPreparacion = 0;

        if (pedidos != null) {
            for (Map<String, Object> pedido : pedidos) {
                String estado = (String) pedido.get("estado");
                if ("ENTREGADO".equals(estado)) pedidosEntregados++;
                else if ("EN_CAMINO".equals(estado)) pedidosEnCamino++;
                else if ("PREPARACION".equals(estado)) pedidosPreparacion++;
            }
        }

        double promedioEstrellas = 0.0;
        if (resenas != null && !resenas.isEmpty()) {
            double sumaEstrellas = 0.0;
            for (Map<String, Object> resena : resenas) {
                Number estrellas = (Number) resena.get("estrellas");
                if (estrellas != null) {
                    sumaEstrellas += estrellas.doubleValue();
                }
            }
            promedioEstrellas = sumaEstrellas / resenas.size();
        }

        estadisticas.put("totalPedidos", totalPedidos);
        estadisticas.put("totalResenas", totalResenas);
        estadisticas.put("pedidosEntregados", pedidosEntregados);
        estadisticas.put("pedidosEnCamino", pedidosEnCamino);
        estadisticas.put("pedidosPreparacion", pedidosPreparacion);
        estadisticas.put("promedioEstrellas", promedioEstrellas);

        return estadisticas;
    }
}
