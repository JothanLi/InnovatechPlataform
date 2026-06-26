package com.innovatech.proyectos_service.integration;

import com.innovatech.proyectos_service.facade.TareaServiceFacade;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:proyectos_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
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
})
@AutoConfigureMockMvc(addFilters = false)
class ProyectoServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @MockitoBean
    private TareaServiceFacade tareaServiceFacade;

    @BeforeEach
    void limpiarBaseDeDatos() {
        proyectoRepository.deleteAll();
    }

    @Test
    void crearProyecto_deberiaResponder201YGuardarEnBaseDeDatos() throws Exception {
        String body = """
                {
                  "nombre": "Sistema SIGP Innovatech",
                  "descripcion": "Plataforma para gestión de proyectos tecnológicos",
                  "estado": "PLANNED",
                  "fechaInicio": "2026-06-01",
                  "fechaFinEstimada": "2026-12-31"
                }
                """;

        mockMvc.perform(post("/api/v1/proyectos")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Sistema SIGP Innovatech"))
                .andExpect(jsonPath("$.descripcion").value("Plataforma para gestión de proyectos tecnológicos"))
                .andExpect(jsonPath("$.estado").value("PLANNED"))
                .andExpect(jsonPath("$.fechaInicio").value("2026-06-01"))
                .andExpect(jsonPath("$.fechaFinEstimada").value("2026-12-31"));

        assertEquals(1, proyectoRepository.count());

        Proyecto proyectoGuardado = proyectoRepository.findAll().get(0);

        assertEquals("Sistema SIGP Innovatech", proyectoGuardado.getNombre());
        assertEquals(EstadoProyecto.PLANNED, proyectoGuardado.getEstado());
        assertEquals(LocalDate.of(2026, 6, 1), proyectoGuardado.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 31), proyectoGuardado.getFechaFinEstimada());
    }

    @Test
    void crearProyecto_deberiaResponder400CuandoNombreYaExiste() throws Exception {
        Proyecto proyectoExistente = Proyecto.builder()
                .nombre("Dashboard Ejecutivo BI")
                .descripcion("Proyecto existente")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 1, 10))
                .fechaFinEstimada(LocalDate.of(2026, 7, 30))
                .build();

        proyectoRepository.save(proyectoExistente);

        String body = """
                {
                  "nombre": "Dashboard Ejecutivo BI",
                  "descripcion": "Proyecto duplicado",
                  "estado": "PLANNED",
                  "fechaInicio": "2026-02-01",
                  "fechaFinEstimada": "2026-08-01"
                }
                """;

        mockMvc.perform(post("/api/v1/proyectos")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Regla de negocio inválida"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un proyecto registrado con el nombre: Dashboard Ejecutivo BI"))
                .andExpect(jsonPath("$.path").value("/api/v1/proyectos"));

        assertEquals(1, proyectoRepository.count());
    }

    @Test
    void cambiarEstado_deberiaResponder400CuandoProyectoTieneTareasPendientes() throws Exception {
        Proyecto proyecto = Proyecto.builder()
                .nombre("Migración Cloud")
                .descripcion("Migración de infraestructura a la nube")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 31))
                .build();

        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        when(tareaServiceFacade.existenTareasPendientes(proyectoGuardado.getId()))
                .thenReturn(true);

        String body = """
                {
                  "estado": "COMPLETED"
                }
                """;

        mockMvc.perform(patch("/api/v1/proyectos/" + proyectoGuardado.getId() + "/estado")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Regla de negocio inválida"))
                .andExpect(jsonPath("$.mensaje").value("No se puede finalizar el proyecto porque existen tareas pendientes"))
                .andExpect(jsonPath("$.path").value("/api/v1/proyectos/" + proyectoGuardado.getId() + "/estado"));

        Proyecto proyectoActual = proyectoRepository.findById(proyectoGuardado.getId())
                .orElseThrow();

        assertEquals(EstadoProyecto.IN_PROGRESS, proyectoActual.getEstado());
        assertEquals(1, proyectoRepository.count());
    }
}