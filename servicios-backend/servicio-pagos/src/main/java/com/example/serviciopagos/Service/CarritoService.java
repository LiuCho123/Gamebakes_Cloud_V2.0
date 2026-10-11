package com.example.serviciopagos.Service;

import com.example.serviciopagos.Model.CarritoItem;
import com.example.serviciopagos.Model.ProductoStockCache;
import com.example.serviciopagos.Repository.CarritoItemRepository;
import com.example.serviciopagos.Repository.ProductoStockCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CarritoService {

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ProductoStockCacheRepository stockCacheRepository;

    public List<CarritoItem> listarPorCliente(String clienteId) {
        return carritoItemRepository.findByClienteId(clienteId);
    }

    public CarritoItem guardarItem(CarritoItem item) {
        ProductoStockCache stockCache = stockCacheRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el catalogo"));

        if (stockCache.getStockDisponible() <= 0){
            throw new RuntimeException("Producto agotado");
        }
        if (stockCache.getStockDisponible() < item.getCantidad()){
            throw new RuntimeException("No hay suficiente Stock");
        }
        return carritoItemRepository.save(item);
    }

    public void eliminarItem(Long id) {
        carritoItemRepository.deleteById(id);
    }

    @Transactional
    public void limpiarCarrito(String clienteId) {
        carritoItemRepository.deleteByClienteId(clienteId);
    }
}