package com.innovatech.equipos_service.dto;

public record ProyectoResponse(
        Long id,
        String nombre,
        String descripcion,
        String estado,
        String tipo
) {
}