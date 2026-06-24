package com.innovatech.bff_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bffOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Innovatech - BFF Service API")
                        .version("1.0.0")
                        .description("API orientada al frontend para agregar datos de proyectos, tareas y equipos."));
    }
}
