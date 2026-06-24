package com.innovatech.equipos_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI equiposOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Innovatech - Equipos Service API")
                        .version("1.0.0")
                        .description("API REST para registrar miembros, consultar equipos y asociar miembros a proyectos."));
    }
}
