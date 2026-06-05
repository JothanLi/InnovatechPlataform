package com.innovatech.bff_service.facade;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.client.ProyectoClient;
import com.innovatech.bff_service.client.TareaClient;
import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InnovatechBffFacade {

    private final ProyectoClient proyectoClient;
    private final TareaClient tareaClient;
    private final EquipoClient equipoClient;

    public InnovatechBffFacade(
            ProyectoClient proyectoClient,
            TareaClient tareaClient,
            EquipoClient equipoClient
    ) {
        this.proyectoClient = proyectoClient;
        this.tareaClient = tareaClient;
        this.equipoClient = equipoClient;
    }

    public List<ProyectoResponseDTO> listarProyectos() {
        return proyectoClient.listarProyectos();
    }

    public ProyectoDetalleResponse obtenerDetalleProyecto(Long idProyecto) {
        ProyectoResponseDTO proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        List<TareaResponseDTO> tareas = tareaClient.listarTareasPorProyecto(idProyecto);
        List<AsignacionProyectoResponse> miembros = equipoClient.listarMiembrosPorProyecto(idProyecto);
        AvanceProyectoResponse avance = calcularAvance(proyecto, tareas);

        return ProyectoDetalleResponse.builder()
                .proyecto(proyecto)
                .tareas(tareas)
                .miembrosAsignados(miembros)
                .avance(avance)
                .build();
    }

    public List<TareaResponseDTO> obtenerTareasPorProyecto(Long idProyecto) {
        return tareaClient.listarTareasPorProyecto(idProyecto);
    }

    public List<AsignacionProyectoResponse> obtenerMiembrosPorProyecto(Long idProyecto) {
        return equipoClient.listarMiembrosPorProyecto(idProyecto);
    }

    public AvanceProyectoResponse obtenerAvanceProyecto(Long idProyecto) {
        ProyectoResponseDTO proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        List<TareaResponseDTO> tareas = tareaClient.listarTareasPorProyecto(idProyecto);

        return calcularAvance(proyecto, tareas);
    }

    private AvanceProyectoResponse calcularAvance(
            ProyectoResponseDTO proyecto,
            List<TareaResponseDTO> tareas
    ) {
        int total = tareas.size();

        int pendientes = (int) tareas.stream()
                .filter(tarea -> "PENDING".equalsIgnoreCase(tarea.getEstado()))
                .count();

        int enProgreso = (int) tareas.stream()
                .filter(tarea -> "IN_PROGRESS".equalsIgnoreCase(tarea.getEstado()))
                .count();

        int terminadas = (int) tareas.stream()
                .filter(tarea -> "DONE".equalsIgnoreCase(tarea.getEstado()))
                .count();

        double porcentajeAvance = total == 0 ? 0 : (terminadas * 100.0) / total;

        return AvanceProyectoResponse.builder()
                .idProyecto(proyecto.getId())
                .nombreProyecto(proyecto.getNombre())
                .totalTareas(total)
                .tareasPendientes(pendientes)
                .tareasEnProgreso(enProgreso)
                .tareasTerminadas(terminadas)
                .porcentajeAvance(Math.round(porcentajeAvance * 100.0) / 100.0)
                .build();
    }
}