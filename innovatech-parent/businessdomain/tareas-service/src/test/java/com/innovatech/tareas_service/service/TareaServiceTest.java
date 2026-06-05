package com.innovatech.tareas_service.service;

import com.innovatech.tareas_service.adapter.ProyectoServiceAdapter;
import com.innovatech.tareas_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.exception.RecursoNoEncontradoException;
import com.innovatech.tareas_service.exception.ReglaNegocioException;
import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.model.Tarea;
import com.innovatech.tareas_service.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
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
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @InjectMocks
    private TareaService tareaService;

    private Tarea tarea;
    private TareaRequestDTO request;
    private ProyectoAdaptadoResponse proyecto;

    @BeforeEach
    void setUp() {
        tarea = Tarea.builder()
                .id(1L)
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        request = TareaRequestDTO.builder()
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        proyecto = new ProyectoAdaptadoResponse(
                10L,
                "Plataforma Innovatech",
                "IN_PROGRESS"
        );
    }

    @Test
    void crearTarea_deberiaCrearTareaCorrectamente() {
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaResponseDTO response = tareaService.crearTarea(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Implementar CRUD de tareas", response.getDescripcion());
        assertEquals(EstadoTarea.PENDING, response.getEstado());
        assertEquals(10L, response.getIdProyecto());
        assertEquals("Plataforma Innovatech", response.getNombreProyecto());
        assertEquals("Sebastian", response.getResponsable());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void crearTarea_deberiaLanzarExcepcionSiFechaFinEsAnteriorAFechaInicio() {
        request.setFechaInicio(LocalDate.of(2026, 6, 10));
        request.setFechaFinEstimada(LocalDate.of(2026, 6, 5));

        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> tareaService.crearTarea(request)
        );

        assertEquals("La fecha fin estimada no puede ser anterior a la fecha de inicio", exception.getMessage());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void listarTareas_deberiaRetornarListaDeTareas() {
        when(tareaRepository.findAll()).thenReturn(List.of(tarea));

        List<TareaResponseDTO> response = tareaService.listarTareas();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Implementar CRUD de tareas", response.get(0).getDescripcion());
        assertEquals(EstadoTarea.PENDING, response.get(0).getEstado());

        verify(tareaRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_deberiaRetornarTareaCuandoExiste() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);

        TareaResponseDTO response = tareaService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Implementar CRUD de tareas", response.getDescripcion());
        assertEquals("Plataforma Innovatech", response.getNombreProyecto());

        verify(tareaRepository, times(1)).findById(1L);
        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
    }

    @Test
    void buscarPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> tareaService.buscarPorId(99L)
        );

        assertEquals("No existe una tarea con ID: 99", exception.getMessage());

        verify(tareaRepository, times(1)).findById(99L);
        verify(proyectoServiceAdapter, never()).obtenerProyectoAdaptado(anyLong());
    }

    @Test
    void buscarPorProyecto_deberiaRetornarTareasDelProyecto() {
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);
        when(tareaRepository.findByIdProyecto(10L)).thenReturn(List.of(tarea));

        List<TareaResponseDTO> response = tareaService.buscarPorProyecto(10L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getIdProyecto());
        assertEquals("Plataforma Innovatech", response.get(0).getNombreProyecto());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
        verify(tareaRepository, times(1)).findByIdProyecto(10L);
    }

    @Test
    void buscarPorEstado_deberiaRetornarTareasPorEstado() {
        when(tareaRepository.findByEstado(EstadoTarea.PENDING)).thenReturn(List.of(tarea));

        List<TareaResponseDTO> response = tareaService.buscarPorEstado(EstadoTarea.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(EstadoTarea.PENDING, response.get(0).getEstado());

        verify(tareaRepository, times(1)).findByEstado(EstadoTarea.PENDING);
    }

    @Test
    void actualizarTarea_deberiaActualizarCorrectamente() {
        TareaRequestDTO requestActualizado = TareaRequestDTO.builder()
                .descripcion("Actualizar módulo de tareas")
                .estado(EstadoTarea.IN_PROGRESS)
                .idProyecto(10L)
                .responsable("Carlos")
                .fechaInicio(LocalDate.of(2026, 6, 6))
                .fechaFinEstimada(LocalDate.of(2026, 6, 12))
                .build();

        Tarea tareaActualizada = Tarea.builder()
                .id(1L)
                .descripcion("Actualizar módulo de tareas")
                .estado(EstadoTarea.IN_PROGRESS)
                .idProyecto(10L)
                .responsable("Carlos")
                .fechaInicio(LocalDate.of(2026, 6, 6))
                .fechaFinEstimada(LocalDate.of(2026, 6, 12))
                .build();

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaActualizada);

        TareaResponseDTO response = tareaService.actualizarTarea(1L, requestActualizado);

        assertNotNull(response);
        assertEquals("Actualizar módulo de tareas", response.getDescripcion());
        assertEquals(EstadoTarea.IN_PROGRESS, response.getEstado());
        assertEquals("Carlos", response.getResponsable());

        verify(tareaRepository, times(1)).findById(1L);
        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void cambiarEstado_deberiaCambiarDePendingAInProgress() {
        Tarea tareaActualizada = Tarea.builder()
                .id(1L)
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.IN_PROGRESS)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaActualizada);
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L)).thenReturn(proyecto);

        TareaResponseDTO response = tareaService.cambiarEstado(1L, EstadoTarea.IN_PROGRESS);

        assertNotNull(response);
        assertEquals(EstadoTarea.IN_PROGRESS, response.getEstado());

        verify(tareaRepository, times(1)).findById(1L);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiPasaDePendingADone() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> tareaService.cambiarEstado(1L, EstadoTarea.DONE)
        );

        assertEquals("Una tarea pendiente debe pasar primero a IN_PROGRESS antes de finalizarse", exception.getMessage());

        verify(tareaRepository, times(1)).findById(1L);
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiTareaFinalizadaCambiaEstado() {
        tarea.setEstado(EstadoTarea.DONE);

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> tareaService.cambiarEstado(1L, EstadoTarea.IN_PROGRESS)
        );

        assertEquals("No se puede modificar el estado de una tarea finalizada", exception.getMessage());

        verify(tareaRepository, times(1)).findById(1L);
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void eliminarTarea_deberiaEliminarCuandoExiste() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        tareaService.eliminarTarea(1L);

        verify(tareaRepository, times(1)).findById(1L);
        verify(tareaRepository, times(1)).delete(tarea);
    }

    @Test
    void eliminarTarea_deberiaLanzarExcepcionCuandoNoExiste() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> tareaService.eliminarTarea(99L)
        );

        assertEquals("No existe una tarea con ID: 99", exception.getMessage());

        verify(tareaRepository, times(1)).findById(99L);
        verify(tareaRepository, never()).delete(any(Tarea.class));
    }

    @Test
    void existenTareasPendientesPorProyecto_deberiaRetornarTrueSiExistenPendientes() {
        when(tareaRepository.existsByIdProyectoAndEstadoNot(10L, EstadoTarea.DONE)).thenReturn(true);

        boolean resultado = tareaService.existenTareasPendientesPorProyecto(10L);

        assertTrue(resultado);

        verify(tareaRepository, times(1)).existsByIdProyectoAndEstadoNot(10L, EstadoTarea.DONE);
    }
}