package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.AsignacionProyectoRequest;
import com.innovatech.equipos_service.dto.AsignacionProyectoResponse;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.exception.RecursoNoEncontradoException;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.AsignacionProyecto;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignacionProyectoService {

    private final AsignacionProyectoRepository asignacionProyectoRepository;
    private final MiembroEquipoService miembroEquipoService;
    private final ProyectoServiceAdapter proyectoServiceAdapter;

    public AsignacionProyectoResponse asignarMiembroAProyecto(AsignacionProyectoRequest request) {
        MiembroEquipo miembro = miembroEquipoService.buscarEntidadPorId(request.idMiembro());

        if (miembro.getEstado() == EstadoMiembro.INACTIVO) {
            throw new ReglaNegocioException("No se puede asignar un miembro inactivo a un proyecto");
        }

        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(request.idProyecto());

        boolean yaAsignado = asignacionProyectoRepository.existsByIdProyectoAndMiembroId(
                request.idProyecto(),
                request.idMiembro()
        );

        if (yaAsignado) {
            throw new ReglaNegocioException("El miembro ya se encuentra asignado a este proyecto");
        }

        AsignacionProyecto asignacion = AsignacionProyecto.builder()
                .idProyecto(request.idProyecto())
                .miembro(miembro)
                .fechaAsignacion(LocalDate.now())
                .build();

        AsignacionProyecto guardada = asignacionProyectoRepository.save(asignacion);

        return mapearAsignacionResponse(guardada, proyecto);
    }

    public List<AsignacionProyectoResponse> listarAsignacionesPorProyecto(Long idProyecto) {
        ProyectoAdaptadoResponse proyecto = proyectoServiceAdapter.obtenerProyectoAdaptado(idProyecto);

        return asignacionProyectoRepository.findByIdProyecto(idProyecto)
                .stream()
                .map(asignacion -> mapearAsignacionResponse(asignacion, proyecto))
                .toList();
    }

    public void eliminarAsignacion(Long idAsignacion) {
        AsignacionProyecto asignacion = asignacionProyectoRepository.findById(idAsignacion)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la asignación con ID: " + idAsignacion
                ));

        asignacionProyectoRepository.delete(asignacion);
    }

    private AsignacionProyectoResponse mapearAsignacionResponse(
            AsignacionProyecto asignacion,
            ProyectoAdaptadoResponse proyecto
    ) {
        MiembroEquipo miembro = asignacion.getMiembro();

        return new AsignacionProyectoResponse(
                asignacion.getId(),
                asignacion.getIdProyecto(),
                proyecto.nombreProyecto(),
                miembro.getId(),
                miembro.getNombre() + " " + miembro.getApellido(),
                miembro.getRol().name(),
                asignacion.getFechaAsignacion()
        );
    }
}