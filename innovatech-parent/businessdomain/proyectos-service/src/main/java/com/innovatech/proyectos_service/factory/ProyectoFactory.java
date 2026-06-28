package com.innovatech.proyectos_service.factory;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.model.Proyecto;

public final class ProyectoFactory {

    private ProyectoFactory() {
    }

    public static Proyecto crearDesdeRequest(ProyectoRequestDTO request) {
        return Proyecto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();
    }

    public static ProyectoResponseDTO crearResponse(Proyecto proyecto) {
        return ProyectoResponseDTO.builder()
                .id(proyecto.getId())
                .nombre(proyecto.getNombre())
                .descripcion(proyecto.getDescripcion())
                .estado(proyecto.getEstado())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                .build();
    }
}
