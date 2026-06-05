package com.innovatech.tareas_service.dto;

import com.innovatech.tareas_service.model.EstadoTarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaRequestDTO {

    @NotBlank(message = "La descripción de la tarea es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El estado de la tarea es obligatorio")
    private EstadoTarea estado;

    @NotNull(message = "El ID del proyecto es obligatorio")
    private Long idProyecto;

    @NotBlank(message = "El responsable de la tarea es obligatorio")
    @Size(max = 120, message = "El responsable no puede superar los 120 caracteres")
    private String responsable;

    private LocalDate fechaInicio;

    private LocalDate fechaFinEstimada;
}