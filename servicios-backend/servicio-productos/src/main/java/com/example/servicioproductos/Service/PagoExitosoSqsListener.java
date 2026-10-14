package com.example.servicioproductos.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PagoExitosoSqsListener {
    private static final Logger log = LoggerFactory.getLogger(PagoExitosoSqsListener.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gamebakes.sqs.dlq-pago}")
    private String dlqUrl;

    @SqsListener(queueNames = "${gamebakes.sqs.queue-pago-exitoso}", id = "pagoExitosoListener")
    public void procesarPagoExitoso(String jsonPago) {
        try {
            procesarPago(jsonPago);
            log.info("Pago procesado exitosamente");
        } catch (Exception e) {
            log.error("Error procesando pago: {}", e.getMessage());
            // Spring Cloud AWS maneja automáticamente los reintentos y DLQ
            throw new RuntimeException(e); // Re-lanzar para que Spring Cloud AWS maneje el reenvío a DLQ
        }
    }

    private void procesarPago(String jsonPago) throws Exception {
        JsonNode root = objectMapper.readTree(jsonPago);

        // Manejar ambos formatos: items array o productoId directo
        if (root.has("items")) {
            // Formato con array de items
            JsonNode items = root.get("items");
            for (JsonNode item : items) {
                Long productoId = item.get("productoId").asLong();
                int cantidad = item.get("cantidad").asInt();
                productoService.restarStock(productoId, cantidad);
                log.info("Stock restado: productoId={}, cantidad={}", productoId, cantidad);
            }
        } else if (root.has("productoId") && root.has("cantidad")) {
            // Formato directo (compra de un solo producto)
            Long productoId = root.get("productoId").asLong();
            int cantidad = root.get("cantidad").asInt();
            productoService.restarStock(productoId, cantidad);
            log.info("Stock restado (directo): productoId={}, cantidad={}", productoId, cantidad);
        } else {
            throw new IllegalArgumentException("Formato de pago inválido: " + jsonPago);
        }
    }
}
