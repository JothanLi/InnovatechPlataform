package com.innovatech.proyectos_service.service;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.RecursoNoEncontradoException;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoResponseDTO crearProyecto(ProyectoRequestDTO request) {
        validarNombreDuplicado(request.getNombre());

        Proyecto proyecto = Proyecto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        return convertirAResponse(proyectoGuardado);
    }

    public List<ProyectoResponseDTO> listarProyectos() {
        return proyectoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ProyectoResponseDTO buscarPorId(Long id) {
        Proyecto proyecto = obtenerProyectoPorId(id);
        return convertirAResponse(proyecto);
    }

    public List<ProyectoResponseDTO> buscarPorEstado(EstadoProyecto estado) {
        return proyectoRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ProyectoResponseDTO actualizarProyecto(Long id, ProyectoRequestDTO request) {
        Proyecto proyecto = obtenerProyectoPorId(id);

        if (!proyecto.getNombre().equalsIgnoreCase(request.getNombre())
                && proyectoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe otro proyecto registrado con el nombre: " + request.getNombre());
        }

        proyecto.setNombre(request.getNombre());
        proyecto.setDescripcion(request.getDescripcion());
        proyecto.setEstado(request.getEstado());
        proyecto.setFechaInicio(request.getFechaInicio());
        proyecto.setFechaFinEstimada(request.getFechaFinEstimada());

        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);

        return convertirAResponse(proyectoActualizado);
    }

    public ProyectoResponseDTO cambiarEstado(Long id, EstadoProyecto nuevoEstado) {
        Proyecto proyecto = obtenerProyectoPorId(id);

        validarCambioEstado(proyecto, nuevoEstado);

        proyecto.setEstado(nuevoEstado);
        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);

        return convertirAResponse(proyectoActualizado);
    }

    public void eliminarProyecto(Long id) {
        Proyecto proyecto = obtenerProyectoPorId(id);
        proyectoRepository.delete(proyecto);
    }

    private Proyecto obtenerProyectoPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un proyecto con ID: " + id));
    }

    private void validarNombreDuplicado(String nombre) {
        if (proyectoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un proyecto registrado con el nombre: " + nombre);
        }
    }

    private void validarCambioEstado(Proyecto proyecto, EstadoProyecto nuevoEstado) {
        if (proyecto.getEstado() == EstadoProyecto.CANCELLED) {
            throw new ReglaNegocioException("No se puede cambiar el estado de un proyecto cancelado");
        }

        if (nuevoEstado == EstadoProyecto.COMPLETED && existenTareasPendientes(proyecto.getId())) {
            throw new ReglaNegocioException("No se puede finalizar el proyecto porque existen tareas pendientes");
        }
    }

    private boolean existenTareasPendientes(Long idProyecto) {
        /*
         * Más adelante esta validación se conectará con tareas-service usando OpenFeign.
         * Por ahora retorna false para permitir probar el CRUD de proyectos.
         */
        return false;
    }

    private ProyectoResponseDTO convertirAResponse(Proyecto proyecto) {
        return ProyectoResponseDTO.builder()
                .id(proyecto.getId())
                .nombre(proyecto.getNombre())
                .descripcion(proyecto.getDescripcion())
                .estado(proyecto.getEstado())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                .build();
    }
}