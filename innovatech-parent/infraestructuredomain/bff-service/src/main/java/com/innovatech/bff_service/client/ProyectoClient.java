package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "proyectos-service",
        url = "${innovatech.services.proyectos}"
)
public interface ProyectoClient {

    @GetMapping("/api/v1/proyectos")
    List<ProyectoResponseDTO> listarProyectos();

    @GetMapping("/api/v1/proyectos/{id}")
    ProyectoResponseDTO obtenerProyectoPorId(@PathVariable("id") Long id);
}