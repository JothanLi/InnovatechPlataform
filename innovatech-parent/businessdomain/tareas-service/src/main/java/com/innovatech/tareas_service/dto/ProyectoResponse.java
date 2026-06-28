package com.innovatech.tareas_service.dto;

public record ProyectoResponse(
        Long id,
        String nombre,
        String descripcion,
        String estado
) {
}