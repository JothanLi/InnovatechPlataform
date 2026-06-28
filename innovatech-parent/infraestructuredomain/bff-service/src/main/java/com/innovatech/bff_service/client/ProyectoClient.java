package com.innovatech.bff_service.client;

import com.innovatech.bff_service.dto.ProyectoRequestDTO;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "proyectos-service",
        url = "${innovatech.services.proyectos:http://proyectos-service:8081}"
)
public interface ProyectoClient {

    @GetMapping("/api/v1/proyectos")
    List<ProyectoResponseDTO> listarProyectos();

    @GetMapping("/api/v1/proyectos/{id}")
    ProyectoResponseDTO obtenerProyectoPorId(@PathVariable("id") Long id);

    @PostMapping("/api/v1/proyectos")
    ProyectoResponseDTO crearProyecto(@RequestBody ProyectoRequestDTO request);

    /*
     * IMPORTANTE:
     * El frontend sigue llamando PATCH al BFF.
     * Pero el BFF llama con PUT a proyectos-service para evitar problemas de Feign con PATCH.
     */
    @PutMapping(
            value = "/api/v1/proyectos/{id}/estado",
            consumes = "application/json"
    )
    ProyectoResponseDTO cambiarEstadoProyecto(
            @PathVariable("id") Long id,
            @RequestBody CambioEstadoProyectoRequest request
    );

    class CambioEstadoProyectoRequest {
        private String estado;

        public CambioEstadoProyectoRequest() {
        }

        public CambioEstadoProyectoRequest(String estado) {
            this.estado = estado;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}