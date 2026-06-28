package com.innovatech.equipos_service.factory;

import com.innovatech.equipos_service.dto.AsignacionProyectoResponse;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.model.AsignacionProyecto;
import com.innovatech.equipos_service.model.MiembroEquipo;

import java.time.LocalDate;

public final class AsignacionProyectoFactory {

    private AsignacionProyectoFactory() {
    }

    public static AsignacionProyecto crear(Long idProyecto, MiembroEquipo miembro) {
        return AsignacionProyecto.builder()
                .idProyecto(idProyecto)
                .miembro(miembro)
                .fechaAsignacion(LocalDate.now())
                .build();
    }

    public static AsignacionProyectoResponse crearResponse(
            AsignacionProyecto asignacion,
            ProyectoAdaptadoResponse proyecto
    ) {
        MiembroEquipo miembro = asignacion.getMiembro();

        return new AsignacionProyectoResponse(
                asignacion.getId(),
                asignacion.getIdProyecto(),
                proyecto.nombreProyecto(),
                miembro.getId(),
                miembro.getNombres() + " " + miembro.getApellidoPaterno() + " " + miembro.getApellidoMaterno(),
                miembro.getRol().name(),
                asignacion.getFechaAsignacion()
        );
    }
}
