package com.innovatech.equipos_service.dto;

import java.time.LocalDate;

public record AsignacionProyectoResponse(
        Long id,
        Long idProyecto,
        String nombreProyecto,
        Long idMiembro,
        String nombreMiembro,
        String rolMiembro,
        LocalDate fechaAsignacion
) {
}