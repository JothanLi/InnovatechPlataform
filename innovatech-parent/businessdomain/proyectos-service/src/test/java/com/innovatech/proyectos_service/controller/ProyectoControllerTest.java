package com.innovatech.proyectos_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatech.proyectos_service.dto.CambioEstadoProyectoDTO;
import com.innovatech.proyectos_service.dto.ProyectoRequestDTO;
import com.innovatech.proyectos_service.dto.ProyectoResponseDTO;
import com.innovatech.proyectos_service.exception.RecursoNoEncontradoException;
import com.innovatech.proyectos_service.exception.ReglaNegocioException;
import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.service.ProyectoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProyectoController.class)
class ProyectoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProyectoService proyectoService;

    @Test
    void crearProyecto_deberiaRetornarStatus201() throws Exception {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Sistema SIGP Innovatech")
                .descripcion("Plataforma inteligente de gestión de proyectos")
                .estado(EstadoProyecto.PLANNED)
                .fechaInicio(LocalDate.of(2026, 6, 4))
                .fechaFinEstimada(LocalDate.of(2026, 12, 4))
                .build();

        ProyectoResponseDTO response = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        when(proyectoService.crearProyecto(any(ProyectoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Sistema SIGP Innovatech"))
                .andExpect(jsonPath("$.estado").value("PLANNED"));
    }

    @Test
    void crearProyecto_deberiaRetornar400CuandoDatosInvalidos() throws Exception {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("")
                .descripcion("")
                .estado(null)
                .build();

        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.validaciones.nombre").exists())
                .andExpect(jsonPath("$.validaciones.descripcion").exists())
                .andExpect(jsonPath("$.validaciones.estado").exists());
    }

    @Test
    void listarProyectos_deberiaRetornarStatus200YLista() throws Exception {
        ProyectoResponseDTO proyecto1 = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Proyecto 1")
                .descripcion("Descripción 1")
                .estado(EstadoProyecto.PLANNED)
                .build();

        ProyectoResponseDTO proyecto2 = ProyectoResponseDTO.builder()
                .id(2L)
                .nombre("Proyecto 2")
                .descripcion("Descripción 2")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoService.listarProyectos()).thenReturn(List.of(proyecto1, proyecto2));

        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Proyecto 1"))
                .andExpect(jsonPath("$[1].nombre").value("Proyecto 2"));
    }

    @Test
    void buscarPorId_deberiaRetornarProyectoCuandoExiste() throws Exception {
        ProyectoResponseDTO response = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Proyecto Innovatech")
                .descripcion("Descripción")
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/proyectos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Proyecto Innovatech"))
                .andExpect(jsonPath("$.estado").value("IN_PROGRESS"));
    }

    @Test
    void buscarPorId_deberiaRetornar404CuandoNoExiste() throws Exception {
        when(proyectoService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe un proyecto con ID: 99"));

        mockMvc.perform(get("/api/proyectos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.mensaje").value("No existe un proyecto con ID: 99"));
    }

    @Test
    void buscarPorEstado_deberiaRetornarListaFiltrada() throws Exception {
        ProyectoResponseDTO response = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Proyecto Planeado")
                .descripcion("Descripción")
                .estado(EstadoProyecto.PLANNED)
                .build();

        when(proyectoService.buscarPorEstado(EstadoProyecto.PLANNED)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/proyectos/estado/PLANNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("PLANNED"));
    }

    @Test
    void actualizarProyecto_deberiaRetornarProyectoActualizado() throws Exception {
        ProyectoRequestDTO request = ProyectoRequestDTO.builder()
                .nombre("Proyecto Actualizado")
                .descripcion("Descripción actualizada")
                .estado(EstadoProyecto.IN_PROGRESS)
                .fechaInicio(LocalDate.of(2026, 6, 4))
                .fechaFinEstimada(LocalDate.of(2026, 12, 4))
                .build();

        ProyectoResponseDTO response = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .fechaInicio(request.getFechaInicio())
                .fechaFinEstimada(request.getFechaFinEstimada())
                .build();

        when(proyectoService.actualizarProyecto(eq(1L), any(ProyectoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/proyectos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Proyecto Actualizado"))
                .andExpect(jsonPath("$.estado").value("IN_PROGRESS"));
    }

    @Test
    void cambiarEstado_deberiaRetornarProyectoConNuevoEstado() throws Exception {
        CambioEstadoProyectoDTO request = CambioEstadoProyectoDTO.builder()
                .estado(EstadoProyecto.COMPLETED)
                .build();

        ProyectoResponseDTO response = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Proyecto")
                .descripcion("Descripción")
                .estado(EstadoProyecto.COMPLETED)
                .build();

        when(proyectoService.cambiarEstado(1L, EstadoProyecto.COMPLETED)).thenReturn(response);

        mockMvc.perform(patch("/api/proyectos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETED"));
    }

    @Test
    void cambiarEstado_deberiaRetornar400CuandoReglaNegocioFalla() throws Exception {
        CambioEstadoProyectoDTO request = CambioEstadoProyectoDTO.builder()
                .estado(EstadoProyecto.IN_PROGRESS)
                .build();

        when(proyectoService.cambiarEstado(1L, EstadoProyecto.IN_PROGRESS))
                .thenThrow(new ReglaNegocioException("No se puede cambiar el estado de un proyecto cancelado"));

        mockMvc.perform(patch("/api/proyectos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de negocio inválida"))
                .andExpect(jsonPath("$.mensaje").value("No se puede cambiar el estado de un proyecto cancelado"));
    }

    @Test
    void eliminarProyecto_deberiaRetornar204() throws Exception {
        doNothing().when(proyectoService).eliminarProyecto(1L);

        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isNoContent());
    }
}