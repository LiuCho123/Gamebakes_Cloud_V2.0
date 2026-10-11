package com.example.serviciopagos.Repository;

import com.example.serviciopagos.Model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
List<Pago> findByClienteId(String clienteId);
}
