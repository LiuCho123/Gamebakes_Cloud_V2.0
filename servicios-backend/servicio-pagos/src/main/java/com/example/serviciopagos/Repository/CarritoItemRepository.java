package com.example.serviciopagos.Repository;

import com.example.serviciopagos.Model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
List<CarritoItem> findByClienteId(String clienteId);

    @Transactional
    void deleteByClienteId(String clienteId);
}