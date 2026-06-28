package com.innovatech.bff_service.dto;

import java.time.LocalDate;

public class ProyectoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    public ProyectoResponseDTO() {
    }

    public ProyectoResponseDTO(
            Long id,
            String nombre,
            String descripcion,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFinEstimada
    ) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public static ProyectoResponseDTOBuilder builder() {
        return new ProyectoResponseDTOBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public static class ProyectoResponseDTOBuilder {

        private Long id;
        private String nombre;
        private String descripcion;
        private String estado;
        private LocalDate fechaInicio;
        private LocalDate fechaFinEstimada;

        public ProyectoResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProyectoResponseDTOBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public ProyectoResponseDTOBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ProyectoResponseDTOBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public ProyectoResponseDTOBuilder fechaInicio(LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public ProyectoResponseDTOBuilder fechaFinEstimada(LocalDate fechaFinEstimada) {
            this.fechaFinEstimada = fechaFinEstimada;
            return this;
        }

        public ProyectoResponseDTO build() {
            return new ProyectoResponseDTO(
                    id,
                    nombre,
                    descripcion,
                    estado,
                    fechaInicio,
                    fechaFinEstimada
            );
        }
    }
}