package com.innovatech.bff_service.controller;

import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import com.innovatech.bff_service.facade.InnovatechBffFacade;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff")
public class BffController {

    private final InnovatechBffFacade bffFacade;

    public BffController(InnovatechBffFacade bffFacade) {
        this.bffFacade = bffFacade;
    }

    @GetMapping("/proyectos")
    public List<ProyectoResponseDTO> listarProyectos() {
        return bffFacade.listarProyectos();
    }

    @GetMapping("/proyectos/{idProyecto}/detalle")
    public ProyectoDetalleResponse obtenerDetalleProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerDetalleProyecto(idProyecto);
    }

    @GetMapping("/proyectos/{idProyecto}/tareas")
    public List<TareaResponseDTO> obtenerTareasPorProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerTareasPorProyecto(idProyecto);
    }

    @GetMapping("/proyectos/{idProyecto}/miembros")
    public List<AsignacionProyectoResponse> obtenerMiembrosPorProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerMiembrosPorProyecto(idProyecto);
    }

    @GetMapping("/proyectos/{idProyecto}/avance")
    public AvanceProyectoResponse obtenerAvanceProyecto(
            @PathVariable Long idProyecto
    ) {
        return bffFacade.obtenerAvanceProyecto(idProyecto);
    }
}