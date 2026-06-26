package com.innovatech.bff_service.facade;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.client.ProyectoClient;
import com.innovatech.bff_service.client.TareaClient;
import com.innovatech.bff_service.dto.AsignacionProyectoRequest;
import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.DashboardResumenResponse;
import com.innovatech.bff_service.dto.MiembroEquipoRequest;
import com.innovatech.bff_service.dto.MiembroEquipoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoRequestDTO;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaRequestDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import com.innovatech.bff_service.security.CurrentUserService;
import com.innovatech.bff_service.security.CurrentUserService.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InnovatechBffFacade {

    private final ProyectoClient proyectoClient;
    private final TareaClient tareaClient;
    private final EquipoClient equipoClient;
    private final CurrentUserService currentUserService;

    public InnovatechBffFacade(
            ProyectoClient proyectoClient,
            TareaClient tareaClient,
            EquipoClient equipoClient,
            CurrentUserService currentUserService
    ) {
        this.proyectoClient = proyectoClient;
        this.tareaClient = tareaClient;
        this.equipoClient = equipoClient;
        this.currentUserService = currentUserService;
    }

    public List<ProyectoResponseDTO> listarProyectos() {
        CurrentUser user = currentUserService.getCurrentUser();

        try {
            return filtrarProyectosVisibles(proyectoClient.listarProyectos(), user);
        } catch (Exception exception) {
            System.err.println("No se pudieron listar proyectos desde BFF: " + exception.getMessage());
            return List.of();
        }
    }

    public ProyectoResponseDTO crearProyecto(ProyectoRequestDTO request) {
        requireAdmin();
        return proyectoClient.crearProyecto(request);
    }

    public ProyectoResponseDTO cambiarEstadoProyecto(Long idProyecto, String estado) {
        CurrentUser user = currentUserService.getCurrentUser();

        requireProjectWorkManager(user);
        requireProyectoVisible(idProyecto, user);

        String estadoNormalizado = normalizarEstadoProyecto(estado);

        return proyectoClient.cambiarEstadoProyecto(
                idProyecto,
                new ProyectoClient.CambioEstadoProyectoRequest(estadoNormalizado)
        );
    }

    public ProyectoDetalleResponse obtenerDetalleProyecto(Long idProyecto) {
        CurrentUser user = currentUserService.getCurrentUser();
        requireProyectoVisible(idProyecto, user);

        ProyectoResponseDTO proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);

        List<TareaResponseDTO> tareas = obtenerTareasSegurasPorProyecto(idProyecto);
        List<AsignacionProyectoResponse> miembros = obtenerAsignacionesSegurasPorProyecto(idProyecto);

        AvanceProyectoResponse avance = calcularAvance(proyecto, tareas);

        return ProyectoDetalleResponse.builder()
                .proyecto(proyecto)
                .tareas(tareas)
                .miembrosAsignados(miembros)
                .avance(avance)
                .build();
    }

    public List<TareaResponseDTO> obtenerTareasPorProyecto(Long idProyecto) {
        requireProyectoVisible(idProyecto, currentUserService.getCurrentUser());
        return tareaClient.listarTareasPorProyecto(idProyecto);
    }

    public TareaResponseDTO crearTarea(TareaRequestDTO request) {
        CurrentUser user = currentUserService.getCurrentUser();

        requireProjectWorkManager(user);
        requireProyectoVisible(request.idProyecto(), user);

        return tareaClient.crearTarea(request);
    }

    public TareaResponseDTO cambiarEstadoTarea(Long idTarea, String estado) {
        CurrentUser user = currentUserService.getCurrentUser();

        requireProjectWorkManager(user);

        String estadoNormalizado = normalizarEstadoTarea(estado);

        TareaResponseDTO tarea = buscarTareaPorId(idTarea);

        if (tarea == null || tarea.getIdProyecto() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la tarea solicitada");
        }

        requireProyectoVisible(tarea.getIdProyecto(), user);

        return tareaClient.cambiarEstadoTarea(
                idTarea,
                new TareaClient.CambioEstadoTareaRequest(estadoNormalizado)
        );
    }

    public List<MiembroEquipoResponse> listarMiembros() {
        CurrentUser user = currentUserService.getCurrentUser();

        if (!user.canManageProjectWork()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para listar usuarios");
        }

        return equipoClient.listarMiembros();
    }

    public MiembroEquipoResponse crearMiembro(MiembroEquipoRequest request) {
        requireAdmin();
        return equipoClient.crearMiembro(request);
    }

    public List<AsignacionProyectoResponse> obtenerMiembrosPorProyecto(Long idProyecto) {
        requireProyectoVisible(idProyecto, currentUserService.getCurrentUser());
        return equipoClient.listarMiembrosPorProyecto(idProyecto);
    }

    public AsignacionProyectoResponse asignarMiembroAProyecto(AsignacionProyectoRequest request) {
        CurrentUser user = currentUserService.getCurrentUser();

        requireProjectWorkManager(user);
        requireProyectoVisible(request.idProyecto(), user);

        return equipoClient.asignarMiembroAProyecto(request);
    }

    public AvanceProyectoResponse obtenerAvanceProyecto(Long idProyecto) {
        requireProyectoVisible(idProyecto, currentUserService.getCurrentUser());

        ProyectoResponseDTO proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        List<TareaResponseDTO> tareas = obtenerTareasSegurasPorProyecto(idProyecto);

        return calcularAvance(proyecto, tareas);
    }

    public DashboardResumenResponse obtenerDashboardResumen() {
        CurrentUser user = currentUserService.getCurrentUser();

        List<ProyectoResponseDTO> proyectos;

        try {
            proyectos = filtrarProyectosVisibles(proyectoClient.listarProyectos(), user);
        } catch (Exception exception) {
            System.err.println("No se pudieron cargar los proyectos para el dashboard: " + exception.getMessage());
            proyectos = new ArrayList<>();
        }

        if (proyectos == null) {
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

            List<TareaResponseDTO> tareasProyecto = obtenerTareasSegurasPorProyecto(idProyecto);
            todasLasTareas.addAll(tareasProyecto);

            List<AsignacionProyectoResponse> asignacionesProyecto = obtenerAsignacionesSegurasPorProyecto(idProyecto);
            todasLasAsignaciones.addAll(asignacionesProyecto);
        }

        int totalTareas = todasLasTareas.size();
        int tareasPendientes = contarTareasPorEstado(todasLasTareas, "PENDING");
        int tareasEnProgreso = contarTareasPorEstado(todasLasTareas, "IN_PROGRESS");
        int tareasTerminadas = contarTareasPorEstado(todasLasTareas, "DONE");

        int totalMiembrosAsignados = (int) todasLasAsignaciones.stream()
                .filter(asignacion -> asignacion != null)
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

    private List<ProyectoResponseDTO> filtrarProyectosVisibles(
            List<ProyectoResponseDTO> proyectos,
            CurrentUser user
    ) {
        if (proyectos == null || proyectos.isEmpty()) {
            return List.of();
        }

        if (user.isAdmin()) {
            return proyectos;
        }

        Set<Long> idsAsignadosTemp;

        try {
            idsAsignadosTemp = equipoClient.listarMiembros()
                    .stream()
                    .filter(miembro -> miembro != null)
                    .filter(miembro -> miembro.getEmail() != null)
                    .filter(miembro -> user.email().equalsIgnoreCase(miembro.getEmail()))
                    .findFirst()
                    .map(miembro -> obtenerIdsProyectosAsignados(miembro.getId()))
                    .orElse(Set.of());
        } catch (Exception exception) {
            System.err.println(
                    "No se pudieron resolver proyectos visibles para "
                            + user.email()
                            + ": "
                            + exception.getMessage()
            );
            idsAsignadosTemp = Set.of();
        }

        final Set<Long> proyectosAsignados = idsAsignadosTemp;

        return proyectos.stream()
                .filter(proyecto -> proyecto != null && proyecto.getId() != null)
                .filter(proyecto -> proyectosAsignados.contains(proyecto.getId()))
                .toList();
    }

    private Set<Long> obtenerIdsProyectosAsignados(Long idMiembro) {
        if (idMiembro == null) {
            return Set.of();
        }

        List<ProyectoResponseDTO> proyectos;

        try {
            proyectos = proyectoClient.listarProyectos();
        } catch (Exception exception) {
            System.err.println("No se pudieron cargar proyectos para resolver asignaciones: " + exception.getMessage());
            return Set.of();
        }

        if (proyectos == null || proyectos.isEmpty()) {
            return Set.of();
        }

        return proyectos.stream()
                .filter(proyecto -> proyecto != null && proyecto.getId() != null)
                .flatMap(proyecto -> {
                    try {
                        List<AsignacionProyectoResponse> asignaciones =
                                equipoClient.listarMiembrosPorProyecto(proyecto.getId());

                        if (asignaciones == null) {
                            return java.util.stream.Stream.empty();
                        }

                        return asignaciones.stream();
                    } catch (Exception exception) {
                        System.err.println(
                                "No se pudieron cargar asignaciones del proyecto "
                                        + proyecto.getId()
                                        + ": "
                                        + exception.getMessage()
                        );
                        return java.util.stream.Stream.empty();
                    }
                })
                .filter(asignacion -> asignacion != null)
                .filter(asignacion -> idMiembro.equals(asignacion.getIdMiembro()))
                .map(AsignacionProyectoResponse::getIdProyecto)
                .filter(idProyecto -> idProyecto != null)
                .collect(Collectors.toSet());
    }

    private List<TareaResponseDTO> obtenerTareasSegurasPorProyecto(Long idProyecto) {
        try {
            List<TareaResponseDTO> tareas = tareaClient.listarTareasPorProyecto(idProyecto);
            return tareas == null ? List.of() : tareas;
        } catch (Exception exception) {
            System.err.println(
                    "No se pudieron cargar tareas del proyecto "
                            + idProyecto
                            + ": "
                            + exception.getMessage()
            );
            return List.of();
        }
    }

    private List<AsignacionProyectoResponse> obtenerAsignacionesSegurasPorProyecto(Long idProyecto) {
        try {
            List<AsignacionProyectoResponse> asignaciones = equipoClient.listarMiembrosPorProyecto(idProyecto);
            return asignaciones == null ? List.of() : asignaciones;
        } catch (Exception exception) {
            System.err.println(
                    "No se pudieron cargar asignaciones del proyecto "
                            + idProyecto
                            + ": "
                            + exception.getMessage()
            );
            return List.of();
        }
    }

    private AvanceProyectoResponse calcularAvance(
            ProyectoResponseDTO proyecto,
            List<TareaResponseDTO> tareas
    ) {
        if (tareas == null) {
            tareas = List.of();
        }

        int total = tareas.size();

        int pendientes = contarTareasPorEstado(tareas, "PENDING");
        int enProgreso = contarTareasPorEstado(tareas, "IN_PROGRESS");
        int terminadas = contarTareasPorEstado(tareas, "DONE");

        double porcentajeAvance = total == 0 ? 0 : (terminadas * 100.0) / total;

        Long idProyecto = proyecto == null ? null : proyecto.getId();
        String nombreProyecto = proyecto == null ? "" : proyecto.getNombre();

        return AvanceProyectoResponse.builder()
                .idProyecto(idProyecto)
                .nombreProyecto(nombreProyecto)
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
        if (proyectos == null || estado == null) {
            return 0;
        }

        return (int) proyectos.stream()
                .filter(proyecto -> proyecto != null)
                .filter(proyecto -> proyecto.getEstado() != null)
                .filter(proyecto -> estado.equalsIgnoreCase(proyecto.getEstado()))
                .count();
    }

    private int contarTareasPorEstado(
            List<TareaResponseDTO> tareas,
            String estado
    ) {
        if (tareas == null || estado == null) {
            return 0;
        }

        return (int) tareas.stream()
                .filter(tarea -> tarea != null)
                .filter(tarea -> tarea.getEstado() != null)
                .filter(tarea -> estado.equalsIgnoreCase(tarea.getEstado()))
                .count();
    }

    private double redondearDosDecimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String normalizarEstadoTarea(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado de la tarea es obligatorio");
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        if (!Set.of("PENDING", "IN_PROGRESS", "DONE").contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de tarea inválido. Valores permitidos: PENDING, IN_PROGRESS, DONE"
            );
        }

        return estadoNormalizado;
    }

    private String normalizarEstadoProyecto(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado del proyecto es obligatorio");
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        if (!Set.of("PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de proyecto inválido. Valores permitidos: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED"
            );
        }

        return estadoNormalizado;
    }

    private void requireProyectoVisible(Long idProyecto, CurrentUser user) {
        if (idProyecto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del proyecto es obligatorio");
        }

        if (user.isAdmin()) {
            return;
        }

        try {
            boolean asignado = equipoClient.listarMiembrosPorProyecto(idProyecto)
                    .stream()
                    .filter(asignacion -> asignacion != null)
                    .anyMatch(asignacion -> user.id().equals(asignacion.getIdMiembro()));

            if (!asignado) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este proyecto");
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            System.err.println(
                    "No se pudo validar visibilidad del proyecto "
                            + idProyecto
                            + ": "
                            + exception.getMessage()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No fue posible validar el acceso al proyecto"
            );
        }
    }

    private void requireAdmin() {
        if (!currentUserService.getCurrentUser().isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede realizar esta accion");
        }
    }

    private void requireProjectWorkManager(CurrentUser user) {
        if (!user.canManageProjectWork()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para gestionar este proyecto");
        }
    }

    private TareaResponseDTO buscarTareaPorId(Long idTarea) {
        return tareaClient.obtenerTareaPorId(idTarea);
    }

    private boolean esResponsableDeTarea(TareaResponseDTO tarea, CurrentUser user) {
        return tarea != null
                && user != null
                && normalizarTexto(tarea.getResponsable()).equals(normalizarTexto(user.displayName()));
    }

    private String normalizarTexto(String valor) {
        return valor == null
                ? ""
                : valor.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}