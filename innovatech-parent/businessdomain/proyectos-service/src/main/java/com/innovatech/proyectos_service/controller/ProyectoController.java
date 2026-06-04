package com.innovatech.proyectos_service.controller;

import com.innovatech.proyectos_service.dto.CambioEstadoProyectoDTO;
import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.service.ProyectoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @PostMapping
    public ResponseEntity<ProyectoResponseDTO> crearProyecto(@Valid @RequestBody ProyectoRequestDTO request) {
        ProyectoResponseDTO response = proyectoService.crearProyecto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProyectoResponseDTO>> listarProyectos() {
        return ResponseEntity.ok(proyectoService.listarProyectos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.buscarPorId(id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ProyectoResponseDTO>> buscarPorEstado(@PathVariable EstadoProyecto estado) {
        return ResponseEntity.ok(proyectoService.buscarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> actualizarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoRequestDTO request
    ) {
        return ResponseEntity.ok(proyectoService.actualizarProyecto(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProyectoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoProyectoDTO request
    ) {
        return ResponseEntity.ok(proyectoService.cambiarEstado(id, request.getEstado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyecto(@PathVariable Long id) {
        proyectoService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }
}