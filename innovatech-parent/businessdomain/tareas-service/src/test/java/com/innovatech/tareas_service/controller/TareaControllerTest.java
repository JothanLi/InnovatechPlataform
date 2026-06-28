package com.innovatech.tareas_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatech.tareas_service.dto.CambioEstadoTareaDTO;
import com.innovatech.tareas_service.dto.TareaRequestDTO;
import com.innovatech.tareas_service.dto.TareaResponseDTO;
import com.innovatech.tareas_service.exception.GlobalExceptionHandler;
import com.innovatech.tareas_service.exception.RecursoNoEncontradoException;
import com.innovatech.tareas_service.exception.ReglaNegocioException;
import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.service.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TareaControllerTest {

    private MockMvc mockMvc;

    private TareaService tareaService;

    private ObjectMapper objectMapper;

    private TareaRequestDTO request;
    private TareaResponseDTO response;

    @BeforeEach
    void setUp() {
        tareaService = mock(TareaService.class);
        TareaController tareaController = new TareaController(tareaService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(tareaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        request = TareaRequestDTO.builder()
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        response = TareaResponseDTO.builder()
                .id(1L)
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.PENDING)
                .idProyecto(10L)
                .nombreProyecto("Plataforma Innovatech")
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();
    }

    @Test
    void crearTarea_deberiaRetornarCreated() throws Exception {
        when(tareaService.crearTarea(any(TareaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Implementar CRUD de tareas"))
                .andExpect(jsonPath("$.estado").value("PENDING"))
                .andExpect(jsonPath("$.idProyecto").value(10L))
                .andExpect(jsonPath("$.nombreProyecto").value("Plataforma Innovatech"))
                .andExpect(jsonPath("$.responsable").value("Sebastian"));

        verify(tareaService, times(1)).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaRetornarBadRequestCuandoDescripcionEstaVacia() throws Exception {
        request.setDescripcion("");

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.validaciones.descripcion").value("La descripción de la tarea es obligatoria"));

        verify(tareaService, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaRetornarBadRequestCuandoResponsableEstaVacio() throws Exception {
        request.setResponsable("");

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.validaciones.responsable").value("El responsable de la tarea es obligatorio"));

        verify(tareaService, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaRetornarBadRequestCuandoIdProyectoEsNull() throws Exception {
        request.setIdProyecto(null);

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.validaciones.idProyecto").value("El ID del proyecto es obligatorio"));

        verify(tareaService, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void listarTareas_deberiaRetornarOkConLista() throws Exception {
        when(tareaService.listarTareas()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/tareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].descripcion").value("Implementar CRUD de tareas"));

        verify(tareaService, times(1)).listarTareas();
    }

    @Test
    void buscarPorId_deberiaRetornarOkCuandoExiste() throws Exception {
        when(tareaService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tareas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Implementar CRUD de tareas"));

        verify(tareaService, times(1)).buscarPorId(1L);
    }

    @Test
    void buscarPorId_deberiaRetornarNotFoundCuandoNoExiste() throws Exception {
        when(tareaService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe una tarea con ID: 99"));

        mockMvc.perform(get("/api/v1/tareas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.mensaje").value("No existe una tarea con ID: 99"));

        verify(tareaService, times(1)).buscarPorId(99L);
    }

    @Test
    void buscarPorProyecto_deberiaRetornarOk() throws Exception {
        when(tareaService.buscarPorProyecto(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/tareas/proyecto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idProyecto").value(10L));

        verify(tareaService, times(1)).buscarPorProyecto(10L);
    }

    @Test
    void buscarPorEstado_deberiaRetornarOk() throws Exception {
        when(tareaService.buscarPorEstado(EstadoTarea.PENDING)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/tareas/estado/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("PENDING"));

        verify(tareaService, times(1)).buscarPorEstado(EstadoTarea.PENDING);
    }

    @Test
    void existenTareasPendientes_deberiaRetornarTrue() throws Exception {
        when(tareaService.existenTareasPendientesPorProyecto(10L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/tareas/proyecto/10/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existenPendientes").value(true));

        verify(tareaService, times(1)).existenTareasPendientesPorProyecto(10L);
    }

    @Test
    void actualizarTarea_deberiaRetornarOk() throws Exception {
        TareaResponseDTO responseActualizado = TareaResponseDTO.builder()
                .id(1L)
                .descripcion("Tarea actualizada")
                .estado(EstadoTarea.IN_PROGRESS)
                .idProyecto(10L)
                .nombreProyecto("Plataforma Innovatech")
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 12))
                .build();

        request.setDescripcion("Tarea actualizada");
        request.setEstado(EstadoTarea.IN_PROGRESS);
        request.setFechaFinEstimada(LocalDate.of(2026, 6, 12));

        when(tareaService.actualizarTarea(eq(1L), any(TareaRequestDTO.class))).thenReturn(responseActualizado);

        mockMvc.perform(put("/api/v1/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Tarea actualizada"))
                .andExpect(jsonPath("$.estado").value("IN_PROGRESS"));

        verify(tareaService, times(1)).actualizarTarea(eq(1L), any(TareaRequestDTO.class));
    }

    @Test
    void cambiarEstado_deberiaRetornarOk() throws Exception {
        CambioEstadoTareaDTO cambioEstado = CambioEstadoTareaDTO.builder()
                .estado(EstadoTarea.IN_PROGRESS)
                .build();

        TareaResponseDTO responseActualizado = TareaResponseDTO.builder()
                .id(1L)
                .descripcion("Implementar CRUD de tareas")
                .estado(EstadoTarea.IN_PROGRESS)
                .idProyecto(10L)
                .nombreProyecto("Plataforma Innovatech")
                .responsable("Sebastian")
                .fechaInicio(LocalDate.of(2026, 6, 5))
                .fechaFinEstimada(LocalDate.of(2026, 6, 10))
                .build();

        when(tareaService.cambiarEstado(1L, EstadoTarea.IN_PROGRESS)).thenReturn(responseActualizado);

        mockMvc.perform(patch("/api/v1/tareas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioEstado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("IN_PROGRESS"));

        verify(tareaService, times(1)).cambiarEstado(1L, EstadoTarea.IN_PROGRESS);
    }

    @Test
    void cambiarEstado_deberiaRetornarBadRequestCuandoReglaNegocioFalla() throws Exception {
        CambioEstadoTareaDTO cambioEstado = CambioEstadoTareaDTO.builder()
                .estado(EstadoTarea.DONE)
                .build();

        when(tareaService.cambiarEstado(1L, EstadoTarea.DONE))
                .thenThrow(new ReglaNegocioException("Una tarea pendiente debe pasar primero a IN_PROGRESS antes de finalizarse"));

        mockMvc.perform(patch("/api/v1/tareas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioEstado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de negocio inválida"))
                .andExpect(jsonPath("$.mensaje").value("Una tarea pendiente debe pasar primero a IN_PROGRESS antes de finalizarse"));

        verify(tareaService, times(1)).cambiarEstado(1L, EstadoTarea.DONE);
    }

    @Test
    void eliminarTarea_deberiaRetornarNoContent() throws Exception {
        doNothing().when(tareaService).eliminarTarea(1L);

        mockMvc.perform(delete("/api/v1/tareas/1"))
                .andExpect(status().isNoContent());

        verify(tareaService, times(1)).eliminarTarea(1L);
    }
}