package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.TareaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "tareas-service",
        url = "${innovatech.services.tareas}"
)
public interface TareaClient {

    @GetMapping("/api/v1/tareas/proyecto/{idProyecto}")
    List<TareaResponseDTO> listarTareasPorProyecto(@PathVariable("idProyecto") Long idProyecto);
}