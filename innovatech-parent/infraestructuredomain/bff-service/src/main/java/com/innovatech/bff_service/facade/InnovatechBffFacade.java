package com.innovatech.bff_service.facade;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.client.ProyectoClient;
import com.innovatech.bff_service.client.TareaClient;
import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AsignacionProyectoRequest;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.DashboardResumenResponse;
import com.innovatech.bff_service.dto.MiembroEquipoRequest;
import com.innovatech.bff_service.dto.MiembroEquipoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoRequestDTO;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaRequestDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public ProyectoResponseDTO crearProyecto(ProyectoRequestDTO request) {
        return proyectoClient.crearProyecto(request);
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

    public TareaResponseDTO crearTarea(TareaRequestDTO request) {
        return tareaClient.crearTarea(request);
    }

    public TareaResponseDTO cambiarEstadoTarea(Long idTarea, String estado) {
        return tareaClient.cambiarEstadoTarea(
                idTarea,
                new TareaClient.CambioEstadoTareaRequest(estado)
        );
    }

    public List<MiembroEquipoResponse> listarMiembros() {
        return equipoClient.listarMiembros();
    }

    public MiembroEquipoResponse crearMiembro(MiembroEquipoRequest request) {
        return equipoClient.crearMiembro(request);
    }

    public List<AsignacionProyectoResponse> obtenerMiembrosPorProyecto(Long idProyecto) {
        return equipoClient.listarMiembrosPorProyecto(idProyecto);
    }

    public AsignacionProyectoResponse asignarMiembroAProyecto(AsignacionProyectoRequest request) {
        return equipoClient.asignarMiembroAProyecto(request);
    }

    public AvanceProyectoResponse obtenerAvanceProyecto(Long idProyecto) {
        ProyectoResponseDTO proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        List<TareaResponseDTO> tareas = tareaClient.listarTareasPorProyecto(idProyecto);

        return calcularAvance(proyecto, tareas);
    }

    public DashboardResumenResponse obtenerDashboardResumen() {
        List<ProyectoResponseDTO> proyectos;

        try {
            proyectos = proyectoClient.listarProyectos();

            if (proyectos == null) {
                proyectos = new ArrayList<>();
            }
        } catch (Exception exception) {
            System.err.println("No se pudieron cargar los proyectos para el dashboard: " + exception.getMessage());
            proyectos = new ArrayList<>();
        }

        int totalProyectos = proyectos.size();

        int proyectosPlanificados = contarProyectosPorEstado(proyectos, "PLANNED");
        int proyectosEnProgreso = contarProyectosPorEstado(proyectos, "IN_PROGRESS");
        int proyectosCompletados = contarProyectosPorEstado(proyectos, "COMPLETED");
        int proyectosCancelados = contarProyectosPorEstado(proyectos, "CANCELLED");

        List<TareaResponseDTO> todasLasTareas = new ArrayList<>();
        List<AsignacionProyectoResponse> todasLasAsignaciones = new ArrayList<>();

        for (ProyectoResponseDTO proyecto : proyectos) {
            if (proyecto == null || proyecto.getId() == null) {
                continue;
            }

            Long idProyecto = proyecto.getId();

            try {
                List<TareaResponseDTO> tareasProyecto = tareaClient.listarTareasPorProyecto(idProyecto);

                if (tareasProyecto != null) {
                    todasLasTareas.addAll(tareasProyecto);
                }
            } catch (Exception exception) {
                System.err.println("No se pudieron cargar tareas del proyecto " + idProyecto + ": " + exception.getMessage());
            }

            try {
                List<AsignacionProyectoResponse> asignacionesProyecto = equipoClient.listarMiembrosPorProyecto(idProyecto);

                if (asignacionesProyecto != null) {
                    todasLasAsignaciones.addAll(asignacionesProyecto);
                }
            } catch (Exception exception) {
                System.err.println("No se pudieron cargar asignaciones del proyecto " + idProyecto + ": " + exception.getMessage());
            }
        }

        int totalTareas = todasLasTareas.size();
        int tareasPendientes = contarTareasPorEstado(todasLasTareas, "PENDING");
        int tareasEnProgreso = contarTareasPorEstado(todasLasTareas, "IN_PROGRESS");
        int tareasTerminadas = contarTareasPorEstado(todasLasTareas, "DONE");

        int totalMiembrosAsignados = (int) todasLasAsignaciones.stream()
                .map(AsignacionProyectoResponse::getIdMiembro)
                .filter(idMiembro -> idMiembro != null)
                .distinct()
                .count();

        double porcentajeAvanceGeneral = totalTareas == 0
                ? 0
                : (tareasTerminadas * 100.0) / totalTareas;

        return DashboardResumenResponse.builder()
                .totalProyectos(totalProyectos)
                .proyectosPlanificados(proyectosPlanificados)
                .proyectosEnProgreso(proyectosEnProgreso)
                .proyectosCompletados(proyectosCompletados)
                .proyectosCancelados(proyectosCancelados)
                .totalTareas(totalTareas)
                .tareasPendientes(tareasPendientes)
                .tareasEnProgreso(tareasEnProgreso)
                .tareasTerminadas(tareasTerminadas)
                .totalMiembrosAsignados(totalMiembrosAsignados)
                .porcentajeAvanceGeneral(redondearDosDecimales(porcentajeAvanceGeneral))
                .build();
    }
    private AvanceProyectoResponse calcularAvance(
            ProyectoResponseDTO proyecto,
            List<TareaResponseDTO> tareas
    ) {
        int total = tareas.size();

        int pendientes = contarTareasPorEstado(tareas, "PENDING");
        int enProgreso = contarTareasPorEstado(tareas, "IN_PROGRESS");
        int terminadas = contarTareasPorEstado(tareas, "DONE");

        double porcentajeAvance = total == 0 ? 0 : (terminadas * 100.0) / total;

        return AvanceProyectoResponse.builder()
                .idProyecto(proyecto.getId())
                .nombreProyecto(proyecto.getNombre())
                .totalTareas(total)
                .tareasPendientes(pendientes)
                .tareasEnProgreso(enProgreso)
                .tareasTerminadas(terminadas)
                .porcentajeAvance(redondearDosDecimales(porcentajeAvance))
                .build();
    }

    private int contarProyectosPorEstado(
            List<ProyectoResponseDTO> proyectos,
            String estado
    ) {
        return (int) proyectos.stream()
                .filter(proyecto -> estado.equalsIgnoreCase(proyecto.getEstado()))
                .count();
    }

    private int contarTareasPorEstado(
            List<TareaResponseDTO> tareas,
            String estado
    ) {
        return (int) tareas.stream()
                .filter(tarea -> estado.equalsIgnoreCase(tarea.getEstado()))
                .count();
    }

    private double redondearDosDecimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
