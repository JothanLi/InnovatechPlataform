package com.innovatech.proyectos_service.facade;

import com.innovatech.proyectos_service.client.TareaClient;
import com.innovatech.proyectos_service.dto.TareasPendientesResponse;
import com.innovatech.proyectos_service.exception.ServicioExternoException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TareaServiceFacade {

    private final TareaClient tareaClient;

    @CircuitBreaker(name = "tareasService", fallbackMethod = "fallbackExistenPendientes")
    public boolean existenTareasPendientes(Long idProyecto) {
        TareasPendientesResponse response = tareaClient.existenTareasPendientes(idProyecto);
        return response.existenPendientes();
    }

    public boolean fallbackExistenPendientes(Long idProyecto, Throwable error) {
        throw new ServicioExternoException(
                "No fue posible validar las tareas pendientes del proyecto con ID: " + idProyecto
        );
    }
}
