package com.innovatech.equipos_service.dto;

import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.RolEquipo;

public record MiembroEquipoResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        RolEquipo rol,
        EstadoMiembro estado
) {
}