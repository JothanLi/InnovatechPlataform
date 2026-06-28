package com.innovatech.proyectos_service.controller;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.service.ProyectoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProyectoResponseDTO crearProyecto(@Valid @RequestBody ProyectoRequestDTO request) {
        return proyectoService.crearProyecto(request);
    }

    @GetMapping
    public List<ProyectoResponseDTO> listarProyectos(
            @RequestParam(required = false) EstadoProyecto estado
    ) {
        if (estado != null) {
            return proyectoService.buscarPorEstado(estado);
        }

        return proyectoService.listarProyectos();
    }

    @GetMapping("/{id}")
    public ProyectoResponseDTO buscarPorId(@PathVariable Long id) {
        return proyectoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ProyectoResponseDTO actualizarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoRequestDTO request
    ) {
        return proyectoService.actualizarProyecto(id, request);
    }

    /*
     * Endpoint externo normal.
     * Puede usarse directo con PATCH.
     */
    @PatchMapping("/{id}/estado")
    public ProyectoResponseDTO cambiarEstadoPatch(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoProyectoRequest request
    ) {
        return proyectoService.cambiarEstado(id, request.estado());
    }

    /*
     * Endpoint interno usado por el BFF.
     * Lo dejamos con PUT para evitar problemas de Feign con PATCH.
     */
    @PutMapping("/{id}/estado")
    public ProyectoResponseDTO cambiarEstadoPut(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoProyectoRequest request
    ) {
        return proyectoService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProyecto(@PathVariable Long id) {
        proyectoService.eliminarProyecto(id);
    }

    public record CambioEstadoProyectoRequest(
            @NotNull(message = "El estado del proyecto es obligatorio")
            EstadoProyecto estado
    ) {
    }
}