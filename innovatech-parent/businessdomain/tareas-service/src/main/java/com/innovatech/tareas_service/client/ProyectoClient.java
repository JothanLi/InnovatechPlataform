package com.innovatech.tareas_service.client;

import com.innovatech.tareas_service.dto.ProyectoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "proyectos-service")
public interface ProyectoClient {

    @GetMapping("/api/v1/proyectos/{id}")
    ProyectoResponse obtenerProyectoPorId(@PathVariable("id") Long id);

    @PatchMapping("/api/v1/proyectos/{id}/estado")
    ProyectoResponse cambiarEstadoProyecto(
            @PathVariable("id") Long id,
            @RequestBody CambioEstadoProyectoRequest request
    );

    record CambioEstadoProyectoRequest(String estado) {
    }
}