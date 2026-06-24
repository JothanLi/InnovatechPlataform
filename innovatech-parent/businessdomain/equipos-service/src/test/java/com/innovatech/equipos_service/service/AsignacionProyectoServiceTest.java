package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.AsignacionProyectoRequest;
import com.innovatech.equipos_service.dto.AsignacionProyectoResponse;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.exception.RecursoNoEncontradoException;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.AsignacionProyecto;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.model.RolEquipo;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
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
class AsignacionProyectoServiceTest {

    @Mock
    private AsignacionProyectoRepository asignacionProyectoRepository;

    @Mock
    private MiembroEquipoService miembroEquipoService;

    @Mock
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @InjectMocks
    private AsignacionProyectoService asignacionProyectoService;

    @Test
    void asignarMiembroAProyecto_deberiaCrearAsignacionCuandoDatosSonValidos() {
        AsignacionProyectoRequest request = new AsignacionProyectoRequest(1L, 10L);

        MiembroEquipo miembro = MiembroEquipo.builder()
                .id(10L)
                .nombres("Sebastian")
                .apellidoPaterno("Mariqueo")
                .apellidoMaterno("Perez")
                .email("sebastian.mariqueo@innovatech.cl")
                .passwordHash("hash")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        ProyectoAdaptadoResponse proyecto = new ProyectoAdaptadoResponse(
                1L,
                "Plataforma Innovatech",
                "IN_PROGRESS"
        );

        AsignacionProyecto asignacionGuardada = AsignacionProyecto.builder()
                .id(100L)
                .idProyecto(1L)
                .miembro(miembro)
                .fechaAsignacion(LocalDate.now())
                .build();

        when(miembroEquipoService.buscarEntidadPorId(10L)).thenReturn(miembro);
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(1L)).thenReturn(proyecto);
        when(asignacionProyectoRepository.existsByIdProyectoAndMiembroId(1L, 10L)).thenReturn(false);
        when(asignacionProyectoRepository.save(any(AsignacionProyecto.class))).thenReturn(asignacionGuardada);

        AsignacionProyectoResponse response = asignacionProyectoService.asignarMiembroAProyecto(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(1L, response.idProyecto());
        assertEquals("Plataforma Innovatech", response.nombreProyecto());
        assertEquals(10L, response.idMiembro());
        assertEquals("Sebastian Mariqueo Perez", response.nombreMiembro());
        assertEquals("DEVELOPER", response.rolMiembro());

        verify(miembroEquipoService).buscarEntidadPorId(10L);
        verify(proyectoServiceAdapter).obtenerProyectoAdaptado(1L);
        verify(asignacionProyectoRepository).existsByIdProyectoAndMiembroId(1L, 10L);
        verify(asignacionProyectoRepository).save(any(AsignacionProyecto.class));
    }

    @Test
    void asignarMiembroAProyecto_deberiaLanzarExcepcionCuandoMiembroEstaInactivo() {
        AsignacionProyectoRequest request = new AsignacionProyectoRequest(1L, 10L);

        MiembroEquipo miembroInactivo = MiembroEquipo.builder()
                .id(10L)
                .nombres("Sebastian")
                .apellidoPaterno("Mariqueo")
                .apellidoMaterno("Perez")
                .email("sebastian.mariqueo@innovatech.cl")
                .passwordHash("hash")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.INACTIVO)
                .build();

