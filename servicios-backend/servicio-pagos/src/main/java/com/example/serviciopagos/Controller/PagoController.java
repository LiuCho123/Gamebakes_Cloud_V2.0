package com.example.serviciopagos.Controller;

import com.example.serviciopagos.DTO.SolicitudPagoDTO;
import com.example.serviciopagos.Model.Pago;
import com.example.serviciopagos.Service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestBody SolicitudPagoDTO solicitud,
                                     @RequestHeader("X-User-Id") String usuarioId){
        try{
            solicitud.setClienteId(usuarioId);
            return ResponseEntity.ok(pagoService.iniciarPagoMP(solicitud));
        } catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirmar/{idPago}")
    public ResponseEntity<Pago> confirmar(@PathVariable Long idPago,
                                          @RequestHeader("X-User-Id") String usuarioId,
                                          @RequestHeader("Authorization") String token,
                                          @RequestHeader(value = "X-User-Name", required = false) String nombreUsuarioStr) {
        String nombreFinal = (nombreUsuarioStr != null) ? nombreUsuarioStr : "Cliente";

        return ResponseEntity.ok(pagoService.confirmarPago(idPago, usuarioId, token, nombreFinal));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<Pago>> historialPropio(@RequestHeader("X-User-Id") String usuarioId) {
        return ResponseEntity.ok(pagoService.obtenerHistorialPorCliente(usuarioId));
    }

    @PostMapping("/iniciar-desde-carrito/{clienteId}")
    public ResponseEntity<?> iniciarDesdeCarrito(@PathVariable String clienteId) {
        try {
            return ResponseEntity.ok(pagoService.iniciarPagoCarrito(clienteId));
        } catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}