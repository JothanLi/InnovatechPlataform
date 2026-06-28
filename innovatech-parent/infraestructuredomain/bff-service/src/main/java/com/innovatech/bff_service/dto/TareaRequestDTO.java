package com.innovatech.bff_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TareaRequestDTO(
        @NotBlank(message = "La descripción de la tarea es obligatoria")
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "El estado de la tarea es obligatorio")
        String estado,

        @NotNull(message = "El ID del proyecto es obligatorio")
        Long idProyecto,

        @NotBlank(message = "El responsable de la tarea es obligatorio")
        @Size(max = 120, message = "El responsable no puede superar los 120 caracteres")
        String responsable,

        LocalDate fechaInicio,
        LocalDate fechaFinEstimada
) {
}
