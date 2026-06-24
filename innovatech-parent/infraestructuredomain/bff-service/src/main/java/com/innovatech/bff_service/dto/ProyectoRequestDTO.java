package com.innovatech.bff_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProyectoRequestDTO(
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @NotBlank(message = "La descripción del proyecto es obligatoria")
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "El estado del proyecto es obligatorio")
        String estado,

        LocalDate fechaInicio,
        LocalDate fechaFinEstimada
) {
}
