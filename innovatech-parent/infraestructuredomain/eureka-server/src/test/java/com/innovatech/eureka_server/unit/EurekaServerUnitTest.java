package com.innovatech.eureka_server.unit;

import com.innovatech.eureka_server.EurekaServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EurekaServerUnitTest {

    @Test
    void aplicacion_deberiaTenerAnotacionEnableEurekaServer() {
        boolean tieneAnotacion = EurekaServerApplication.class
                .isAnnotationPresent(EnableEurekaServer.class);

        assertTrue(tieneAnotacion, "La aplicación debe estar anotada con @EnableEurekaServer");
    }

    @Test
    void aplicacion_deberiaTenerAnotacionSpringBootApplication() {
        boolean tieneAnotacion = EurekaServerApplication.class
                .isAnnotationPresent(SpringBootApplication.class);

        assertTrue(tieneAnotacion, "La aplicación debe estar anotada con @SpringBootApplication");
    }

    @Test
    void applicationProperties_deberiaTenerConfiguracionCriticaDeEurekaServer() throws IOException {
        Properties properties = cargarApplicationProperties();

        assertEquals("eureka-server", properties.getProperty("spring.application.name"));
        assertEquals("8761", properties.getProperty("server.port"));

        assertEquals("false", properties.getProperty("eureka.client.register-with-eureka"));
        assertEquals("false", properties.getProperty("eureka.client.fetch-registry"));

        assertEquals("false", properties.getProperty("eureka.server.enable-self-preservation"));
        assertEquals("always", properties.getProperty("management.endpoint.health.show-details"));
    }

    private Properties cargarApplicationProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            assertNotNull(inputStream, "No se encontró application.properties");
            properties.load(inputStream);
        }

        return properties;
    }
}