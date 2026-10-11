package com.example.serviciopagos.Controller;

import com.example.serviciopagos.Model.CarritoItem;
import com.example.serviciopagos.Service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{clienteId}")
    public ResponseEntity<List<CarritoItem>> obtener(@PathVariable String clienteId) {
        return ResponseEntity.ok(carritoService.listarPorCliente(clienteId));
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregar(@RequestBody CarritoItem item) {
        try {
            CarritoItem guardado = carritoService.guardarItem(item);
            return ResponseEntity.ok(guardado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        carritoService.eliminarItem(id);
        return ResponseEntity.noContent().build();
    }
}