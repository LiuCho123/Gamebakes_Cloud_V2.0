package com.example.servicioproductos.Controller;

import com.example.servicioproductos.Model.Producto;
import com.example.servicioproductos.Service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listar(){
        return ResponseEntity.ok(productoService.listar());
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<Producto>> listarPorVendedor(
            @PathVariable String vendedorId,
            @RequestHeader(value = "X-User-Role", required = false) String rol){

        System.out.println("LISTANDO PRODUCTOS. Rol recibido: " + rol + " | VendedorId: " + vendedorId);
        return ResponseEntity.ok(productoService.listarPorVendedor(vendedorId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto,
                                   @RequestHeader(value = "X-User-Id", required = false) String vendedorIdStr,
                                   @RequestHeader(value = "X-User-Role", required = false) String rol) {

        System.out.println("CREANDO PRODUCTO. Rol recibido: " + rol + " | VendedorId: " + vendedorIdStr);

        if (vendedorIdStr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        try {
            producto.setVendedorId(vendedorIdStr);
            return ResponseEntity.ok(productoService.guardar(producto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el producto: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId (@PathVariable Long id){
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Role", required = false) String rol){

        if (rol == null || !"VENDEDOR".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo los vendedores pueden eliminar.");
        }

        try {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestParam boolean activo,
                                           @RequestHeader(value = "X-User-Role", required = false) String rol){
        if (rol == null || !"VENDEDOR".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        try {
            return ResponseEntity.ok(productoService.cambiarEstadoActivo(id, activo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar el estado: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Producto producto,
                                        @RequestHeader(value = "X-User-Id", required = false) String vendedorIdStr,
                                        @RequestHeader(value = "X-User-Role", required = false) String rol) {

        if (rol == null || !"VENDEDOR".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        if (vendedorIdStr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        try {
            producto.setId(id);
            producto.setVendedorId(vendedorIdStr);
            return ResponseEntity.ok(productoService.guardar(producto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/restar-stock")
    public ResponseEntity<?> restarStock(@PathVariable Long id,
                                         @RequestParam int cantidad) {
        try {
            return ResponseEntity.ok(productoService.restarStock(id, cantidad));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }
}