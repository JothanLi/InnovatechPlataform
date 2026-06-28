package com.innovatech.tareas_service.integration;

import com.innovatech.tareas_service.adapter.ProyectoServiceAdapter;
import com.innovatech.tareas_service.client.ProyectoClient;
import com.innovatech.tareas_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.model.Tarea;
import com.innovatech.tareas_service.repository.TareaRepository;
import com.innovatech.tareas_service.service.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tareas_it;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
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
class TareaServiceIntegrationTest {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private TareaRepository tareaRepository;

    @MockitoBean
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @MockitoBean
    private ProyectoClient proyectoClient;

    @BeforeEach
    void setUp() {
        tareaRepository.deleteAll();

        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L))
                .thenReturn(new ProyectoAdaptadoResponse(
                        10L,
                        "Plataforma Innovatech",
                        "IN_PROGRESS"
                ));
    }

    @Test
    void crearTarea_deberiaPersistirEnH2YRelacionarseConProyectoAdaptado() {
        TareaRequestDTO request = TareaRequestDTO.builder()
                .descripcion("Implementar pruebas de integración")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        TareaResponseDTO creada = tareaService.crearTarea(request);

        assertNotNull(creada.getId());
        assertEquals("Implementar pruebas de integración", creada.getDescripcion());
        assertEquals(EstadoTarea.PENDING, creada.getEstado());
        assertEquals(10L, creada.getIdProyecto());
        assertEquals("Plataforma Innovatech", creada.getNombreProyecto());
        assertEquals("Sebastian", creada.getResponsable());

        Optional<Tarea> tareaPersistida = tareaRepository.findById(creada.getId());

        assertTrue(tareaPersistida.isPresent());
        assertEquals("Implementar pruebas de integración", tareaPersistida.get().getDescripcion());
        assertEquals(1, tareaRepository.count());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
    }

    @Test
    void buscarPorProyecto_deberiaRetornarTareasPersistidasEnH2() {
        Tarea tarea = Tarea.builder()
                .descripcion("Tarea guardada directamente en H2")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        tareaRepository.save(tarea);

        List<TareaResponseDTO> tareas = tareaService.buscarPorProyecto(10L);

        assertEquals(1, tareas.size());
        assertEquals("Tarea guardada directamente en H2", tareas.get(0).getDescripcion());
        assertEquals("Plataforma Innovatech", tareas.get(0).getNombreProyecto());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
    }

    @Test
    void cambiarEstadoAInProgress_deberiaPersistirCambioYSincronizarProyecto() {
        Tarea tarea = Tarea.builder()
                .descripcion("Cambiar estado de tarea")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        Tarea guardada = tareaRepository.save(tarea);

        TareaResponseDTO actualizada =
                tareaService.cambiarEstado(guardada.getId(), EstadoTarea.IN_PROGRESS);

        assertEquals(EstadoTarea.IN_PROGRESS, actualizada.getEstado());

        Tarea tareaPersistida = tareaRepository.findById(guardada.getId()).orElseThrow();

        assertEquals(EstadoTarea.IN_PROGRESS, tareaPersistida.getEstado());

        verify(proyectoClient, times(1)).cambiarEstadoProyecto(
                eq(10L),
                any(ProyectoClient.CambioEstadoProyectoRequest.class)
        );
    }
}