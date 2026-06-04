package com.innovatech.tareas_service.service;

import com.innovatech.tareas_service.dto.TareaDTO;
import com.innovatech.tareas_service.entity.EstadoTarea;
import com.innovatech.tareas_service.entity.PrioridadTarea;
import com.innovatech.tareas_service.entity.Tarea;
import com.innovatech.tareas_service.exception.TareaNotFoundException;
import com.innovatech.tareas_service.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private TareaService tareaService;

    private Tarea tarea;
    private TareaDTO tareaDTO;

    @BeforeEach
    void setUp() {
        tarea = Tarea.builder()
                .id(1L)
                .titulo("Test Tarea")
                .descripcion("Descripción de prueba")
                .estado(EstadoTarea.PENDIENTE)
                .prioridad(PrioridadTarea.ALTA)
                .proyectoId(1L)
                .equipoId(1L)
                .asignadoA(1L)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        tareaDTO = TareaDTO.builder()
                .id(1L)
                .titulo("Test Tarea")
                .descripcion("Descripción de prueba")
                .estado(EstadoTarea.PENDIENTE)
                .prioridad(PrioridadTarea.ALTA)
                .proyectoId(1L)
                .equipoId(1L)
                .asignadoA(1L)
                .build();
    }

    @Test
    void testCrearTarea() {
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaDTO resultado = tareaService.crearTarea(tareaDTO);

        assertNotNull(resultado);
        assertEquals("Test Tarea", resultado.getTitulo());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testObtenerTarea() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        TareaDTO resultado = tareaService.obtenerTarea(1L);

        assertNotNull(resultado);
        assertEquals("Test Tarea", resultado.getTitulo());
        verify(tareaRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerTarea_NoEncontrada() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TareaNotFoundException.class, () -> tareaService.obtenerTarea(1L));
    }

    @Test
    void testActualizarEstado() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaDTO resultado = tareaService.actualizarEstado(1L, EstadoTarea.EN_PROGRESO);

        assertNotNull(resultado);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testAsignarTarea() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaDTO resultado = tareaService.asignarTarea(1L, 2L);

        assertNotNull(resultado);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testEliminarTarea() {
        when(tareaRepository.existsById(1L)).thenReturn(true);

        tareaService.eliminarTarea(1L);

        verify(tareaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarTarea_NoEncontrada() {
        when(tareaRepository.existsById(1L)).thenReturn(false);

        assertThrows(TareaNotFoundException.class, () -> tareaService.eliminarTarea(1L));
    }
}
