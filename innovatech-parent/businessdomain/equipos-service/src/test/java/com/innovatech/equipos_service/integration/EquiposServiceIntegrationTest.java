package com.innovatech.equipos_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.model.RolEquipo;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equipos_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.client.enabled=false",
        "jwt.secret=innovatech_super_secret_key_2026_para_jwt_seguro_123456789"
})
@AutoConfigureMockMvc(addFilters = false)
class EquiposServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MiembroEquipoRepository miembroEquipoRepository;

    @Autowired
    private AsignacionProyectoRepository asignacionProyectoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @BeforeEach
    void limpiarBaseDeDatos() {
        asignacionProyectoRepository.deleteAll();
        miembroEquipoRepository.deleteAll();
    }

    @Test
    void crearMiembro_deberiaResponder201GuardarMiembroYEncriptarPassword() throws Exception {
        String body = """
                {
                  "nombres": "Sebastian",
                  "apellidoPaterno": "Mariqueo",
                  "apellidoMaterno": "Perez",
                  "email": "sebastian.mariqueo@innovatech.cl",
                  "rol": "DEVELOPER",
                  "password": "Sebastian123"
                }
                """;

        mockMvc.perform(post("/api/v1/equipos/miembros")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombres").value("Sebastian"))
                .andExpect(jsonPath("$.apellidoPaterno").value("Mariqueo"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Perez"))
                .andExpect(jsonPath("$.email").value("sebastian.mariqueo@innovatech.cl"))
                .andExpect(jsonPath("$.rol").value("DEVELOPER"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        MiembroEquipo miembroGuardado = miembroEquipoRepository
                .findByEmail("sebastian.mariqueo@innovatech.cl")
                .orElseThrow();

        assertEquals("Sebastian", miembroGuardado.getNombres());
        assertEquals(RolEquipo.DEVELOPER, miembroGuardado.getRol());
        assertEquals(EstadoMiembro.ACTIVO, miembroGuardado.getEstado());

        assertNotEquals("Sebastian123", miembroGuardado.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("Sebastian123", miembroGuardado.getPasswordHash()));
    }

    @Test
    void crearMiembro_deberiaResponder400CuandoEmailYaExiste() throws Exception {
        MiembroEquipo miembroExistente = MiembroEquipo.builder()
                .nombres("Camila")
                .apellidoPaterno("Torres")
                .apellidoMaterno("Rojas")
                .email("camila.torres@innovatech.cl")
                .passwordHash(new BCryptPasswordEncoder().encode("Camila123"))
                .rol(RolEquipo.QA)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        miembroEquipoRepository.save(miembroExistente);

        String body = """
                {
                  "nombres": "Camila",
                  "apellidoPaterno": "Torres",
                  "apellidoMaterno": "Rojas",
                  "email": "camila.torres@innovatech.cl",
                  "rol": "QA",
                  "password": "Camila123"
                }
                """;

        mockMvc.perform(post("/api/v1/equipos/miembros")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Regla de negocio"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un miembro con el email: camila.torres@innovatech.cl"))
                .andExpect(jsonPath("$.ruta").value("/api/v1/equipos/miembros"));

        assertEquals(1, miembroEquipoRepository.findAll().size());
    }

    @Test
    void asignarMiembroAProyecto_deberiaResponder201GuardarAsignacionYRetornarDatosCombinados() throws Exception {
        MiembroEquipo miembro = MiembroEquipo.builder()
                .nombres("Jorge")
                .apellidoPaterno("Salazar")
                .apellidoMaterno("Parra")
                .email("jorge.salazar@innovatech.cl")
                .passwordHash(new BCryptPasswordEncoder().encode("Jorge123"))
                .rol(RolEquipo.DEVOPS)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        MiembroEquipo miembroGuardado = miembroEquipoRepository.save(miembro);

        when(proyectoServiceAdapter.obtenerProyectoAdaptado(77L))
                .thenReturn(new ProyectoAdaptadoResponse(
                        77L,
                        "Migración Cloud Innovatech",
                        "IN_PROGRESS"
                ));

        String body = """
                {
                  "idProyecto": 77,
                  "idMiembro": %d
                }
                """.formatted(miembroGuardado.getId());

        mockMvc.perform(post("/api/v1/equipos/asignaciones")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.idProyecto").value(77))
                .andExpect(jsonPath("$.nombreProyecto").value("Migración Cloud Innovatech"))
                .andExpect(jsonPath("$.idMiembro").value(miembroGuardado.getId()))
                .andExpect(jsonPath("$.nombreMiembro").value("Jorge Salazar Parra"))
                .andExpect(jsonPath("$.rolMiembro").value("DEVOPS"))
                .andExpect(jsonPath("$.fechaAsignacion").exists());

        assertTrue(
                asignacionProyectoRepository.existsByIdProyectoAndMiembroId(
                        77L,
                        miembroGuardado.getId()
                )
        );

        assertEquals(1, asignacionProyectoRepository.findAll().size());
    }
}