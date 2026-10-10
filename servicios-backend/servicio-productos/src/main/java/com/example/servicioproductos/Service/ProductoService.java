package com.example.servicioproductos.Service;

import com.example.servicioproductos.Model.Producto;
import com.example.servicioproductos.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // CAMBIO: Inyectamos RabbitTemplate en lugar de KafkaTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public List<Producto> listar(){
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> listarPorVendedor(String vendedorId) {
        return productoRepository.findByVendedorId(vendedorId);
    }

    public Producto guardar(Producto producto){
        if (producto.getPrecio() < 0) throw new RuntimeException("El precio no puede ser negativo");
        if (producto.getStock() < 0) throw new RuntimeException("El stock no puede ser negativo");
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        Producto guardado = productoRepository.save(producto);

        publicarEventoStock(guardado.getId(), guardado.getStock());

        return guardado;

    }

    public Producto obtenerPorId(Long id){
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void eliminar(Long id){
        Producto p = obtenerPorId(id);
        p.setActivo(false);
        productoRepository.save(p);
    }

    public Producto cambiarEstadoActivo(Long id, boolean estado){
        Producto p = obtenerPorId(id);
        p.setActivo(estado);
        return productoRepository.save(p);
    }

    public Producto restarStock(Long id, int cantidad) {
        Producto producto = obtenerPorId(id);
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente en bodega para el producto: " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - cantidad);
        Producto guardado = productoRepository.save(producto);

        publicarEventoStock(guardado.getId(), guardado.getStock());

        return guardado;
    }

    private void publicarEventoStock(Long productoId, Integer stock){
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> evento = new HashMap<>();
            evento.put("productoId", productoId);
            evento.put("stock", stock);

            String json = mapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend("cola.stock.productos", json);
            System.out.println("Evento de stock enviado a RabbitMQ: "+ json);
        } catch (Exception e){
            System.out.println("Error al enviar evento a RabbitMQ: " + e.getMessage());
        }
    }
}