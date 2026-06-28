package com.innovatech.bff_service.dto;

public class AvanceProyectoResponse {

    private Long idProyecto;
    private String nombreProyecto;
    private int totalTareas;
    private int tareasPendientes;
    private int tareasEnProgreso;
    private int tareasTerminadas;
    private double porcentajeAvance;

    public AvanceProyectoResponse() {
    }

    public AvanceProyectoResponse(
            Long idProyecto,
            String nombreProyecto,
            int totalTareas,
            int tareasPendientes,
            int tareasEnProgreso,
            int tareasTerminadas,
            double porcentajeAvance
    ) {
        this.idProyecto = idProyecto;
        this.nombreProyecto = nombreProyecto;
        this.totalTareas = totalTareas;
        this.tareasPendientes = tareasPendientes;
        this.tareasEnProgreso = tareasEnProgreso;
        this.tareasTerminadas = tareasTerminadas;
        this.porcentajeAvance = porcentajeAvance;
    }

    public static AvanceProyectoResponseBuilder builder() {
        return new AvanceProyectoResponseBuilder();
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public int getTotalTareas() {
        return totalTareas;
    }

    public void setTotalTareas(int totalTareas) {
        this.totalTareas = totalTareas;
    }

    public int getTareasPendientes() {
        return tareasPendientes;
    }

    public void setTareasPendientes(int tareasPendientes) {
        this.tareasPendientes = tareasPendientes;
    }

    public int getTareasEnProgreso() {
        return tareasEnProgreso;
    }

    public void setTareasEnProgreso(int tareasEnProgreso) {
        this.tareasEnProgreso = tareasEnProgreso;
    }

    public int getTareasTerminadas() {
        return tareasTerminadas;
    }

    public void setTareasTerminadas(int tareasTerminadas) {
        this.tareasTerminadas = tareasTerminadas;
    }

    public double getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(double porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public static class AvanceProyectoResponseBuilder {

        private Long idProyecto;
        private String nombreProyecto;
        private int totalTareas;
        private int tareasPendientes;
        private int tareasEnProgreso;
        private int tareasTerminadas;
        private double porcentajeAvance;

        public AvanceProyectoResponseBuilder idProyecto(Long idProyecto) {
            this.idProyecto = idProyecto;
            return this;
        }

        public AvanceProyectoResponseBuilder nombreProyecto(String nombreProyecto) {
            this.nombreProyecto = nombreProyecto;
            return this;
        }

        public AvanceProyectoResponseBuilder totalTareas(int totalTareas) {
            this.totalTareas = totalTareas;
            return this;
        }

        public AvanceProyectoResponseBuilder tareasPendientes(int tareasPendientes) {
            this.tareasPendientes = tareasPendientes;
            return this;
        }

        public AvanceProyectoResponseBuilder tareasEnProgreso(int tareasEnProgreso) {
            this.tareasEnProgreso = tareasEnProgreso;
            return this;
        }

        public AvanceProyectoResponseBuilder tareasTerminadas(int tareasTerminadas) {
            this.tareasTerminadas = tareasTerminadas;
            return this;
        }

        public AvanceProyectoResponseBuilder porcentajeAvance(double porcentajeAvance) {
            this.porcentajeAvance = porcentajeAvance;
            return this;
        }

        public AvanceProyectoResponse build() {
            return new AvanceProyectoResponse(
                    idProyecto,
                    nombreProyecto,
                    totalTareas,
                    tareasPendientes,
                    tareasEnProgreso,
                    tareasTerminadas,
                    porcentajeAvance
            );
        }
    }
}