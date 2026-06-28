package com.innovatech.equipos_service.dto;

import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.RolEquipo;

public record MiembroAuthResponse(
        Long id,
        String email,
        String passwordHash,
        RolEquipo rol,
        EstadoMiembro estado
) {
}
