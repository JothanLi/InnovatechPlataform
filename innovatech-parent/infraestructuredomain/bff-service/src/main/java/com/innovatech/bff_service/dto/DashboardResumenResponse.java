package com.innovatech.bff_service.dto;

public class DashboardResumenResponse {

    private int totalProyectos;
    private int proyectosPlanificados;
    private int proyectosEnProgreso;
    private int proyectosCompletados;
    private int proyectosCancelados;

    private int totalTareas;
    private int tareasPendientes;
    private int tareasEnProgreso;
    private int tareasTerminadas;

    private int totalMiembrosAsignados;
    private double porcentajeAvanceGeneral;

    public DashboardResumenResponse() {
    }

    public DashboardResumenResponse(
            int totalProyectos,
            int proyectosPlanificados,
            int proyectosEnProgreso,
            int proyectosCompletados,
            int proyectosCancelados,
            int totalTareas,
            int tareasPendientes,
            int tareasEnProgreso,
            int tareasTerminadas,
            int totalMiembrosAsignados,
            double porcentajeAvanceGeneral
    ) {
        this.totalProyectos = totalProyectos;
        this.proyectosPlanificados = proyectosPlanificados;
        this.proyectosEnProgreso = proyectosEnProgreso;
        this.proyectosCompletados = proyectosCompletados;
        this.proyectosCancelados = proyectosCancelados;
        this.totalTareas = totalTareas;
        this.tareasPendientes = tareasPendientes;
        this.tareasEnProgreso = tareasEnProgreso;
        this.tareasTerminadas = tareasTerminadas;
        this.totalMiembrosAsignados = totalMiembrosAsignados;
        this.porcentajeAvanceGeneral = porcentajeAvanceGeneral;
    }

    public static DashboardResumenResponseBuilder builder() {
        return new DashboardResumenResponseBuilder();
    }

    public int getTotalProyectos() {
        return totalProyectos;
    }

    public void setTotalProyectos(int totalProyectos) {
        this.totalProyectos = totalProyectos;
    }

    public int getProyectosPlanificados() {
        return proyectosPlanificados;
    }

    public void setProyectosPlanificados(int proyectosPlanificados) {
        this.proyectosPlanificados = proyectosPlanificados;
    }

    public int getProyectosEnProgreso() {
        return proyectosEnProgreso;
    }

    public void setProyectosEnProgreso(int proyectosEnProgreso) {
        this.proyectosEnProgreso = proyectosEnProgreso;
    }

    public int getProyectosCompletados() {
        return proyectosCompletados;
    }

    public void setProyectosCompletados(int proyectosCompletados) {
        this.proyectosCompletados = proyectosCompletados;
    }

    public int getProyectosCancelados() {
        return proyectosCancelados;
    }

    public void setProyectosCancelados(int proyectosCancelados) {
        this.proyectosCancelados = proyectosCancelados;
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

    public int getTotalMiembrosAsignados() {
        return totalMiembrosAsignados;
    }

    public void setTotalMiembrosAsignados(int totalMiembrosAsignados) {
        this.totalMiembrosAsignados = totalMiembrosAsignados;
    }

    public double getPorcentajeAvanceGeneral() {
        return porcentajeAvanceGeneral;
    }

    public void setPorcentajeAvanceGeneral(double porcentajeAvanceGeneral) {
        this.porcentajeAvanceGeneral = porcentajeAvanceGeneral;
    }

    public static class DashboardResumenResponseBuilder {

        private int totalProyectos;
        private int proyectosPlanificados;
        private int proyectosEnProgreso;
        private int proyectosCompletados;
        private int proyectosCancelados;

        private int totalTareas;
        private int tareasPendientes;
        private int tareasEnProgreso;
        private int tareasTerminadas;

        private int totalMiembrosAsignados;
        private double porcentajeAvanceGeneral;

        public DashboardResumenResponseBuilder totalProyectos(int totalProyectos) {
            this.totalProyectos = totalProyectos;
            return this;
        }

        public DashboardResumenResponseBuilder proyectosPlanificados(int proyectosPlanificados) {
            this.proyectosPlanificados = proyectosPlanificados;
            return this;
        }

        public DashboardResumenResponseBuilder proyectosEnProgreso(int proyectosEnProgreso) {
            this.proyectosEnProgreso = proyectosEnProgreso;
            return this;
        }

        public DashboardResumenResponseBuilder proyectosCompletados(int proyectosCompletados) {
            this.proyectosCompletados = proyectosCompletados;
            return this;
        }

        public DashboardResumenResponseBuilder proyectosCancelados(int proyectosCancelados) {
            this.proyectosCancelados = proyectosCancelados;
            return this;
        }

        public DashboardResumenResponseBuilder totalTareas(int totalTareas) {
            this.totalTareas = totalTareas;
            return this;
        }

        public DashboardResumenResponseBuilder tareasPendientes(int tareasPendientes) {
            this.tareasPendientes = tareasPendientes;
            return this;
        }

        public DashboardResumenResponseBuilder tareasEnProgreso(int tareasEnProgreso) {
            this.tareasEnProgreso = tareasEnProgreso;
            return this;
        }

        public DashboardResumenResponseBuilder tareasTerminadas(int tareasTerminadas) {
            this.tareasTerminadas = tareasTerminadas;
            return this;
        }

        public DashboardResumenResponseBuilder totalMiembrosAsignados(int totalMiembrosAsignados) {
            this.totalMiembrosAsignados = totalMiembrosAsignados;
            return this;
        }

        public DashboardResumenResponseBuilder porcentajeAvanceGeneral(double porcentajeAvanceGeneral) {
            this.porcentajeAvanceGeneral = porcentajeAvanceGeneral;
            return this;
        }

        public DashboardResumenResponse build() {
            return new DashboardResumenResponse(
                    totalProyectos,
                    proyectosPlanificados,
                    proyectosEnProgreso,
                    proyectosCompletados,
                    proyectosCancelados,
                    totalTareas,
                    tareasPendientes,
                    tareasEnProgreso,
                    tareasTerminadas,
                    totalMiembrosAsignados,
                    porcentajeAvanceGeneral
            );
        }
    }
}