package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AsignacionProyectoRequest;
import com.innovatech.bff_service.dto.MiembroEquipoRequest;
import com.innovatech.bff_service.dto.MiembroEquipoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "equipos-service",
        url = "${innovatech.services.equipos}"
)
public interface EquipoClient {

    @GetMapping("/api/v1/equipos/miembros")
    List<MiembroEquipoResponse> listarMiembros();

    @PostMapping("/api/v1/equipos/miembros")
    MiembroEquipoResponse crearMiembro(@RequestBody MiembroEquipoRequest request);

    @GetMapping("/api/v1/equipos/asignaciones/proyecto/{idProyecto}")
    List<AsignacionProyectoResponse> listarMiembrosPorProyecto(@PathVariable("idProyecto") Long idProyecto);

    @PostMapping("/api/v1/equipos/asignaciones")
    AsignacionProyectoResponse asignarMiembroAProyecto(@RequestBody AsignacionProyectoRequest request);
}