        when(miembroEquipoService.buscarEntidadPorId(10L)).thenReturn(miembroInactivo);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> asignacionProyectoService.asignarMiembroAProyecto(request)
        );

        assertEquals("No se puede asignar un miembro inactivo a un proyecto", exception.getMessage());

        verify(miembroEquipoService).buscarEntidadPorId(10L);
        verify(proyectoServiceAdapter, never()).obtenerProyectoAdaptado(anyLong());
        verify(asignacionProyectoRepository, never()).save(any(AsignacionProyecto.class));
    }

    @Test
    void asignarMiembroAProyecto_deberiaLanzarExcepcionCuandoMiembroYaEstaAsignado() {
        AsignacionProyectoRequest request = new AsignacionProyectoRequest(1L, 10L);

        MiembroEquipo miembro = MiembroEquipo.builder()
                .id(10L)
                .nombres("Camila")
                .apellidoPaterno("Torres")
                .apellidoMaterno("Rojas")
                .email("camila.torres@innovatech.cl")
                .passwordHash("hash")
                .rol(RolEquipo.PROJECT_MANAGER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        ProyectoAdaptadoResponse proyecto = new ProyectoAdaptadoResponse(
                1L,
                "Plataforma Innovatech",
                "IN_PROGRESS"
        );

        when(miembroEquipoService.buscarEntidadPorId(10L)).thenReturn(miembro);
        when(proyectoServiceAdapter.obtenerProyectoAdaptado(1L)).thenReturn(proyecto);
        when(asignacionProyectoRepository.existsByIdProyectoAndMiembroId(1L, 10L)).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> asignacionProyectoService.asignarMiembroAProyecto(request)
        );

        assertEquals("El miembro ya se encuentra asignado a este proyecto", exception.getMessage());

        verify(asignacionProyectoRepository, never()).save(any(AsignacionProyecto.class));
    }

    @Test
    void listarAsignacionesPorProyecto_deberiaRetornarAsignacionesDelProyecto() {
        Long idProyecto = 1L;

        MiembroEquipo miembro = MiembroEquipo.builder()
                .id(10L)
                .nombres("Jorge")
                .apellidoPaterno("Salazar")
                .apellidoMaterno("Parra")
                .email("jorge.salazar@innovatech.cl")
                .passwordHash("hash")
                .rol(RolEquipo.QA)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        ProyectoAdaptadoResponse proyecto = new ProyectoAdaptadoResponse(
                1L,
                "Plataforma Innovatech",
                "IN_PROGRESS"
        );

        AsignacionProyecto asignacion = AsignacionProyecto.builder()
                .id(100L)
                .idProyecto(idProyecto)
                .miembro(miembro)
                .fechaAsignacion(LocalDate.of(2026, 6, 4))
                .build();

        when(proyectoServiceAdapter.obtenerProyectoAdaptado(idProyecto)).thenReturn(proyecto);
        when(asignacionProyectoRepository.findByIdProyecto(idProyecto)).thenReturn(List.of(asignacion));

        List<AsignacionProyectoResponse> response = asignacionProyectoService.listarAsignacionesPorProyecto(idProyecto);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).id());
        assertEquals("Plataforma Innovatech", response.get(0).nombreProyecto());
        assertEquals("Jorge Salazar Parra", response.get(0).nombreMiembro());
        assertEquals("QA", response.get(0).rolMiembro());

        verify(proyectoServiceAdapter).obtenerProyectoAdaptado(idProyecto);
        verify(asignacionProyectoRepository).findByIdProyecto(idProyecto);
    }

    @Test
    void eliminarAsignacion_deberiaEliminarCuandoExiste() {
        AsignacionProyecto asignacion = AsignacionProyecto.builder()
                .id(100L)
                .idProyecto(1L)
                .fechaAsignacion(LocalDate.now())
                .build();

        when(asignacionProyectoRepository.findById(100L)).thenReturn(Optional.of(asignacion));

        asignacionProyectoService.eliminarAsignacion(100L);

        verify(asignacionProyectoRepository).findById(100L);
        verify(asignacionProyectoRepository).delete(asignacion);
    }

    @Test
    void eliminarAsignacion_deberiaLanzarExcepcionCuandoNoExiste() {
        when(asignacionProyectoRepository.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> asignacionProyectoService.eliminarAsignacion(999L)
        );

        assertEquals("No existe la asignación con ID: 999", exception.getMessage());

        verify(asignacionProyectoRepository).findById(999L);
        verify(asignacionProyectoRepository, never()).delete(any(AsignacionProyecto.class));
    }
}
