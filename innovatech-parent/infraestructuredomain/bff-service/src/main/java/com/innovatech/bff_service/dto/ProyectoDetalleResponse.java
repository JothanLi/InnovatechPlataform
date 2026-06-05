package com.innovatech.bff_service.dto;

import java.util.List;

public class ProyectoDetalleResponse {

    private ProyectoResponseDTO proyecto;
    private List<TareaResponseDTO> tareas;
    private List<AsignacionProyectoResponse> miembrosAsignados;
    private AvanceProyectoResponse avance;

    public ProyectoDetalleResponse() {
    }

    public ProyectoDetalleResponse(
            ProyectoResponseDTO proyecto,
            List<TareaResponseDTO> tareas,
            List<AsignacionProyectoResponse> miembrosAsignados,
            AvanceProyectoResponse avance
    ) {
        this.proyecto = proyecto;
        this.tareas = tareas;
        this.miembrosAsignados = miembrosAsignados;
        this.avance = avance;
    }

    public static ProyectoDetalleResponseBuilder builder() {
        return new ProyectoDetalleResponseBuilder();
    }

    public ProyectoResponseDTO getProyecto() {
        return proyecto;
    }

    public void setProyecto(ProyectoResponseDTO proyecto) {
        this.proyecto = proyecto;
    }

    public List<TareaResponseDTO> getTareas() {
        return tareas;
    }

    public void setTareas(List<TareaResponseDTO> tareas) {
        this.tareas = tareas;
    }

    public List<AsignacionProyectoResponse> getMiembrosAsignados() {
        return miembrosAsignados;
    }

    public void setMiembrosAsignados(List<AsignacionProyectoResponse> miembrosAsignados) {
        this.miembrosAsignados = miembrosAsignados;
    }

    public AvanceProyectoResponse getAvance() {
        return avance;
    }

    public void setAvance(AvanceProyectoResponse avance) {
        this.avance = avance;
    }

    public static class ProyectoDetalleResponseBuilder {

        private ProyectoResponseDTO proyecto;
        private List<TareaResponseDTO> tareas;
        private List<AsignacionProyectoResponse> miembrosAsignados;
        private AvanceProyectoResponse avance;

        public ProyectoDetalleResponseBuilder proyecto(ProyectoResponseDTO proyecto) {
            this.proyecto = proyecto;
            return this;
        }

        public ProyectoDetalleResponseBuilder tareas(List<TareaResponseDTO> tareas) {
            this.tareas = tareas;
            return this;
        }

        public ProyectoDetalleResponseBuilder miembrosAsignados(List<AsignacionProyectoResponse> miembrosAsignados) {
            this.miembrosAsignados = miembrosAsignados;
            return this;
        }

        public ProyectoDetalleResponseBuilder avance(AvanceProyectoResponse avance) {
            this.avance = avance;
            return this;
        }

        public ProyectoDetalleResponse build() {
            return new ProyectoDetalleResponse(
                    proyecto,
                    tareas,
                    miembrosAsignados,
                    avance
            );
        }
    }
}