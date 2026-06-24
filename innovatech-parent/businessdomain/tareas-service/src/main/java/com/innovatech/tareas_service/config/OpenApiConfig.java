package com.innovatech.tareas_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tareasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Innovatech - Tareas Service API")
                        .version("1.0.0")
                        .description("API REST para crear, consultar, actualizar, asignar y cambiar estado de tareas asociadas a proyectos."));
    }
}
