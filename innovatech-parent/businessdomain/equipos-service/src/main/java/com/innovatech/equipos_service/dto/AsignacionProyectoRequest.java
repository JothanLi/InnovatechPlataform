package com.innovatech.equipos_service.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionProyectoRequest(

        @NotNull(message = "El id del proyecto es obligatorio")
        Long idProyecto,

        @NotNull(message = "El id del miembro es obligatorio")
        Long idMiembro
) {
}