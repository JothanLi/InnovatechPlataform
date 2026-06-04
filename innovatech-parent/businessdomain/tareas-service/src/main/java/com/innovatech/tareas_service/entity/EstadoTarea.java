package com.innovatech.tareas_service.entity;

public enum EstadoTarea {
    PENDIENTE("Pendiente"),
    EN_PROGRESO("En Progreso"),
    EN_REVISION("En Revisión"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoTarea(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
