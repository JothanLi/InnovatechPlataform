package com.innovatech.bff_service.dto;

import java.time.LocalDate;

public class TareaResponseDTO {

    private Long id;
    private String descripcion;
    private String estado;
    private Long idProyecto;
    private String nombreProyecto;
    private String responsable;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    public TareaResponseDTO() {
    }

    public TareaResponseDTO(
            Long id,
            String descripcion,
            String estado,
            Long idProyecto,
            String nombreProyecto,
            String responsable,
            LocalDate fechaInicio,
            LocalDate fechaFinEstimada
    ) {
        this.id = id;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idProyecto = idProyecto;
        this.nombreProyecto = nombreProyecto;
        this.responsable = responsable;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public static TareaResponseDTOBuilder builder() {
        return new TareaResponseDTOBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public String getResponsable() {
        return responsable;
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

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public static class TareaResponseDTOBuilder {

        private Long id;
        private String descripcion;
        private String estado;
        private Long idProyecto;
        private String nombreProyecto;
        private String responsable;
        private LocalDate fechaInicio;
        private LocalDate fechaFinEstimada;

        public TareaResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TareaResponseDTOBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public TareaResponseDTOBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public TareaResponseDTOBuilder idProyecto(Long idProyecto) {
            this.idProyecto = idProyecto;
            return this;
        }

        public TareaResponseDTOBuilder nombreProyecto(String nombreProyecto) {
            this.nombreProyecto = nombreProyecto;
            return this;
        }

        public TareaResponseDTOBuilder responsable(String responsable) {
            this.responsable = responsable;
            return this;
        }

        public TareaResponseDTOBuilder fechaInicio(LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public TareaResponseDTOBuilder fechaFinEstimada(LocalDate fechaFinEstimada) {
            this.fechaFinEstimada = fechaFinEstimada;
            return this;
        }

        public TareaResponseDTO build() {
            return new TareaResponseDTO(
                    id,
                    descripcion,
                    estado,
                    idProyecto,
                    nombreProyecto,
                    responsable,
                    fechaInicio,
                    fechaFinEstimada
            );
        }
    }
}