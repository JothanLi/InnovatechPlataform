package com.innovatech.equipos_service.factory;

import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;

public final class MiembroEquipoFactory {

    private MiembroEquipoFactory() {
    }

    public static MiembroEquipo crearDesdeRequest(MiembroEquipoRequest request, String passwordHash) {
        return MiembroEquipo.builder()
                .nombres(request.nombres())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .email(request.email())
                .passwordHash(passwordHash)
                .rol(request.rol())
                .estado(EstadoMiembro.ACTIVO)
                .build();
    }

    public static MiembroEquipoResponse crearResponse(MiembroEquipo miembro) {
        return new MiembroEquipoResponse(
                miembro.getId(),
                miembro.getNombres(),
                miembro.getApellidoPaterno(),
                miembro.getApellidoMaterno(),
                miembro.getEmail(),
                miembro.getRol(),
                miembro.getEstado()
        );
    }
}
