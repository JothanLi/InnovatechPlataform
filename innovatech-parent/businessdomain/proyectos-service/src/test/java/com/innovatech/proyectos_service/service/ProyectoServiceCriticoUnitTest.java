package com.innovatech.proyectos_service.service;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.facade.TareaServiceFacade;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceCriticoUnitTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TareaServiceFacade tareaServiceFacade;

    private ProyectoService proyectoService;

    @BeforeEach
    void setUp() {
        proyectoService = new ProyectoService(proyectoRepository, tareaServiceFacade);
    }

    @Test
    void crearProyecto_deberiaGuardarProyectoCuandoNombreNoExiste() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Sistema SIGP Innovatech")
                .descripcion("Plataforma para gestión de proyectos tecnológicos")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 31))
                .build();

        Proyecto proyectoGuardado = Proyecto.builder()
                .id(1L)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        when(proyectoRepository.existsByNombreIgnoreCase(request.getNombre())).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoGuardado);

        ProyectoResponseDTO response = proyectoService.crearProyecto(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sistema SIGP Innovatech", response.getNombre());
        assertEquals("Plataforma para gestión de proyectos tecnológicos", response.getDescripcion());
        assertEquals(EstadoProyecto.PLANNED, response.getEstado());
        assertEquals(LocalDate.of(2026, 6, 1), response.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 31), response.getFechaFinEstimada());

        ArgumentCaptor<Proyecto> captor = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoRepository).save(captor.capture());

        Proyecto proyectoCapturado = captor.getValue();

        assertEquals("Sistema SIGP Innovatech", proyectoCapturado.getNombre());
        assertEquals(EstadoProyecto.PLANNED, proyectoCapturado.getEstado());

        verify(proyectoRepository).existsByNombreIgnoreCase("Sistema SIGP Innovatech");
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    @Test
    void crearProyecto_deberiaRechazarNombreDuplicado() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Dashboard Ejecutivo BI")
                .descripcion("Proyecto duplicado")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 10, 1))
                .build();

        when(proyectoRepository.existsByNombreIgnoreCase(request.getNombre())).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.crearProyecto(request)
        );

        assertEquals(
                "Ya existe un proyecto registrado con el nombre: Dashboard Ejecutivo BI",
                exception.getMessage()
        );

        verify(proyectoRepository).existsByNombreIgnoreCase("Dashboard Ejecutivo BI");
        verify(proyectoRepository, never()).save(any(Proyecto.class));
    }

    @Test
    void cambiarEstado_deberiaRechazarFinalizarProyectoConTareasPendientes() {
        Proyecto proyecto = Proyecto.builder()
                .id(10L)
                .nombre("Migración Cloud")
                .descripcion("Migración de infraestructura a la nube")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFinEstimada(LocalDate.of(2026, 12, 31))
                .build();

        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(tareaServiceFacade.existenTareasPendientes(10L)).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.cambiarEstado(10L, EstadoProyecto.COMPLETED)
        );

        assertEquals(
                "No se puede finalizar el proyecto porque existen tareas pendientes",
                exception.getMessage()
        );

        assertEquals(EstadoProyecto.IN_PROGRESS, proyecto.getEstado());

        verify(proyectoRepository).findById(10L);
        verify(tareaServiceFacade).existenTareasPendientes(10L);
        verify(proyectoRepository, never()).save(any(Proyecto.class));
    }
}