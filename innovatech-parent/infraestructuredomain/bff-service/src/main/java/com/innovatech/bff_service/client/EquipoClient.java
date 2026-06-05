package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "equipos-service",
        url = "${innovatech.services.equipos}"
)
public interface EquipoClient {

    @GetMapping("/api/v1/equipos/asignaciones/proyecto/{idProyecto}")
    List<AsignacionProyectoResponse> listarMiembrosPorProyecto(@PathVariable("idProyecto") Long idProyecto);
}