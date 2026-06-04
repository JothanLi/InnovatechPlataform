package com.innovatech.equipos_service.dto;

import com.innovatech.equipos_service.model.RolEquipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MiembroEquipoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Email(message = "El email no tiene un formato válido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotNull(message = "El rol es obligatorio")
        RolEquipo rol
) {
}