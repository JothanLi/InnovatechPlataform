package com.innovatech.proyectos_service.integration;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import com.innovatech.proyectos_service.service.ProyectoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:proyectos_it;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "jwt.secret=12345678901234567890123456789012345678901234567890"
})
@Transactional
class ProyectoServiceIntegrationTest {

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @BeforeEach
    void setUp() {
        proyectoRepository.deleteAll();
    }

    @Test
    void crearProyecto_deberiaPersistirEnH2YPermitirBuscarPorEstado() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Sistema de Gestión Innovatech")
                .descripcion("Proyecto de integración usando H2")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 1))
                .build();

        ProyectoResponseDTO creado = proyectoService.crearProyecto(request);

        assertNotNull(creado.getId());
        assertEquals("Sistema de Gestión Innovatech", creado.getNombre());
        assertEquals(EstadoProyecto.PLANNED, creado.getEstado());
        assertEquals(1, proyectoRepository.count());

        List<ProyectoResponseDTO> proyectosPlaneados =
                proyectoService.buscarPorEstado(EstadoProyecto.PLANNED);

        assertEquals(1, proyectosPlaneados.size());
        assertEquals(creado.getId(), proyectosPlaneados.get(0).getId());
    }

    @Test
    void crearProyecto_deberiaRechazarNombreDuplicadoUsandoH2() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Proyecto Duplicado")
                .descripcion("Primer proyecto")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 1))
                .build();

        proyectoService.crearProyecto(request);

        ProyectoRequestDTO requestDuplicado = ProyectoRequestDTO.builder()
                .nombre("Proyecto Duplicado")
                .descripcion("Segundo proyecto")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 7, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 20))
                .build();

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.crearProyecto(requestDuplicado)
        );

        assertEquals(
                "Ya existe un proyecto registrado con el nombre: Proyecto Duplicado",
                exception.getMessage()
        );
        assertEquals(1, proyectoRepository.count());
    }
}