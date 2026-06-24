package com.innovatech.equipos_service.dto;

import com.innovatech.equipos_service.model.RolEquipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MiembroEquipoRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "El apellido paterno es obligatorio")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        String apellidoMaterno,

        @Email(message = "El email no tiene un formato válido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotNull(message = "El rol es obligatorio")
        RolEquipo rol,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password
) {
}
