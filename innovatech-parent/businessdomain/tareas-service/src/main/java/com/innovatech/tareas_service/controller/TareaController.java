package com.innovatech.tareas_service.controller;

import com.innovatech.tareas_service.dto.TareaDTO;
import com.innovatech.tareas_service.entity.EstadoTarea;
import com.innovatech.tareas_service.entity.PrioridadTarea;
import com.innovatech.tareas_service.service.TareaEstadisticasDTO;
import com.innovatech.tareas_service.service.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
@Slf4j
public class TareaController {

    private final TareaService tareaService;

    /**
     * Crear una nueva tarea
     */
    @PostMapping
    public ResponseEntity<TareaDTO> crearTarea(@Valid @RequestBody TareaDTO tareaDTO) {
        log.info("Request para crear nueva tarea");
        TareaDTO tareaCreada = tareaService.crearTarea(tareaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaCreada);
    }

    /**
     * Obtener tarea por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TareaDTO> obtenerTarea(@PathVariable Long id) {
        log.info("Request para obtener tarea con ID: {}", id);
        TareaDTO tarea = tareaService.obtenerTarea(id);
        return ResponseEntity.ok(tarea);
    }

    /**
     * Obtener todas las tareas
     */
    @GetMapping
    public ResponseEntity<Page<TareaDTO>> obtenerTodasLasTareas(Pageable pageable) {
        log.info("Request para obtener todas las tareas");
        Page<TareaDTO> tareas = tareaService.obtenerTodasLasTareas(pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas por proyecto
     */
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasPorProyecto(
            @PathVariable Long proyectoId,
            Pageable pageable) {
        log.info("Request para obtener tareas del proyecto: {}", proyectoId);
        Page<TareaDTO> tareas = tareaService.obtenerTareasPorProyecto(proyectoId, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas por equipo
     */
    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasPorEquipo(
            @PathVariable Long equipoId,
            Pageable pageable) {
        log.info("Request para obtener tareas del equipo: {}", equipoId);
        Page<TareaDTO> tareas = tareaService.obtenerTareasPorEquipo(equipoId, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas asignadas a un usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasAsignadas(
            @PathVariable Long usuarioId,
            Pageable pageable) {
        log.info("Request para obtener tareas asignadas al usuario: {}", usuarioId);
        Page<TareaDTO> tareas = tareaService.obtenerTareasAsignadas(usuarioId, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasPorEstado(
            @PathVariable EstadoTarea estado,
            Pageable pageable) {
        log.info("Request para obtener tareas con estado: {}", estado);
        Page<TareaDTO> tareas = tareaService.obtenerTareasPorEstado(estado, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas por prioridad
     */
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasPorPrioridad(
            @PathVariable PrioridadTarea prioridad,
            Pageable pageable) {
        log.info("Request para obtener tareas con prioridad: {}", prioridad);
        Page<TareaDTO> tareas = tareaService.obtenerTareasPorPrioridad(prioridad, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener tareas por estado y prioridad
     */
    @GetMapping("/filtro")
    public ResponseEntity<Page<TareaDTO>> obtenerTareasFiltradas(
            @RequestParam EstadoTarea estado,
            @RequestParam PrioridadTarea prioridad,
            Pageable pageable) {
        log.info("Request para obtener tareas filtradas - Estado: {}, Prioridad: {}", estado, prioridad);
        Page<TareaDTO> tareas = tareaService.obtenerTareasPorEstadoYPrioridad(estado, prioridad, pageable);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Actualizar tarea
     */
    @PutMapping("/{id}")
    public ResponseEntity<TareaDTO> actualizarTarea(
            @PathVariable Long id,
            @Valid @RequestBody TareaDTO tareaDTO) {
        log.info("Request para actualizar tarea con ID: {}", id);
        TareaDTO tareaActualizada = tareaService.actualizarTarea(id, tareaDTO);
        return ResponseEntity.ok(tareaActualizada);
    }

    /**
     * Actualizar solo el estado de la tarea
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TareaDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoTarea nuevoEstado) {
        log.info("Request para actualizar estado de tarea con ID: {} a {}", id, nuevoEstado);
        TareaDTO tareaActualizada = tareaService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(tareaActualizada);
    }

    /**
     * Asignar tarea a un usuario
     */
    @PatchMapping("/{id}/asignar/{usuarioId}")
    public ResponseEntity<TareaDTO> asignarTarea(
            @PathVariable Long id,
            @PathVariable Long usuarioId) {
        log.info("Request para asignar tarea con ID: {} al usuario: {}", id, usuarioId);
        TareaDTO tareaAsignada = tareaService.asignarTarea(id, usuarioId);
        return ResponseEntity.ok(tareaAsignada);
    }

    /**
     * Eliminar tarea
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        log.info("Request para eliminar tarea con ID: {}", id);
        tareaService.eliminarTarea(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener tareas próximas a vencer
     */
    @GetMapping("/proximas-a-vencer")
    public ResponseEntity<List<TareaDTO>> obtenerTareasPróximasAVencer(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        log.info("Request para obtener tareas próximas a vencer entre {} y {}", inicio, fin);
        List<TareaDTO> tareas = tareaService.obtenerTareasPróximasAVencer(inicio, fin);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtener estadísticas de tareas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<TareaEstadisticasDTO> obtenerEstadisticas() {
        log.info("Request para obtener estadísticas de tareas");
        TareaEstadisticasDTO estadisticas = tareaService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }
}
