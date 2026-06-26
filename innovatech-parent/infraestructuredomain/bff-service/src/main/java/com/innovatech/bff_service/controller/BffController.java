package com.innovatech.bff_service.controller;

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
import com.innovatech.bff_service.facade.InnovatechBffFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff")
@Validated
public class BffController {

    private final InnovatechBffFacade bffFacade;

    public BffController(InnovatechBffFacade bffFacade) {
        this.bffFacade = bffFacade;
    }

    @GetMapping("/proyectos")
    public List<ProyectoResponseDTO> listarProyectos() {
        return bffFacade.listarProyectos();
    }

    @PostMapping("/proyectos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProyectoResponseDTO crearProyecto(
            @Valid @RequestBody ProyectoRequestDTO request
    ) {
        return bffFacade.crearProyecto(request);
    }

    @GetMapping({
            "/proyectos/{idProyecto}",
            "/proyectos/{idProyecto}/detalle"
    })
    public ProyectoDetalleResponse obtenerDetalleProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerDetalleProyecto(idProyecto);
    }

    @GetMapping({
            "/proyectos/{idProyecto}/tareas",
            "/tareas/proyecto/{idProyecto}"
    })
    public List<TareaResponseDTO> obtenerTareasPorProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerTareasPorProyecto(idProyecto);
    }

    @PostMapping("/tareas")
    @ResponseStatus(HttpStatus.CREATED)
    public TareaResponseDTO crearTarea(
            @Valid @RequestBody TareaRequestDTO request
    ) {
        return bffFacade.crearTarea(request);
    }

    @PatchMapping("/tareas/{idTarea}/estado")
    public TareaResponseDTO cambiarEstadoTarea(
            @PathVariable Long idTarea,
            @Valid @RequestBody TareaClientEstadoRequest request
    ) {
        return bffFacade.cambiarEstadoTarea(idTarea, request.estado());
    }

    @GetMapping("/miembros")
    public List<MiembroEquipoResponse> listarMiembros() {
        return bffFacade.listarMiembros();
    }

    @PostMapping("/miembros")
    @ResponseStatus(HttpStatus.CREATED)
    public MiembroEquipoResponse crearMiembro(
            @Valid @RequestBody MiembroEquipoRequest request
    ) {
        return bffFacade.crearMiembro(request);
    }

    @GetMapping({
            "/proyectos/{idProyecto}/miembros",
            "/equipos/asignaciones/proyecto/{idProyecto}"
    })
    public List<AsignacionProyectoResponse> obtenerMiembrosPorProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerMiembrosPorProyecto(idProyecto);
    }

    @PostMapping({
            "/asignaciones",
            "/equipos/asignaciones"
    })
    @ResponseStatus(HttpStatus.CREATED)
    public AsignacionProyectoResponse asignarMiembroAProyecto(
            @Valid @RequestBody AsignacionProyectoRequest request
    ) {
        return bffFacade.asignarMiembroAProyecto(request);
    }

    @GetMapping("/proyectos/{idProyecto}/avance")
    public AvanceProyectoResponse obtenerAvanceProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerAvanceProyecto(idProyecto);
    }

    @GetMapping("/dashboard/resumen")
    public DashboardResumenResponse obtenerDashboardResumen() {
        return bffFacade.obtenerDashboardResumen();
    }

    public record TareaClientEstadoRequest(
            @NotBlank(message = "El estado de la tarea es obligatorio")
            String estado
    ) {
    }
}