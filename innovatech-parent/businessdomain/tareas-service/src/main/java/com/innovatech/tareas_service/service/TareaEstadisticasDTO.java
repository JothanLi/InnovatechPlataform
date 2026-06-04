package com.innovatech.tareas_service.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaEstadisticasDTO {
    private Long totalPendientes;
    private Long totalEnProgreso;
    private Long totalEnRevision;
    private Long totalCompletadas;
    private Long totalCanceladas;
}
