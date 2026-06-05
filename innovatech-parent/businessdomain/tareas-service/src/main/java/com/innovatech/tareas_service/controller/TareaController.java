package com.innovatech.tareas_service.controller;

import com.innovatech.tareas_service.dto.CambioEstadoTareaDTO;
import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.service.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @PostMapping
    public ResponseEntity<TareaResponseDTO> crearTarea(@Valid @RequestBody TareaRequestDTO request) {
        TareaResponseDTO response = tareaService.crearTarea(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TareaResponseDTO>> listarTareas() {
        return ResponseEntity.ok(tareaService.listarTareas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.buscarPorId(id));
    }

    @GetMapping("/proyecto/{idProyecto}")
    public ResponseEntity<List<TareaResponseDTO>> buscarPorProyecto(@PathVariable Long idProyecto) {
        return ResponseEntity.ok(tareaService.buscarPorProyecto(idProyecto));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TareaResponseDTO>> buscarPorEstado(@PathVariable EstadoTarea estado) {
        return ResponseEntity.ok(tareaService.buscarPorEstado(estado));
    }

    @GetMapping("/proyecto/{idProyecto}/pendientes")
    public ResponseEntity<Map<String, Boolean>> existenTareasPendientes(@PathVariable Long idProyecto) {
        boolean existenPendientes = tareaService.existenTareasPendientesPorProyecto(idProyecto);
        return ResponseEntity.ok(Map.of("existenPendientes", existenPendientes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaResponseDTO> actualizarTarea(
            @PathVariable Long id,
            @Valid @RequestBody TareaRequestDTO request
    ) {
        return ResponseEntity.ok(tareaService.actualizarTarea(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TareaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoTareaDTO request
    ) {
        return ResponseEntity.ok(tareaService.cambiarEstado(id, request.getEstado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return ResponseEntity.noContent().build();
    }
}