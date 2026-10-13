package com.gamebakes.servicio_perfil.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "perfiles")
@Data
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerfil;

    @Column(name = "id_usuario", unique = true, nullable = false)
    private String usuarioId;

    private String nombreCompleto;
    
    private String telefono;
    
    private String direccion; 
}