package com.innovatech.equipos_service.client;

import com.innovatech.equipos_service.dto.ProyectoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * Este cliente se comunica con proyectos-service usando Eureka.
 * El nombre debe coincidir con:
 * spring.application.name=proyectos-service
 */

@FeignClient(name = "proyectos-service")
public interface ProyectoClient {

    @GetMapping("/api/v1/proyectos/{id}")
    ProyectoResponse obtenerProyectoPorId(@PathVariable("id") Long id);
}