package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.exception.RecursoNoEncontradoException;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroEquipoService {

    private final MiembroEquipoRepository miembroEquipoRepository;

    public List<MiembroEquipoResponse> listarMiembros() {
        return miembroEquipoRepository.findAll()
                .stream()
                .map(this::mapearMiembroResponse)
                .toList();
    }

    public MiembroEquipoResponse obtenerMiembroPorId(Long id) {
        MiembroEquipo miembro = buscarMiembroPorId(id);
        return mapearMiembroResponse(miembro);
    }

    public MiembroEquipoResponse crearMiembro(MiembroEquipoRequest request) {
        if (miembroEquipoRepository.existsByEmail(request.email())) {
            throw new ReglaNegocioException("Ya existe un miembro con el email: " + request.email());
        }

        MiembroEquipo miembro = MiembroEquipo.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .rol(request.rol())
                .estado(EstadoMiembro.ACTIVO)
                .build();

        MiembroEquipo guardado = miembroEquipoRepository.save(miembro);
        return mapearMiembroResponse(guardado);
    }

    public MiembroEquipoResponse actualizarMiembro(Long id, MiembroEquipoRequest request) {
        MiembroEquipo miembro = buscarMiembroPorId(id);

        miembroEquipoRepository.findByEmail(request.email())
                .ifPresent(miembroExistente -> {
                    if (!miembroExistente.getId().equals(id)) {
                        throw new ReglaNegocioException("Ya existe otro miembro con el email: " + request.email());
                    }
                });

        miembro.setNombre(request.nombre());
        miembro.setApellido(request.apellido());
        miembro.setEmail(request.email());
        miembro.setRol(request.rol());

        MiembroEquipo actualizado = miembroEquipoRepository.save(miembro);
        return mapearMiembroResponse(actualizado);
    }

    public void desactivarMiembro(Long id) {
        MiembroEquipo miembro = buscarMiembroPorId(id);
        miembro.setEstado(EstadoMiembro.INACTIVO);
        miembroEquipoRepository.save(miembro);
    }

    public MiembroEquipo buscarEntidadPorId(Long id) {
        return buscarMiembroPorId(id);
    }

    private MiembroEquipo buscarMiembroPorId(Long id) {
        return miembroEquipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el miembro con ID: " + id
                ));
    }

    private MiembroEquipoResponse mapearMiembroResponse(MiembroEquipo miembro) {
        return new MiembroEquipoResponse(
                miembro.getId(),
                miembro.getNombre(),
                miembro.getApellido(),
                miembro.getEmail(),
                miembro.getRol(),
                miembro.getEstado()
        );
    }
}