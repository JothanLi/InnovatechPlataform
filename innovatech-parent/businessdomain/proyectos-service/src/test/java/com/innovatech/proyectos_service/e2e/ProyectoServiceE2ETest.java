package com.innovatech.proyectos_service.e2e;

import com.innovatech.proyectos_service.facade.TareaServiceFacade;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:proyectos_e2e_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",

                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.openfeign.client.enabled=false",

                "innovatech.services.tareas=http://localhost:8084",

                "jwt.secret=innovatech_super_secret_key_2026_para_jwt_seguro_123456789"
        }
)
@AutoConfigureTestRestTemplate
class ProyectoServiceE2ETest {

    private static final String JWT_SECRET =
            "innovatech_super_secret_key_2026_para_jwt_seguro_123456789";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @MockitoBean
    private TareaServiceFacade tareaServiceFacade;

    @BeforeEach
    void limpiarBaseDeDatos() {
        proyectoRepository.deleteAll();
    }

    @Test
    void e2e_001_deberiaBloquearCreacionDeProyectoSinToken() {
        String body = """
                {
                  "nombre": "Proyecto Sin Token",
                  "descripcion": "Este proyecto no debería crearse porque no tiene JWT",
                  "estado": "PLANNED",
                  "fechaInicio": "2026-06-01",
                  "fechaFinEstimada": "2026-12-31"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/proyectos"),
                new HttpEntity<>(body, headers),
                String.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.FORBIDDEN ||
                        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );

        assertEquals(0, proyectoRepository.count());
    }

    @Test
    void e2e_002_adminConTokenDebeCrearProyectoYConsultarloPorId() {
        String body = """
                {
                  "nombre": "Sistema SIGP Innovatech E2E",
                  "descripcion": "Plataforma para gestión de proyectos tecnológicos",
                  "estado": "PLANNED",
                  "fechaInicio": "2026-06-01",
                  "fechaFinEstimada": "2026-12-31"
                }
                """;

        ResponseEntity<Map> crearResponse = restTemplate.exchange(
                url("/api/v1/proyectos"),
                HttpMethod.POST,
                new HttpEntity<>(body, headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.CREATED, crearResponse.getStatusCode());
        assertNotNull(crearResponse.getBody());

        assertEquals("Sistema SIGP Innovatech E2E", crearResponse.getBody().get("nombre"));
        assertEquals("Plataforma para gestión de proyectos tecnológicos", crearResponse.getBody().get("descripcion"));
        assertEquals("PLANNED", crearResponse.getBody().get("estado"));
        assertEquals("2026-06-01", crearResponse.getBody().get("fechaInicio"));
        assertEquals("2026-12-31", crearResponse.getBody().get("fechaFinEstimada"));

        Number idProyectoNumber = (Number) crearResponse.getBody().get("id");
        Long idProyecto = idProyectoNumber.longValue();

        assertEquals(1, proyectoRepository.count());

        ResponseEntity<Map> consultarResponse = restTemplate.exchange(
                url("/api/v1/proyectos/" + idProyecto),
                HttpMethod.GET,
                new HttpEntity<>(headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.OK, consultarResponse.getStatusCode());
        assertNotNull(consultarResponse.getBody());

        assertEquals(idProyecto, ((Number) consultarResponse.getBody().get("id")).longValue());
        assertEquals("Sistema SIGP Innovatech E2E", consultarResponse.getBody().get("nombre"));
        assertEquals("PLANNED", consultarResponse.getBody().get("estado"));
    }

    @Test
    void e2e_003_adminDebeCrearProyectoCambiarEstadoACompletadoYPersistirCambio() {
        Proyecto proyecto = Proyecto.builder()
                .nombre("Migración Cloud E2E")
                .descripcion("Migración de infraestructura a la nube")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 31))
                .build();

        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        when(tareaServiceFacade.existenTareasPendientes(proyectoGuardado.getId()))
                .thenReturn(false);

        String body = """
                {
                  "estado": "COMPLETED"
                }
                """;

        ResponseEntity<Map> cambiarEstadoResponse = restTemplate.exchange(
                url("/api/v1/proyectos/" + proyectoGuardado.getId() + "/estado"),
                HttpMethod.PATCH,
                new HttpEntity<>(body, headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.OK, cambiarEstadoResponse.getStatusCode());
        assertNotNull(cambiarEstadoResponse.getBody());

        assertEquals(proyectoGuardado.getId(), ((Number) cambiarEstadoResponse.getBody().get("id")).longValue());
        assertEquals("Migración Cloud E2E", cambiarEstadoResponse.getBody().get("nombre"));
        assertEquals("COMPLETED", cambiarEstadoResponse.getBody().get("estado"));

        Proyecto proyectoActualizado = proyectoRepository.findById(proyectoGuardado.getId())
                .orElseThrow();

        assertEquals(EstadoProyecto.COMPLETED, proyectoActualizado.getEstado());
        assertEquals(1, proyectoRepository.count());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders headersConTokenAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(generarToken("admin@innovatech.cl", "ADMIN"));
        return headers;
    }

    private String generarToken(String username, String role) {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + 1000 * 60 * 60);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}