package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.exception.RecursoNoEncontradoException;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.model.RolEquipo;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiembroEquipoServiceTest {

    @Mock
    private MiembroEquipoRepository miembroEquipoRepository;

    @InjectMocks
    private MiembroEquipoService miembroEquipoService;

    @Test
    void crearMiembro_deberiaCrearMiembroCuandoEmailNoExiste() {
        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo",
                "sebastian.mariqueo@innovatech.cl",
                RolEquipo.DEVELOPER
        );

        MiembroEquipo miembroGuardado = MiembroEquipo.builder()
                .id(1L)
                .nombre("Sebastian")
                .apellido("Mariqueo")
                .email("sebastian.mariqueo@innovatech.cl")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroEquipoRepository.existsByEmail(request.email())).thenReturn(false);
        when(miembroEquipoRepository.save(any(MiembroEquipo.class))).thenReturn(miembroGuardado);

        MiembroEquipoResponse response = miembroEquipoService.crearMiembro(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Sebastian", response.nombre());
        assertEquals("Mariqueo", response.apellido());
        assertEquals("sebastian.mariqueo@innovatech.cl", response.email());
        assertEquals(RolEquipo.DEVELOPER, response.rol());
        assertEquals(EstadoMiembro.ACTIVO, response.estado());

        verify(miembroEquipoRepository).existsByEmail(request.email());
        verify(miembroEquipoRepository).save(any(MiembroEquipo.class));
    }

    @Test
    void crearMiembro_deberiaLanzarExcepcionCuandoEmailYaExiste() {
        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo",
                "sebastian.mariqueo@innovatech.cl",
                RolEquipo.DEVELOPER
        );

        when(miembroEquipoRepository.existsByEmail(request.email())).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> miembroEquipoService.crearMiembro(request)
        );

        assertEquals("Ya existe un miembro con el email: sebastian.mariqueo@innovatech.cl", exception.getMessage());

        verify(miembroEquipoRepository).existsByEmail(request.email());
        verify(miembroEquipoRepository, never()).save(any(MiembroEquipo.class));
    }

    @Test
    void obtenerMiembroPorId_deberiaRetornarMiembroCuandoExiste() {
        MiembroEquipo miembro = MiembroEquipo.builder()
                .id(1L)
                .nombre("Camila")
                .apellido("Torres")
                .email("camila.torres@innovatech.cl")
                .rol(RolEquipo.PROJECT_MANAGER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroEquipoRepository.findById(1L)).thenReturn(Optional.of(miembro));

        MiembroEquipoResponse response = miembroEquipoService.obtenerMiembroPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Camila", response.nombre());
        assertEquals(RolEquipo.PROJECT_MANAGER, response.rol());

        verify(miembroEquipoRepository).findById(1L);
    }

    @Test
    void obtenerMiembroPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        when(miembroEquipoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> miembroEquipoService.obtenerMiembroPorId(99L)
        );

        assertEquals("No existe el miembro con ID: 99", exception.getMessage());

        verify(miembroEquipoRepository).findById(99L);
    }

    @Test
    void actualizarMiembro_deberiaActualizarCuandoEmailPerteneceAlMismoMiembro() {
        MiembroEquipo miembroExistente = MiembroEquipo.builder()
                .id(1L)
                .nombre("Sebastian")
                .apellido("Mariqueo")
                .email("sebastian.mariqueo@innovatech.cl")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo Perez",
                "sebastian.mariqueo@innovatech.cl",
                RolEquipo.DEVOPS
        );

        MiembroEquipo miembroActualizado = MiembroEquipo.builder()
                .id(1L)
                .nombre("Sebastian")
                .apellido("Mariqueo Perez")
                .email("sebastian.mariqueo@innovatech.cl")
                .rol(RolEquipo.DEVOPS)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroEquipoRepository.findById(1L)).thenReturn(Optional.of(miembroExistente));
        when(miembroEquipoRepository.findByEmail(request.email())).thenReturn(Optional.of(miembroExistente));
        when(miembroEquipoRepository.save(any(MiembroEquipo.class))).thenReturn(miembroActualizado);

        MiembroEquipoResponse response = miembroEquipoService.actualizarMiembro(1L, request);

        assertEquals("Mariqueo Perez", response.apellido());
        assertEquals(RolEquipo.DEVOPS, response.rol());

        verify(miembroEquipoRepository).save(any(MiembroEquipo.class));
    }

    @Test
    void actualizarMiembro_deberiaLanzarExcepcionCuandoEmailPerteneceAOtroMiembro() {
        MiembroEquipo miembroActual = MiembroEquipo.builder()
                .id(1L)
                .nombre("Sebastian")
                .apellido("Mariqueo")
                .email("sebastian.mariqueo@innovatech.cl")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        MiembroEquipo otroMiembro = MiembroEquipo.builder()
                .id(2L)
                .nombre("Camila")
                .apellido("Torres")
                .email("camila.torres@innovatech.cl")
                .rol(RolEquipo.QA)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo",
                "camila.torres@innovatech.cl",
                RolEquipo.DEVELOPER
        );

        when(miembroEquipoRepository.findById(1L)).thenReturn(Optional.of(miembroActual));
        when(miembroEquipoRepository.findByEmail(request.email())).thenReturn(Optional.of(otroMiembro));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> miembroEquipoService.actualizarMiembro(1L, request)
        );

        assertEquals("Ya existe otro miembro con el email: camila.torres@innovatech.cl", exception.getMessage());

        verify(miembroEquipoRepository, never()).save(any(MiembroEquipo.class));
    }

    @Test
    void desactivarMiembro_deberiaCambiarEstadoAInactivo() {
        MiembroEquipo miembro = MiembroEquipo.builder()
                .id(1L)
                .nombre("Jorge")
                .apellido("Salazar")
                .email("jorge.salazar@innovatech.cl")
                .rol(RolEquipo.QA)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroEquipoRepository.findById(1L)).thenReturn(Optional.of(miembro));
        when(miembroEquipoRepository.save(any(MiembroEquipo.class))).thenReturn(miembro);

        miembroEquipoService.desactivarMiembro(1L);

        assertEquals(EstadoMiembro.INACTIVO, miembro.getEstado());

        verify(miembroEquipoRepository).findById(1L);
        verify(miembroEquipoRepository).save(miembro);
    }
}