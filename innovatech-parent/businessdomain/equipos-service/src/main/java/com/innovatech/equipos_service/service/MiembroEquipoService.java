package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.dto.MiembroAuthResponse;
import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.exception.RecursoNoEncontradoException;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.factory.MiembroEquipoFactory;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroEquipoService {

    private final MiembroEquipoRepository miembroEquipoRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

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

    public MiembroAuthResponse obtenerMiembroAuthPorEmail(String email) {
        MiembroEquipo miembro = miembroEquipoRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un miembro con email: " + email
                ));

        return new MiembroAuthResponse(
                miembro.getId(),
                miembro.getEmail(),
                miembro.getPasswordHash(),
                miembro.getRol(),
                miembro.getEstado()
        );
    }

    public MiembroEquipoResponse crearMiembro(MiembroEquipoRequest request) {
        if (miembroEquipoRepository.existsByEmail(request.email())) {
            throw new ReglaNegocioException("Ya existe un miembro con el email: " + request.email());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        MiembroEquipo miembro = MiembroEquipoFactory.crearDesdeRequest(request, passwordHash);

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

        miembro.setNombres(request.nombres());
        miembro.setApellidoPaterno(request.apellidoPaterno());
        miembro.setApellidoMaterno(request.apellidoMaterno());
        miembro.setEmail(request.email());
        miembro.setRol(request.rol());
        miembro.setPasswordHash(passwordEncoder.encode(request.password()));

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
        return MiembroEquipoFactory.crearResponse(miembro);
    }
}
