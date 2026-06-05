package com.innovatech.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionProyectoResponse {

    private Long id;
    private Long idMiembro;
    private String nombreMiembro;
    private String rolMiembro;
    private Long idProyecto;
    private String nombreProyecto;
}