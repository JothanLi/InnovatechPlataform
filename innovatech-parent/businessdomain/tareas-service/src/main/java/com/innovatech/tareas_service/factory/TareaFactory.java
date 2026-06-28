package com.innovatech.tareas_service.factory;

import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.model.Tarea;

public final class TareaFactory {

    private TareaFactory() {
    }

    public static Tarea crearDesdeRequest(TareaRequestDTO request) {
        return Tarea.builder()
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .idProyecto(request.getIdProyecto())
                .responsable(request.getResponsable())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();
    }

    public static TareaResponseDTO crearResponse(Tarea tarea, String nombreProyecto) {
        return TareaResponseDTO.builder()
                .id(tarea.getId())
                .descripcion(tarea.getDescripcion())
                .estado(tarea.getEstado())
                .idProyecto(tarea.getIdProyecto())
                .nombreProyecto(nombreProyecto)
                .responsable(tarea.getResponsable())
                .fechaInicio(tarea.getFechaInicio())
                .fechaFinEstimada(tarea.getFechaFinEstimada())
                .build();
    }
}
