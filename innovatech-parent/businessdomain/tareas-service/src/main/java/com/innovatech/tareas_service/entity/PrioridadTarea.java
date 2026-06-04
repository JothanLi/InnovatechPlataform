package com.innovatech.tareas_service.entity;

public enum PrioridadTarea {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    URGENTE("Urgente");

    private final String descripcion;

    PrioridadTarea(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
