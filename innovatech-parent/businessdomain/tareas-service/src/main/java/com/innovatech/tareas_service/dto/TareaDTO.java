package com.innovatech.tareas_service.dto;

import com.innovatech.tareas_service.entity.EstadoTarea;
import com.innovatech.tareas_service.entity.PrioridadTarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaDTO {

    private Long id;

    @NotBlank(message = "El título es requerido")
    private String titulo;

    private String descripcion;

    @NotNull(message = "El estado es requerido")
    private EstadoTarea estado;

    @NotNull(message = "La prioridad es requerida")
    private PrioridadTarea prioridad;

    private Long proyectoId;

    private Long equipoId;

    private Long asignadoA;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaVencimiento;
}
