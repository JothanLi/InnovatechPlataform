package com.innovatech.equipos_service.e2e;

import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
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
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:equipos_e2e_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.openfeign.client.enabled=false",
                "jwt.secret=innovatech_super_secret_key_2026_para_jwt_seguro_123456789"
        }
)
@AutoConfigureTestRestTemplate
class EquiposServiceE2ETest {

    private static final String JWT_SECRET =
            "innovatech_super_secret_key_2026_para_jwt_seguro_123456789";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MiembroEquipoRepository miembroEquipoRepository;

    @Autowired
    private AsignacionProyectoRepository asignacionProyectoRepository;

    @MockitoBean
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @BeforeEach
    void limpiarBaseDeDatos() {
        asignacionProyectoRepository.deleteAll();
        miembroEquipoRepository.deleteAll();
    }

    @Test
    void e2e_001_deberiaBloquearCreacionDeMiembroSinToken() {
        String body = """
                {
                  "nombres": "Sebastian",
                  "apellidoPaterno": "Mariqueo",
                  "apellidoMaterno": "Perez",
                  "email": "sebastian.sin.token@innovatech.cl",
                  "rol": "DEVELOPER",
                  "password": "Sebastian123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/v1/equipos/miembros"),
                request,
                String.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.FORBIDDEN ||
                        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );

        assertEquals(0, miembroEquipoRepository.count());
    }

    @Test
    void e2e_002_adminConTokenDebeCrearMiembroYPermitirConsultarloPorEmail() {
        String email = "camila.e2e@innovatech.cl";

        String body = """
                {
                  "nombres": "Camila",
                  "apellidoPaterno": "Torres",
                  "apellidoMaterno": "Rojas",
                  "email": "%s",
                  "rol": "QA",
                  "password": "Camila123"
                }
                """.formatted(email);

        ResponseEntity<Map> crearResponse = restTemplate.exchange(
                url("/api/v1/equipos/miembros"),
                HttpMethod.POST,
                new HttpEntity<>(body, headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.CREATED, crearResponse.getStatusCode());
        assertNotNull(crearResponse.getBody());

        assertEquals("Camila", crearResponse.getBody().get("nombres"));
        assertEquals("Torres", crearResponse.getBody().get("apellidoPaterno"));
        assertEquals("Rojas", crearResponse.getBody().get("apellidoMaterno"));
        assertEquals(email, crearResponse.getBody().get("email"));
        assertEquals("QA", crearResponse.getBody().get("rol"));
        assertEquals("ACTIVO", crearResponse.getBody().get("estado"));

        assertEquals(1, miembroEquipoRepository.count());

        ResponseEntity<Map> authResponse = restTemplate.getForEntity(
                url("/api/v1/equipos/miembros/auth/" + email),
                Map.class
        );

        assertEquals(HttpStatus.OK, authResponse.getStatusCode());
        assertNotNull(authResponse.getBody());

        assertEquals(email, authResponse.getBody().get("email"));
        assertEquals("QA", authResponse.getBody().get("rol"));
        assertEquals("ACTIVO", authResponse.getBody().get("estado"));

        String passwordHash = String.valueOf(authResponse.getBody().get("passwordHash"));

        assertNotNull(passwordHash);
        assertFalse(passwordHash.isBlank());
        assertNotEquals("Camila123", passwordHash);
    }

    @Test
    void e2e_003_adminDebeCrearMiembroAsignarloAProyectoYListarAsignaciones() {
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(99L))
                .thenReturn(new ProyectoAdaptadoResponse(
                        99L,
                        "Sistema de Gestión de Proyectos",
                        "IN_PROGRESS"
                ));

        String miembroBody = """
                {
                  "nombres": "Jorge",
                  "apellidoPaterno": "Salazar",
                  "apellidoMaterno": "Parra",
                  "email": "jorge.e2e@innovatech.cl",
                  "rol": "DEVOPS",
                  "password": "Jorge123"
                }
                """;

        ResponseEntity<Map> crearMiembroResponse = restTemplate.exchange(
                url("/api/v1/equipos/miembros"),
                HttpMethod.POST,
                new HttpEntity<>(miembroBody, headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.CREATED, crearMiembroResponse.getStatusCode());
        assertNotNull(crearMiembroResponse.getBody());

        Number idMiembroNumber = (Number) crearMiembroResponse.getBody().get("id");
        Long idMiembro = idMiembroNumber.longValue();

        String asignacionBody = """
                {
                  "idProyecto": 99,
                  "idMiembro": %d
                }
                """.formatted(idMiembro);

        ResponseEntity<Map> asignarResponse = restTemplate.exchange(
                url("/api/v1/equipos/asignaciones"),
                HttpMethod.POST,
                new HttpEntity<>(asignacionBody, headersConTokenAdmin()),
                Map.class
        );

        assertEquals(HttpStatus.CREATED, asignarResponse.getStatusCode());
        assertNotNull(asignarResponse.getBody());

        assertEquals(99L, ((Number) asignarResponse.getBody().get("idProyecto")).longValue());
        assertEquals("Sistema de Gestión de Proyectos", asignarResponse.getBody().get("nombreProyecto"));
        assertEquals(idMiembro, ((Number) asignarResponse.getBody().get("idMiembro")).longValue());
        assertEquals("Jorge Salazar Parra", asignarResponse.getBody().get("nombreMiembro"));
        assertEquals("DEVOPS", asignarResponse.getBody().get("rolMiembro"));

        assertEquals(1, asignacionProyectoRepository.count());

        ResponseEntity<Object[]> listarResponse = restTemplate.getForEntity(
                url("/api/v1/equipos/asignaciones/proyecto/99"),
                Object[].class
        );

        assertEquals(HttpStatus.OK, listarResponse.getStatusCode());
        assertNotNull(listarResponse.getBody());
        assertEquals(1, listarResponse.getBody().length);
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