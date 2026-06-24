package com.innovatech.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient equiposRestClient(
            RestClient.Builder builder,
            @Value("${innovatech.services.equipos.url}") String equiposServiceUrl
    ) {
        return builder
                .baseUrl(equiposServiceUrl)
                .build();
    }
}
