package com.gamebakes.servicio_resenas.repository;

import com.gamebakes.servicio_resenas.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    //Para que los clientes vean las reseñas de un producto
    List<Resena> findByProductoId(Long productoId);

    // Para que el vendedor recupere todas las reseñas de sus productos
    List<Resena> findByVendedorId(String vendedorId);

    // Para que el cliente vea todas sus reseñas
    List<Resena> findByClienteId(String clienteId);

}