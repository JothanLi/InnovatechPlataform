package com.innovatech.proyectos_service.client;

import com.innovatech.proyectos_service.dto.TareasPendientesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "tareas-service",
        url = "${innovatech.services.tareas}"
)
public interface TareaClient {

    @GetMapping("/api/v1/tareas/proyecto/{idProyecto}/pendientes")
    TareasPendientesResponse existenTareasPendientes(@PathVariable("idProyecto") Long idProyecto);
}
