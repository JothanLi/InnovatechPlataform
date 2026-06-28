package com.innovatech.tareas_service.dto;

import com.innovatech.tareas_service.model.EstadoTarea;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaResponseDTO {

    private Long id;
    private String descripcion;
    private EstadoTarea estado;
    private Long idProyecto;
    private String nombreProyecto;
    private String responsable;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
}