package com.innovatech.tareas_service.dto;

import com.innovatech.tareas_service.model.EstadoTarea;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoTareaDTO {

    @NotNull(message = "El nuevo estado de la tarea es obligatorio")
    private EstadoTarea estado;
}