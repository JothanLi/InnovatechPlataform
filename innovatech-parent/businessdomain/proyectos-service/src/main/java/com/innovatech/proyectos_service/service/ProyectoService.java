package com.innovatech.proyectos_service.service;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.RecursoNoEncontradoException;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.factory.ProyectoFactory;
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

        Proyecto proyecto = ProyectoFactory.crearDesdeRequest(request);

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
        if (nuevoEstado == null) {
            throw new ReglaNegocioException("El estado del proyecto es obligatorio");
        }

        /*
         * El administrador puede cambiar manualmente el estado del proyecto.
         * Por eso aquí NO bloqueamos COMPLETED aunque existan tareas pendientes.
         *
         * La actualización automática por tareas sigue funcionando desde tareas-service:
         * cuando todas las tareas quedan DONE, el proyecto pasa a COMPLETED.
         */
    }

    private ProyectoResponseDTO convertirAResponse(Proyecto proyecto) {
        return ProyectoFactory.crearResponse(proyecto);
    }
}