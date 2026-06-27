package facade;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.client.ProyectoClient;
import com.innovatech.bff_service.client.TareaClient;
import com.innovatech.bff_service.dto.AsignacionProyectoRequest;
import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.MiembroEquipoRequest;
import com.innovatech.bff_service.dto.MiembroEquipoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoRequestDTO;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaRequestDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import com.innovatech.bff_service.facade.InnovatechBffFacade;
import com.innovatech.bff_service.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InnovatechBffFacadeTest {

    private ProyectoClient proyectoClient;
    private TareaClient tareaClient;
    private EquipoClient equipoClient;
    private CurrentUserService currentUserService;
    private InnovatechBffFacade facade;

    @BeforeEach
    void setUp() {
        proyectoClient = mock(ProyectoClient.class);
        tareaClient = mock(TareaClient.class);
        equipoClient = mock(EquipoClient.class);
        currentUserService = mock(CurrentUserService.class);

        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserService.CurrentUser(
                        1L,
                        "admin@innovatech.cl",
                        "ADMIN",
                        "Admin Innovatech"
                )
        );

        facade = new InnovatechBffFacade(
                proyectoClient,
                tareaClient,
                equipoClient,
                currentUserService
        );
    }

    @Test
    void obtenerDetalleProyecto_deberiaRetornarProyectoConTareasMiembrosYAvance() {
        ProyectoResponseDTO proyecto = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Plataforma Innovatech")
                .descripcion("Sistema de gestión de proyectos")
                .estado("IN_PROGRESS")
                .build();

        List<TareaResponseDTO> tareas = List.of(
                TareaResponseDTO.builder()
                        .id(1L)
                        .descripcion("Crear microservicio de proyectos")
                        .estado("DONE")
                        .idProyecto(1L)
                        .responsable("Sebastian")
                        .build(),
                TareaResponseDTO.builder()
                        .id(2L)
                        .descripcion("Crear BFF")
                        .estado("PENDING")
                        .idProyecto(1L)
                        .responsable("Sebastian")
                        .build()
        );

        List<AsignacionProyectoResponse> miembros = List.of(
                AsignacionProyectoResponse.builder()
                        .id(1L)
                        .idProyecto(1L)
                        .idMiembro(1L)
                        .nombreMiembro("Sebastian")
                        .rolMiembro("Backend Developer")
                        .build()
        );

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto);
        when(tareaClient.listarTareasPorProyecto(1L)).thenReturn(tareas);
        when(equipoClient.listarMiembrosPorProyecto(1L)).thenReturn(miembros);

        ProyectoDetalleResponse response = facade.obtenerDetalleProyecto(1L);

        assertNotNull(response);
        assertEquals("Plataforma Innovatech", response.getProyecto().getNombre());
        assertEquals(2, response.getTareas().size());
        assertEquals(1, response.getMiembrosAsignados().size());
        assertEquals(50.0, response.getAvance().getPorcentajeAvance());

        verify(proyectoClient, times(1)).obtenerProyectoPorId(1L);
        verify(tareaClient, times(1)).listarTareasPorProyecto(1L);
        verify(equipoClient, times(1)).listarMiembrosPorProyecto(1L);
    }

    @Test
    void obtenerAvanceProyecto_deberiaCalcularCeroCuandoNoHayTareas() {
        ProyectoResponseDTO proyecto = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Proyecto sin tareas")
                .estado("PLANNED")
                .build();

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto);
        when(tareaClient.listarTareasPorProyecto(1L)).thenReturn(List.of());

        AvanceProyectoResponse response = facade.obtenerAvanceProyecto(1L);

        assertNotNull(response);
        assertEquals(0, response.getTotalTareas());
        assertEquals(0.0, response.getPorcentajeAvance());

        verify(proyectoClient, times(1)).obtenerProyectoPorId(1L);
        verify(tareaClient, times(1)).listarTareasPorProyecto(1L);
    }

    @Test
    void crearProyecto_deberiaDelegarEnProyectoClient() {
        ProyectoRequestDTO request = new ProyectoRequestDTO(
                "Portal Innovatech",
                "Gestión de proyectos tecnológicos",
                "PLANNED",
                null,
                null
        );

        ProyectoResponseDTO esperado = ProyectoResponseDTO.builder()
                .id(10L)
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(request.estado())
                .build();

        when(proyectoClient.crearProyecto(request)).thenReturn(esperado);

        ProyectoResponseDTO response = facade.crearProyecto(request);

        assertEquals(10L, response.getId());
        assertEquals("Portal Innovatech", response.getNombre());
        verify(proyectoClient).crearProyecto(request);
    }

    @Test
    void crearTarea_deberiaCrearCuandoProyectoEstaActivoYResponsablePerteneceAlEquipo() {
        TareaRequestDTO request = nuevaTareaRequest("Jonathan");

        TareaResponseDTO esperado = TareaResponseDTO.builder()
                .id(8L)
                .descripcion(request.descripcion())
                .estado(request.estado())
                .idProyecto(request.idProyecto())
                .responsable(request.responsable())
                .build();

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto(1L, "IN_PROGRESS"));
        when(equipoClient.listarMiembrosPorProyecto(1L)).thenReturn(List.of(asignacion(1L, 20L, "Jonathan")));
        when(tareaClient.crearTarea(request)).thenReturn(esperado);

        TareaResponseDTO response = facade.crearTarea(request);

        assertEquals(8L, response.getId());
        assertEquals("Configurar BFF", response.getDescripcion());
        assertEquals("Jonathan", response.getResponsable());
        verify(proyectoClient).obtenerProyectoPorId(1L);
        verify(equipoClient).listarMiembrosPorProyecto(1L);
        verify(tareaClient).crearTarea(request);
    }

    @Test
    void crearTarea_deberiaLanzarBadRequestCuandoProyectoEstaFinalizado() {
        TareaRequestDTO request = nuevaTareaRequest("Jonathan");

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto(1L, "COMPLETED"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> facade.crearTarea(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("finalizado o cancelado"));
        verify(proyectoClient).obtenerProyectoPorId(1L);
        verify(equipoClient, never()).listarMiembrosPorProyecto(anyLong());
        verify(tareaClient, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaLanzarBadRequestCuandoProyectoEstaCancelado() {
        TareaRequestDTO request = nuevaTareaRequest("Jonathan");

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto(1L, "CANCELLED"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> facade.crearTarea(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("finalizado o cancelado"));
        verify(proyectoClient).obtenerProyectoPorId(1L);
        verify(equipoClient, never()).listarMiembrosPorProyecto(anyLong());
        verify(tareaClient, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaLanzarBadRequestCuandoResponsableNoPerteneceAlProyecto() {
        TareaRequestDTO request = nuevaTareaRequest("Persona Externa");

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto(1L, "IN_PROGRESS"));
        when(equipoClient.listarMiembrosPorProyecto(1L)).thenReturn(List.of(asignacion(1L, 20L, "Jonathan")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> facade.crearTarea(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("no pertenece al equipo"));
        verify(proyectoClient).obtenerProyectoPorId(1L);
        verify(equipoClient).listarMiembrosPorProyecto(1L);
        verify(tareaClient, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void crearTarea_deberiaLanzarBadRequestCuandoProyectoNoTieneMiembrosAsignados() {
        TareaRequestDTO request = nuevaTareaRequest("Jonathan");

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyecto(1L, "IN_PROGRESS"));
        when(equipoClient.listarMiembrosPorProyecto(1L)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> facade.crearTarea(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("no tiene miembros asignados"));
        verify(proyectoClient).obtenerProyectoPorId(1L);
        verify(equipoClient).listarMiembrosPorProyecto(1L);
        verify(tareaClient, never()).crearTarea(any(TareaRequestDTO.class));
    }

    @Test
    void cambiarEstadoTarea_deberiaDelegarEnTareaClient() {
        TareaResponseDTO esperado = TareaResponseDTO.builder()
                .id(8L)
                .estado("DONE")
                .idProyecto(1L)
                .build();

        TareaResponseDTO tareaActual = TareaResponseDTO.builder()
                .id(8L)
                .estado("IN_PROGRESS")
                .idProyecto(1L)
                .responsable("Admin Innovatech")
                .build();

        when(tareaClient.obtenerTareaPorId(8L)).thenReturn(tareaActual);
        when(tareaClient.cambiarEstadoTarea(eq(8L), any(TareaClient.CambioEstadoTareaRequest.class)))
                .thenReturn(esperado);

        TareaResponseDTO response = facade.cambiarEstadoTarea(8L, "DONE");

        assertEquals("DONE", response.getEstado());
        verify(tareaClient).cambiarEstadoTarea(eq(8L), any(TareaClient.CambioEstadoTareaRequest.class));
    }


    @Test
    void eliminarTarea_deberiaDelegarEnTareaClientCuandoUsuarioEsAdminYTareaExiste() {
        TareaResponseDTO tarea = TareaResponseDTO.builder()
                .id(8L)
                .descripcion("Configurar BFF")
                .estado("PENDING")
                .idProyecto(1L)
                .responsable("Jonathan")
                .build();

        when(tareaClient.obtenerTareaPorId(8L)).thenReturn(tarea);
        doNothing().when(tareaClient).eliminarTarea(8L);

        facade.eliminarTarea(8L);

        verify(tareaClient).obtenerTareaPorId(8L);
        verify(tareaClient).eliminarTarea(8L);
    }

    @Test
    void eliminarTarea_deberiaLanzarForbiddenCuandoUsuarioNoEsAdmin() {
        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserService.CurrentUser(
                        2L,
                        "pm@innovatech.cl",
                        "PROJECT_MANAGER",
                        "Project Manager"
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> facade.eliminarTarea(8L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(tareaClient, never()).obtenerTareaPorId(anyLong());
        verify(tareaClient, never()).eliminarTarea(anyLong());
    }

    @Test
    void listarYCrearMiembros_deberiaDelegarEnEquipoClient() {
        MiembroEquipoRequest request = new MiembroEquipoRequest(
                "Jorge",
                "Salazar",
                "Parra",
                "jorge@innovatech.cl",
                "DEVELOPER",
                "Jorge123"
        );

        MiembroEquipoResponse miembro = MiembroEquipoResponse.builder()
                .id(3L)
                .nombres("Jorge")
                .apellidoPaterno("Salazar")
                .apellidoMaterno("Parra")
                .email("jorge@innovatech.cl")
                .rol("DEVELOPER")
                .estado("ACTIVO")
                .build();

        when(equipoClient.listarMiembros()).thenReturn(List.of(miembro));
        when(equipoClient.crearMiembro(request)).thenReturn(miembro);

        assertEquals(1, facade.listarMiembros().size());
        assertEquals(3L, facade.crearMiembro(request).getId());

        verify(equipoClient).listarMiembros();
        verify(equipoClient).crearMiembro(request);
    }

    @Test
    void asignarMiembroAProyecto_deberiaDelegarEnEquipoClient() {
        AsignacionProyectoRequest request = new AsignacionProyectoRequest(1L, 3L);
        AsignacionProyectoResponse esperado = AsignacionProyectoResponse.builder()
                .id(5L)
                .idProyecto(1L)
                .idMiembro(3L)
                .nombreMiembro("Jorge Salazar")
                .rolMiembro("DEVELOPER")
                .build();

        when(equipoClient.asignarMiembroAProyecto(request)).thenReturn(esperado);

        AsignacionProyectoResponse response = facade.asignarMiembroAProyecto(request);

        assertEquals(5L, response.getId());
        assertEquals(3L, response.getIdMiembro());
        verify(equipoClient).asignarMiembroAProyecto(request);
    }


    private TareaRequestDTO nuevaTareaRequest(String responsable) {
        return new TareaRequestDTO(
                "Configurar BFF",
                "PENDING",
                1L,
                responsable,
                null,
                null
        );
    }

    private ProyectoResponseDTO proyecto(Long idProyecto, String estado) {
        return ProyectoResponseDTO.builder()
                .id(idProyecto)
                .nombre("Plataforma Innovatech")
                .descripcion("Sistema de gestión de proyectos")
                .estado(estado)
                .build();
    }

    private AsignacionProyectoResponse asignacion(Long idProyecto, Long idMiembro, String nombreMiembro) {
        return AsignacionProyectoResponse.builder()
                .id(100L)
                .idProyecto(idProyecto)
                .idMiembro(idMiembro)
                .nombreMiembro(nombreMiembro)
                .rolMiembro("DEVELOPER")
                .build();
    }
}
