package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.TareaRequestDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "tareas-service",
        url = "${innovatech.services.tareas}"
)
public interface TareaClient {

    @GetMapping("/api/v1/tareas/proyecto/{idProyecto}")
    List<TareaResponseDTO> listarTareasPorProyecto(@PathVariable("idProyecto") Long idProyecto);

    @PostMapping("/api/v1/tareas")
    TareaResponseDTO crearTarea(@RequestBody TareaRequestDTO request);

    @PatchMapping("/api/v1/tareas/{id}/estado")
    TareaResponseDTO cambiarEstadoTarea(
            @PathVariable("id") Long id,
            @RequestBody CambioEstadoTareaRequest request
    );

    record CambioEstadoTareaRequest(String estado) {
    }
}
