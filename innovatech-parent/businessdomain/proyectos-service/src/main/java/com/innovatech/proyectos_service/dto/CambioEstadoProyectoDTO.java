package com.innovatech.proyectos_service.dto;

import com.innovatech.proyectos_service.model.EstadoProyecto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoProyectoDTO {

    @NotNull(message = "El nuevo estado del proyecto es obligatorio")
    private EstadoProyecto estado;
}