package com.innovatech.equipos_service.controller;

import com.innovatech.equipos_service.dto.AsignacionProyectoRequest;
import com.innovatech.equipos_service.dto.AsignacionProyectoResponse;
import com.innovatech.equipos_service.service.AsignacionProyectoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/equipos/asignaciones")
public class AsignacionProyectoController {

    private final AsignacionProyectoService asignacionProyectoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsignacionProyectoResponse asignarMiembroAProyecto(
            @Valid @RequestBody AsignacionProyectoRequest request
    ) {
        return asignacionProyectoService.asignarMiembroAProyecto(request);
    }

    @GetMapping("/proyecto/{idProyecto}")
    public List<AsignacionProyectoResponse> listarAsignacionesPorProyecto(
            @PathVariable Long idProyecto
    ) {
        return asignacionProyectoService.listarAsignacionesPorProyecto(idProyecto);
    }

    @DeleteMapping("/{idAsignacion}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAsignacion(@PathVariable Long idAsignacion) {
        asignacionProyectoService.eliminarAsignacion(idAsignacion);
    }
}