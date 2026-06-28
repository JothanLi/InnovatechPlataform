package com.innovatech.proyectos_service.dto;

import com.innovatech.proyectos_service.model.EstadoProyecto;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private EstadoProyecto estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
}