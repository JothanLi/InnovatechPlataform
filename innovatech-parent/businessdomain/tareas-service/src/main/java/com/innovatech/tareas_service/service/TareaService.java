package com.innovatech.tareas_service.service;

import com.innovatech.tareas_service.adapter.ProyectoServiceAdapter;
import com.innovatech.tareas_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.exception.RecursoNoEncontradoException;
import com.innovatech.tareas_service.exception.ReglaNegocioException;
import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.model.Tarea;
import com.innovatech.tareas_service.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final ProyectoServiceAdapter proyectoServiceAdapter;

    public TareaResponseDTO crearTarea(TareaRequestDTO request) {
        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(request.getIdProyecto());

        validarFechas(request);

        Tarea tarea = Tarea.builder()
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .idProyecto(request.getIdProyecto())
                .responsable(request.getResponsable())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        Tarea tareaGuardada = tareaRepository.save(tarea);

        return convertirAResponse(tareaGuardada, proyecto.nombre());
    }

    public List<TareaResponseDTO> listarTareas() {
        return tareaRepository.findAll()
                .stream()
                .map(this::convertirAResponseSinProyecto)
                .toList();
    }

    public TareaResponseDTO buscarPorId(Long id) {
        Tarea tarea = obtenerTareaPorId(id);
        return convertirAResponseConProyecto(tarea);
    }

    public List<TareaResponseDTO> buscarPorProyecto(Long idProyecto) {
        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(idProyecto);

        return tareaRepository.findByIdProyecto(idProyecto)
                .stream()
                .map(tarea -> convertirAResponse(tarea, proyecto.nombre()))
                .toList();
    }

    public List<TareaResponseDTO> buscarPorEstado(EstadoTarea estado) {
        return tareaRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponseSinProyecto)
                .toList();
    }

    public TareaResponseDTO actualizarTarea(Long id, TareaRequestDTO request) {
        Tarea tarea = obtenerTareaPorId(id);
        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(request.getIdProyecto());

        validarFechas(request);
        validarCambioEstado(tarea.getEstado(), request.getEstado());

        tarea.setDescripcion(request.getDescripcion());
        tarea.setEstado(request.getEstado());
        tarea.setIdProyecto(request.getIdProyecto());
        tarea.setResponsable(request.getResponsable());
        tarea.setFechaInicio(request.getFechaInicio());
        tarea.setFechaFinEstimada(request.getFechaFinEstimada());

        Tarea tareaActualizada = tareaRepository.save(tarea);

        return convertirAResponse(tareaActualizada, proyecto.nombre());
    }

    public TareaResponseDTO cambiarEstado(Long id, EstadoTarea nuevoEstado) {
        Tarea tarea = obtenerTareaPorId(id);

        validarCambioEstado(tarea.getEstado(), nuevoEstado);

        tarea.setEstado(nuevoEstado);
        Tarea tareaActualizada = tareaRepository.save(tarea);

        return convertirAResponseConProyecto(tareaActualizada);
    }

    public void eliminarTarea(Long id) {
        Tarea tarea = obtenerTareaPorId(id);
        tareaRepository.delete(tarea);
    }

    public boolean existenTareasPendientesPorProyecto(Long idProyecto) {
        return tareaRepository.existsByIdProyectoAndEstadoNot(idProyecto, EstadoTarea.DONE);
    }

    private Tarea obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una tarea con ID: " + id));
    }

    private void validarFechas(TareaRequestDTO request) {
        if (request.getFechaInicio() != null
                && request.getFechaFinEstimada() != null
                && request.getFechaFinEstimada().isBefore(request.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha fin estimada no puede ser anterior a la fecha de inicio");
        }
    }

    private void validarCambioEstado(EstadoTarea estadoActual, EstadoTarea nuevoEstado) {
        if (estadoActual == EstadoTarea.DONE && nuevoEstado != EstadoTarea.DONE) {
            throw new ReglaNegocioException("No se puede modificar el estado de una tarea finalizada");
        }

        if (estadoActual == EstadoTarea.PENDING && nuevoEstado == EstadoTarea.DONE) {
            throw new ReglaNegocioException("Una tarea pendiente debe pasar primero a IN_PROGRESS antes de finalizarse");
        }
    }

    private TareaResponseDTO convertirAResponseConProyecto(Tarea tarea) {
        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(tarea.getIdProyecto());
        return convertirAResponse(tarea, proyecto.nombre());
    }

    private TareaResponseDTO convertirAResponseSinProyecto(Tarea tarea) {
        return convertirAResponse(tarea, null);
    }

    private TareaResponseDTO convertirAResponse(Tarea tarea, String nombreProyecto) {
        return TareaResponseDTO.builder()
                .id(tarea.getId())
                .descripcion(tarea.getDescripcion())
                .estado(tarea.getEstado())
                .idProyecto(tarea.getIdProyecto())
                .nombreProyecto(nombreProyecto)
                .responsable(tarea.getResponsable())
                .fechaInicio(tarea.getFechaInicio())
                .fechaFinEstimada(tarea.getFechaFinEstimada())
                .build();
    }
}