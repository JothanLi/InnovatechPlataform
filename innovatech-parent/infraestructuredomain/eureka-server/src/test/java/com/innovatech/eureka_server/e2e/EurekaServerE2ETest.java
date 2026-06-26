package com.innovatech.eureka_server.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",
                "eureka.server.enable-self-preservation=false",
                "management.endpoints.web.exposure.include=health,info,metrics,env,beans,loggers,threaddump,httpexchanges",
                "management.endpoint.health.show-details=always",
                "management.info.env.enabled=true",
                "info.app.name=eureka-server",
                "info.app.description=Microservicio de la plataforma Innovatech",
                "info.app.version=1.0.0"
        }
)
class EurekaServerE2ETest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void e2e_001_actuatorHealthDebeResponderUp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url("/actuator/health")))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void e2e_002_actuatorInfoDebeResponderInformacionDeLaApp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url("/actuator/info")))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        assertTrue(response.body().contains("eureka-server"));
        assertTrue(response.body().contains("Microservicio de la plataforma Innovatech"));
        assertTrue(response.body().contains("1.0.0"));
    }

    @Test
    void e2e_003_endpointEurekaAppsDebeResponderListaDeAplicaciones() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url("/eureka/apps")))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());

        String body = response.body().toLowerCase();

        assertTrue(
                body.contains("applications") || body.contains("application"),
                "La respuesta debe contener la estructura de aplicaciones de Eureka"
        );
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}