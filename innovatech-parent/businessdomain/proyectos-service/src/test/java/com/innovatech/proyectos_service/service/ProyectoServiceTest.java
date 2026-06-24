package com.innovatech.proyectos_service.service;

import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.RecursoNoEncontradoException;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.facade.TareaServiceFacade;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import com.innovatech.proyectos_service.repository.ProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TareaServiceFacade tareaServiceFacade;

    @InjectMocks
    private ProyectoService proyectoService;

    @Test
    void crearProyecto_deberiaCrearProyectoCorrectamente() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Sistema SIGP Innovatech")
                .descripcion("Plataforma de gestión de proyectos tecnológicos")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 4))
                .fechaFinEstimada(LocalDate.of(2026, 12, 4))
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
        assertEquals(EstadoProyecto.PLANNED, response.getEstado());

        verify(proyectoRepository).existsByNombreIgnoreCase(request.getNombre());
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    @Test
    void crearProyecto_deberiaLanzarExcepcionCuandoNombreExiste() {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Sistema SIGP Innovatech")
                .descripcion("Proyecto duplicado")
                .estado(EstadoProyecto.PLANNED)
                .build();

        when(proyectoRepository.existsByNombreIgnoreCase(request.getNombre())).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.crearProyecto(request)
        );

        assertEquals(
                "Ya existe un proyecto registrado con el nombre: Sistema SIGP Innovatech",
                exception.getMessage()
        );

        verify(proyectoRepository).existsByNombreIgnoreCase(request.getNombre());
        verify(proyectoRepository, never()).save(any(Proyecto.class));
    }

    @Test
    void listarProyectos_deberiaRetornarListaDeProyectos() {
        Proyecto proyecto1 = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto 1")
                .descripcion("Descripción 1")
                .estado(EstadoProyecto.PLANNED)
                .build();

        Proyecto proyecto2 = Proyecto.builder()
                .id(2L)
                .nombre("Proyecto 2")
                .descripcion("Descripción 2")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoRepository.findAll()).thenReturn(List.of(proyecto1, proyecto2));

        List<ProyectoResponseDTO> response = proyectoService.listarProyectos();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Proyecto 1", response.get(0).getNombre());
        assertEquals("Proyecto 2", response.get(1).getNombre());

        verify(proyectoRepository).findAll();
    }

    @Test
    void buscarPorId_deberiaRetornarProyectoCuandoExiste() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto Innovatech")
                .descripcion("Descripción del proyecto")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        ProyectoResponseDTO response = proyectoService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Proyecto Innovatech", response.getNombre());
        assertEquals(EstadoProyecto.IN_PROGRESS, response.getEstado());

        verify(proyectoRepository).findById(1L);
    }

    @Test
    void buscarPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> proyectoService.buscarPorId(99L)
        );

        assertEquals("No existe un proyecto con ID: 99", exception.getMessage());

        verify(proyectoRepository).findById(99L);
    }

    @Test
    void buscarPorEstado_deberiaRetornarProyectosFiltrados() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto Planeado")
                .descripcion("Descripción")
                .estado(EstadoProyecto.PLANNED)
                .build();

        when(proyectoRepository.findByEstado(EstadoProyecto.PLANNED)).thenReturn(List.of(proyecto));

        List<ProyectoResponseDTO> response = proyectoService.buscarPorEstado(EstadoProyecto.PLANNED);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(EstadoProyecto.PLANNED, response.get(0).getEstado());

        verify(proyectoRepository).findByEstado(EstadoProyecto.PLANNED);
    }

    @Test
    void actualizarProyecto_deberiaActualizarProyectoCorrectamente() {
        Proyecto proyectoExistente = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto Antiguo")
                .descripcion("Descripción antigua")
                .estado(EstadoProyecto.PLANNED)
                .build();

        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Proyecto Actualizado")
                .descripcion("Descripción actualizada")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 6, 4))
                .fechaFinEstimada(LocalDate.of(2026, 12, 4))
                .build();

        Proyecto proyectoActualizado = Proyecto.builder()
                .id(1L)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoExistente));
        when(proyectoRepository.existsByNombreIgnoreCase(request.getNombre())).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoActualizado);

        ProyectoResponseDTO response = proyectoService.actualizarProyecto(1L, request);

        assertNotNull(response);
        assertEquals("Proyecto Actualizado", response.getNombre());
        assertEquals("Descripción actualizada", response.getDescripcion());
        assertEquals(EstadoProyecto.IN_PROGRESS, response.getEstado());

        verify(proyectoRepository).findById(1L);
        verify(proyectoRepository).existsByNombreIgnoreCase(request.getNombre());
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    @Test
    void cambiarEstado_deberiaCambiarEstadoCorrectamente() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto")
                .descripcion("Descripción")
                .estado(EstadoProyecto.PLANNED)
                .build();

        Proyecto proyectoActualizado = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto")
                .descripcion("Descripción")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoActualizado);

        ProyectoResponseDTO response = proyectoService.cambiarEstado(1L, EstadoProyecto.IN_PROGRESS);

        assertNotNull(response);
        assertEquals(EstadoProyecto.IN_PROGRESS, response.getEstado());

        verify(proyectoRepository).findById(1L);
        verify(tareaServiceFacade, never()).existenTareasPendientes(1L);
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiExistenTareasPendientes() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto")
                .descripcion("Descripción")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(tareaServiceFacade.existenTareasPendientes(1L)).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.cambiarEstado(1L, EstadoProyecto.COMPLETED)
        );

        assertEquals("No se puede finalizar el proyecto porque existen tareas pendientes", exception.getMessage());

        verify(proyectoRepository).findById(1L);
        verify(tareaServiceFacade).existenTareasPendientes(1L);
        verify(proyectoRepository, never()).save(any(Proyecto.class));
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiProyectoEstaCancelado() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto Cancelado")
                .descripcion("Descripción")
                .estado(EstadoProyecto.CANCELLED)
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> proyectoService.cambiarEstado(1L, EstadoProyecto.IN_PROGRESS)
        );

        assertEquals("No se puede cambiar el estado de un proyecto cancelado", exception.getMessage());

        verify(proyectoRepository).findById(1L);
        verify(proyectoRepository, never()).save(any(Proyecto.class));
    }

    @Test
    void eliminarProyecto_deberiaEliminarProyectoCuandoExiste() {
        Proyecto proyecto = Proyecto.builder()
                .id(1L)
                .nombre("Proyecto")
                .descripcion("Descripción")
                .estado(EstadoProyecto.PLANNED)
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        proyectoService.eliminarProyecto(1L);

        verify(proyectoRepository).findById(1L);
        verify(proyectoRepository).delete(proyecto);
    }
}
