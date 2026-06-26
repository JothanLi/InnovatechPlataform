package com.innovatech.eureka_server.integration;

import com.innovatech.eureka_server.EurekaServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "server.port=0",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "eureka.server.enable-self-preservation=false",
        "management.endpoints.web.exposure.include=health,info,metrics,env,beans,loggers,threaddump,httpexchanges",
        "management.endpoint.health.show-details=always",
        "management.info.env.enabled=true",
        "info.app.name=eureka-server",
        "info.app.description=Microservicio de la plataforma Innovatech",
        "info.app.version=1.0.0"
})
@AutoConfigureMockMvc
class EurekaServerIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contexto_deberiaLevantarComoServidorEureka() {
        assertNotNull(environment);

        assertTrue(
                EurekaServerApplication.class.isAnnotationPresent(EnableEurekaServer.class),
                "La aplicación debe estar configurada como servidor Eureka"
        );
    }

    @Test
    void environment_deberiaCargarConfiguracionCriticaDeEureka() {
        assertEquals("false", environment.getProperty("eureka.client.register-with-eureka"));
        assertEquals("false", environment.getProperty("eureka.client.fetch-registry"));
        assertEquals("false", environment.getProperty("eureka.server.enable-self-preservation"));

        assertEquals("always", environment.getProperty("management.endpoint.health.show-details"));
        assertEquals("true", environment.getProperty("management.info.env.enabled"));
    }

    @Test
    void actuatorHealth_deberiaResponderOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"UP\"")));
    }
}