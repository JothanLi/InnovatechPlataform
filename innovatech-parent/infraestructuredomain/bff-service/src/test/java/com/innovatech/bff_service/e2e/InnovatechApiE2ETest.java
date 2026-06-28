package com.innovatech.bff_service.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InnovatechApiE2ETest {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /*
     * Estos valores se pueden sobreescribir al ejecutar Maven:
     *
     * -De2e.gateway.url=http://localhost:8090
     * -De2e.admin.username=admin@innovatech.cl
     * -De2e.admin.password=Admin123456
     */
    private static final String GATEWAY_URL =
            System.getProperty("e2e.gateway.url", "http://localhost:8090");

    private static final String ADMIN_USERNAME =
            System.getProperty("e2e.admin.username", "admin@innovatech.cl");

    private static final String ADMIN_PASSWORD =
            System.getProperty("e2e.admin.password", "Admin123");

    private static String adminToken;

    @BeforeAll
    static void setUp() throws Exception {
        verificarSistemaLevantado();
        adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertNotNull(adminToken);
        assertFalse(adminToken.isBlank());
    }

    @Test
    @Order(1)
    void e2e_adminCreaProyectoCreaMiembroAsignaMiembroCreaTareaYConsultaDetalle() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        JsonNode proyectoCreado = postJson(
                "/api/v1/bff/proyectos",
                adminToken,
                Map.of(
                        "nombre", "E2E Proyecto Completo " + sufijo,
                        "descripcion", "Proyecto creado desde prueba End to End",
                        "estado", "IN_PROGRESS",
                        "fechaInicio", LocalDate.now().toString(),
                        "fechaFinEstimada", LocalDate.now().plusMonths(2).toString()
                ),
                201
        );

        Long idProyecto = proyectoCreado.get("id").asLong();

        assertNotNull(idProyecto);
        assertEquals("IN_PROGRESS", proyectoCreado.get("estado").asText());

        JsonNode miembroCreado = postJson(
                "/api/v1/bff/miembros",
                adminToken,
                Map.of(
                        "nombres", "UsuarioE2E",
                        "apellidoPaterno", "Flujo",
                        "apellidoMaterno", sufijo,
                        "email", "usuario.e2e." + sufijo + "@innovatech.cl",
                        "rol", "DEVELOPER",
                        "password", "Password123"
                ),
                201
        );

        Long idMiembro = miembroCreado.get("id").asLong();

        assertNotNull(idMiembro);
        assertEquals("DEVELOPER", miembroCreado.get("rol").asText());

        JsonNode asignacionCreada = postJson(
                "/api/v1/bff/asignaciones",
                adminToken,
                Map.of(
                        "idProyecto", idProyecto,
                        "idMiembro", idMiembro
                ),
                201
        );

        String nombreResponsable = asignacionCreada.get("nombreMiembro").asText();

        assertEquals(idProyecto.longValue(), asignacionCreada.get("idProyecto").asLong());
        assertEquals(idMiembro.longValue(), asignacionCreada.get("idMiembro").asLong());
        assertTrue(nombreResponsable.contains("UsuarioE2E"));

        JsonNode tareaCreada = postJson(
                "/api/v1/bff/tareas",
                adminToken,
                Map.of(
                        "descripcion", "E2E implementar tarea crítica " + sufijo,
                        "estado", "PENDING",
                        "idProyecto", idProyecto,
                        "responsable", nombreResponsable,
                        "fechaInicio", LocalDate.now().toString(),
                        "fechaFinEstimada", LocalDate.now().plusWeeks(1).toString()
                ),
                201
        );

        assertNotNull(tareaCreada.get("id").asLong());
        assertEquals("PENDING", tareaCreada.get("estado").asText());
        assertEquals(idProyecto.longValue(), tareaCreada.get("idProyecto").asLong());
        assertEquals(nombreResponsable, tareaCreada.get("responsable").asText());

        JsonNode detalle = getJson(
                "/api/v1/bff/proyectos/" + idProyecto + "/detalle",
                adminToken,
                200
        );

        assertEquals(idProyecto.longValue(), detalle.get("proyecto").get("id").asLong());
        assertEquals("E2E Proyecto Completo " + sufijo, detalle.get("proyecto").get("nombre").asText());

        assertTrue(detalle.get("tareas").isArray());
        assertTrue(detalle.get("tareas").size() >= 1);

        assertTrue(detalle.get("miembrosAsignados").isArray());
        assertTrue(detalle.get("miembrosAsignados").size() >= 1);

        assertEquals(1, detalle.get("avance").get("totalTareas").asInt());
        assertEquals(1, detalle.get("avance").get("tareasPendientes").asInt());
    }

    @Test
    @Order(2)
    void e2e_noDebePermitirCrearTareaEnProyectoCancelado() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        JsonNode proyectoCancelado = postJson(
                "/api/v1/bff/proyectos",
                adminToken,
                Map.of(
                        "nombre", "E2E Proyecto Cancelado " + sufijo,
                        "descripcion", "Proyecto usado para validar regla de negocio",
                        "estado", "CANCELLED",
                        "fechaInicio", LocalDate.now().toString(),
                        "fechaFinEstimada", LocalDate.now().plusMonths(1).toString()
                ),
                201
        );

        Long idProyecto = proyectoCancelado.get("id").asLong();

        JsonNode respuestaError = postJson(
                "/api/v1/bff/tareas",
                adminToken,
                Map.of(
                        "descripcion", "Tarea que no debería crearse",
                        "estado", "PENDING",
                        "idProyecto", idProyecto,
                        "responsable", "Responsable inexistente",
                        "fechaInicio", LocalDate.now().toString(),
                        "fechaFinEstimada", LocalDate.now().plusDays(5).toString()
                ),
                400
        );

        String body = respuestaError.toString().toLowerCase();

        assertTrue(
                body.contains("cancelado")
                        || body.contains("finalizado")
                        || body.contains("no se pueden asignar"),
                "La respuesta debe indicar que no se pueden crear tareas en proyectos cancelados o finalizados"
        );
    }

    @Test
    @Order(3)
    void e2e_usuarioSinRolAdminNoDebeCrearProyecto() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        JsonNode miembroCreado = postJson(
                "/api/v1/bff/miembros",
                adminToken,
                Map.of(
                        "nombres", "NoAdminE2E",
                        "apellidoPaterno", "Usuario",
                        "apellidoMaterno", sufijo,
                        "email", "noadmin.e2e." + sufijo + "@innovatech.cl",
                        "rol", "DEVELOPER",
                        "password", "Password123"
                ),
                201
        );

        String emailNoAdmin = miembroCreado.get("email").asText();

        String tokenNoAdmin = login(emailNoAdmin, "Password123");

        JsonNode respuestaError = postJson(
                "/api/v1/bff/proyectos",
                tokenNoAdmin,
                Map.of(
                        "nombre", "Proyecto prohibido " + sufijo,
                        "descripcion", "Este proyecto no debería crearse porque el usuario no es admin",
                        "estado", "PLANNED",
                        "fechaInicio", LocalDate.now().toString(),
                        "fechaFinEstimada", LocalDate.now().plusMonths(1).toString()
                ),
                403
        );

        assertNotNull(respuestaError);
    }

    private static void verificarSistemaLevantado() throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GATEWAY_URL + "/actuator/health"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            assertTrue(
                    response.statusCode() >= 200 && response.statusCode() < 500,
                    "El API Gateway no respondió correctamente. Verifica que Docker esté levantado."
            );
        } catch (ConnectException exception) {
            fail("""
                    No se pudo conectar al API Gateway.
                    Antes de ejecutar los E2E levanta el sistema con:
                    
                    docker compose up -d --build
                    """);
        }
    }

    private static String login(String username, String password) throws Exception {
        JsonNode response = postJson(
                "/api/v1/auth/login",
                null,
                Map.of(
                        "username", username,
                        "password", password
                ),
                200
        );

        assertTrue(
                response.hasNonNull("accessToken"),
                "La respuesta de login debe contener accessToken"
        );

        return response.get("accessToken").asText();
    }

    private static JsonNode getJson(
            String path,
            String token,
            int expectedStatus
    ) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .GET()
                .header("Accept", "application/json");

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(
                expectedStatus,
                response.statusCode(),
                "Respuesta inesperada para GET " + path + ". Body: " + response.body()
        );

        return parseJson(response.body());
    }

    private static JsonNode postJson(
            String path,
            String token,
            Object body,
            int expectedStatus
    ) throws Exception {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(
                expectedStatus,
                response.statusCode(),
                "Respuesta inesperada para POST " + path + ". Body: " + response.body()
        );

        return parseJson(response.body());
    }

    private static JsonNode parseJson(String body) throws IOException {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }

        return objectMapper.readTree(body);
    }
}