package com.innovatech.equipos_service.service;

import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.dto.AsignacionProyectoRequest;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.EstadoMiembro;
import com.innovatech.equipos_service.model.MiembroEquipo;
import com.innovatech.equipos_service.model.RolEquipo;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquiposServiceCriticoUnitTest {

    @Mock
    private MiembroEquipoRepository miembroEquipoRepository;

    @Mock
    private AsignacionProyectoRepository asignacionProyectoRepository;

    @Mock
    private MiembroEquipoService miembroEquipoServiceMock;

    @Mock
    private ProyectoServiceAdapter proyectoServiceAdapter;

    private MiembroEquipoService miembroEquipoService;
    private AsignacionProyectoService asignacionProyectoService;

    @BeforeEach
    void setUp() {
        miembroEquipoService = new MiembroEquipoService(miembroEquipoRepository);

        asignacionProyectoService = new AsignacionProyectoService(
                asignacionProyectoRepository,
                miembroEquipoServiceMock,
                proyectoServiceAdapter
        );
    }

    @Test
    void crearMiembro_deberiaGuardarPasswordEncriptadaYEstadoActivo() {
        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo",
                "Perez",
                "sebastian.mariqueo@innovatech.cl",
                RolEquipo.DEVELOPER,
                "Sebastian123"
        );

        MiembroEquipo miembroGuardado = MiembroEquipo.builder()
                .id(1L)
                .nombres("Sebastian")
                .apellidoPaterno("Mariqueo")
                .apellidoMaterno("Perez")
                .email("sebastian.mariqueo@innovatech.cl")
                .passwordHash("$2a$12$hashsimulado")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroEquipoRepository.existsByEmail(request.email())).thenReturn(false);
        when(miembroEquipoRepository.save(any(MiembroEquipo.class))).thenReturn(miembroGuardado);

        MiembroEquipoResponse response = miembroEquipoService.crearMiembro(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Sebastian", response.nombres());
        assertEquals("sebastian.mariqueo@innovatech.cl", response.email());
        assertEquals(RolEquipo.DEVELOPER, response.rol());
        assertEquals(EstadoMiembro.ACTIVO, response.estado());

        ArgumentCaptor<MiembroEquipo> captor = ArgumentCaptor.forClass(MiembroEquipo.class);
        verify(miembroEquipoRepository).save(captor.capture());

        MiembroEquipo miembroCapturado = captor.getValue();

        assertEquals(EstadoMiembro.ACTIVO, miembroCapturado.getEstado());
        assertNotEquals("Sebastian123", miembroCapturado.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("Sebastian123", miembroCapturado.getPasswordHash()));

        verify(miembroEquipoRepository).existsByEmail("sebastian.mariqueo@innovatech.cl");
        verify(miembroEquipoRepository).save(any(MiembroEquipo.class));
    }

    @Test
    void crearMiembro_deberiaRechazarEmailDuplicado() {
        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Camila",
                "Torres",
                "Rojas",
                "camila.torres@innovatech.cl",
                RolEquipo.QA,
                "Camila123"
        );

        when(miembroEquipoRepository.existsByEmail(request.email())).thenReturn(true);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> miembroEquipoService.crearMiembro(request)
        );

        assertEquals(
                "Ya existe un miembro con el email: camila.torres@innovatech.cl",
                exception.getMessage()
        );

        verify(miembroEquipoRepository).existsByEmail("camila.torres@innovatech.cl");
        verify(miembroEquipoRepository, never()).save(any(MiembroEquipo.class));
    }

    @Test
    void asignarMiembroAProyecto_deberiaRechazarMiembroInactivo() {
        AsignacionProyectoRequest request = new AsignacionProyectoRequest(10L, 5L);

        MiembroEquipo miembroInactivo = MiembroEquipo.builder()
                .id(5L)
                .nombres("Jorge")
                .apellidoPaterno("Salazar")
                .apellidoMaterno("Parra")
                .email("jorge.salazar@innovatech.cl")
                .passwordHash("hash")
                .rol(RolEquipo.DEVELOPER)
                .estado(EstadoMiembro.INACTIVO)
                .build();

        when(miembroEquipoServiceMock.buscarEntidadPorId(5L)).thenReturn(miembroInactivo);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> asignacionProyectoService.asignarMiembroAProyecto(request)
        );

        assertEquals(
                "No se puede asignar un miembro inactivo a un proyecto",
                exception.getMessage()
        );

        verify(miembroEquipoServiceMock).buscarEntidadPorId(5L);
        verify(proyectoServiceAdapter, never()).obtenerProyectoAdaptado(anyLong());
        verify(asignacionProyectoRepository, never()).existsByIdProyectoAndMiembroId(anyLong(), anyLong());
        verify(asignacionProyectoRepository, never()).save(any());
    }
}