package com.innovatech.equipos_service.adapter;

import com.innovatech.equipos_service.client.ProyectoClient;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.dto.ProyectoResponse;
import com.innovatech.equipos_service.exception.ServicioExternoException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProyectoServiceAdapter {

    private final ProyectoClient proyectoClient;

    @CircuitBreaker(name = "proyectosService", fallbackMethod = "fallbackObtenerProyecto")
    public ProyectoAdaptadoResponse obtenerProyectoAdaptado(Long idProyecto) {
        ProyectoResponse proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);

        return new ProyectoAdaptadoResponse(
                proyecto.id(),
                proyecto.nombre(),
                proyecto.estado()
        );
    }

    public ProyectoAdaptadoResponse fallbackObtenerProyecto(Long idProyecto, Throwable error) {
        throw new ServicioExternoException(
                "No fue posible validar el proyecto con ID: " + idProyecto +
                        ". El servicio de proyectos no está disponible."
        );
    }
}