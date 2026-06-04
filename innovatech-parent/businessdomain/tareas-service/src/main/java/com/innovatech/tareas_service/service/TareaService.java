package com.innovatech.tareas_service.service;

import com.innovatech.tareas_service.dto.TareaDTO;
import com.innovatech.tareas_service.entity.EstadoTarea;
import com.innovatech.tareas_service.entity.PrioridadTarea;
import com.innovatech.tareas_service.entity.Tarea;
import com.innovatech.tareas_service.exception.TareaNotFoundException;
import com.innovatech.tareas_service.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TareaService {

    private final TareaRepository tareaRepository;

    /**
     * Crear una nueva tarea
     */
    public TareaDTO crearTarea(TareaDTO tareaDTO) {
        log.info("Creando nueva tarea: {}", tareaDTO.getTitulo());
        Tarea tarea = mapearDTOAEntidad(tareaDTO);
        Tarea tareaGuardada = tareaRepository.save(tarea);
        log.info("Tarea creada exitosamente con ID: {}", tareaGuardada.getId());
        return mapearEntidadADTO(tareaGuardada);
    }

    /**
     * Obtener tarea por ID
     */
    @Transactional(readOnly = true)
    public TareaDTO obtenerTarea(Long id) {
        log.info("Obteniendo tarea con ID: {}", id);
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaNotFoundException("Tarea no encontrada con ID: " + id));
        return mapearEntidadADTO(tarea);
    }

    /**
     * Obtener todas las tareas con paginación
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTodasLasTareas(Pageable pageable) {
        log.info("Obteniendo todas las tareas");
        return tareaRepository.findAll(pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas por proyecto
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasPorProyecto(Long proyectoId, Pageable pageable) {
        log.info("Obteniendo tareas para el proyecto: {}", proyectoId);
        return tareaRepository.findByProyectoId(proyectoId, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas por equipo
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasPorEquipo(Long equipoId, Pageable pageable) {
        log.info("Obteniendo tareas para el equipo: {}", equipoId);
        return tareaRepository.findByEquipoId(equipoId, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas asignadas a un usuario
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasAsignadas(Long usuarioId, Pageable pageable) {
        log.info("Obteniendo tareas asignadas al usuario: {}", usuarioId);
        return tareaRepository.findByAsignadoA(usuarioId, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas por estado
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasPorEstado(EstadoTarea estado, Pageable pageable) {
        log.info("Obteniendo tareas con estado: {}", estado);
        return tareaRepository.findByEstado(estado, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas por prioridad
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasPorPrioridad(PrioridadTarea prioridad, Pageable pageable) {
        log.info("Obteniendo tareas con prioridad: {}", prioridad);
        return tareaRepository.findByPrioridad(prioridad, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Obtener tareas por estado y prioridad
     */
    @Transactional(readOnly = true)
    public Page<TareaDTO> obtenerTareasPorEstadoYPrioridad(
            EstadoTarea estado,
            PrioridadTarea prioridad,
            Pageable pageable) {
        log.info("Obteniendo tareas con estado: {} y prioridad: {}", estado, prioridad);
        return tareaRepository.findByEstadoAndPrioridad(estado, prioridad, pageable)
                .map(this::mapearEntidadADTO);
    }

    /**
     * Actualizar tarea
     */
    public TareaDTO actualizarTarea(Long id, TareaDTO tareaDTO) {
        log.info("Actualizando tarea con ID: {}", id);
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaNotFoundException("Tarea no encontrada con ID: " + id));

        tarea.setTitulo(tareaDTO.getTitulo());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setEstado(tareaDTO.getEstado());
        tarea.setPrioridad(tareaDTO.getPrioridad());
        tarea.setProyectoId(tareaDTO.getProyectoId());
        tarea.setEquipoId(tareaDTO.getEquipoId());
        tarea.setAsignadoA(tareaDTO.getAsignadoA());
        tarea.setFechaVencimiento(tareaDTO.getFechaVencimiento());

        Tarea tareaActualizada = tareaRepository.save(tarea);
        log.info("Tarea actualizada exitosamente");
        return mapearEntidadADTO(tareaActualizada);
    }

    /**
     * Actualizar estado de la tarea
     */
    public TareaDTO actualizarEstado(Long id, EstadoTarea nuevoEstado) {
        log.info("Actualizando estado de tarea con ID: {} a {}", id, nuevoEstado);
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaNotFoundException("Tarea no encontrada con ID: " + id));

        tarea.setEstado(nuevoEstado);
        Tarea tareaActualizada = tareaRepository.save(tarea);
        log.info("Estado de tarea actualizado exitosamente");
        return mapearEntidadADTO(tareaActualizada);
    }

    /**
     * Asignar tarea a un usuario
     */
    public TareaDTO asignarTarea(Long id, Long usuarioId) {
        log.info("Asignando tarea con ID: {} al usuario: {}", id, usuarioId);
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaNotFoundException("Tarea no encontrada con ID: " + id));

        tarea.setAsignadoA(usuarioId);
        Tarea tareaActualizada = tareaRepository.save(tarea);
        log.info("Tarea asignada exitosamente");
        return mapearEntidadADTO(tareaActualizada);
    }

    /**
     * Eliminar tarea
     */
    public void eliminarTarea(Long id) {
        log.info("Eliminando tarea con ID: {}", id);
        if (!tareaRepository.existsById(id)) {
            throw new TareaNotFoundException("Tarea no encontrada con ID: " + id);
        }
        tareaRepository.deleteById(id);
        log.info("Tarea eliminada exitosamente");
    }

    /**
     * Obtener tareas próximas a vencer
     */
    @Transactional(readOnly = true)
    public List<TareaDTO> obtenerTareasPróximasAVencer(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Obteniendo tareas próximas a vencer entre {} y {}", inicio, fin);
        return tareaRepository.findTareasPorRangoFecha(inicio, fin)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas de tareas
     */
    @Transactional(readOnly = true)
    public TareaEstadisticasDTO obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de tareas");
        return TareaEstadisticasDTO.builder()
                .totalPendientes(tareaRepository.countByEstado(EstadoTarea.PENDIENTE))
                .totalEnProgreso(tareaRepository.countByEstado(EstadoTarea.EN_PROGRESO))
                .totalEnRevision(tareaRepository.countByEstado(EstadoTarea.EN_REVISION))
                .totalCompletadas(tareaRepository.countByEstado(EstadoTarea.COMPLETADA))
                .totalCanceladas(tareaRepository.countByEstado(EstadoTarea.CANCELADA))
                .build();
    }

    /**
     * Mapear DTO a Entidad
     */
    private Tarea mapearDTOAEntidad(TareaDTO tareaDTO) {
        return Tarea.builder()
                .id(tareaDTO.getId())
                .titulo(tareaDTO.getTitulo())
                .descripcion(tareaDTO.getDescripcion())
                .estado(tareaDTO.getEstado())
                .prioridad(tareaDTO.getPrioridad())
                .proyectoId(tareaDTO.getProyectoId())
                .equipoId(tareaDTO.getEquipoId())
                .asignadoA(tareaDTO.getAsignadoA())
                .fechaVencimiento(tareaDTO.getFechaVencimiento())
                .build();
    }

    /**
     * Mapear Entidad a DTO
     */
    private TareaDTO mapearEntidadADTO(Tarea tarea) {
        return TareaDTO.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())
                .estado(tarea.getEstado())
                .prioridad(tarea.getPrioridad())
                .proyectoId(tarea.getProyectoId())
                .equipoId(tarea.getEquipoId())
                .asignadoA(tarea.getAsignadoA())
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaActualizacion(tarea.getFechaActualizacion())
                .fechaVencimiento(tarea.getFechaVencimiento())
                .build();
    }
}
