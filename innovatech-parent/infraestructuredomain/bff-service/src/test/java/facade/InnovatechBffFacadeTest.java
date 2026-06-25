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
    void crearTarea_deberiaDelegarEnTareaClient() {
        TareaRequestDTO request = new TareaRequestDTO(
                "Configurar BFF",
                "PENDING",
                1L,
                "Jonathan",
                null,
                null
        );

        TareaResponseDTO esperado = TareaResponseDTO.builder()
                .id(8L)
                .descripcion(request.descripcion())
                .estado(request.estado())
                .idProyecto(request.idProyecto())
                .responsable(request.responsable())
                .build();

        when(tareaClient.crearTarea(request)).thenReturn(esperado);

        TareaResponseDTO response = facade.crearTarea(request);

        assertEquals(8L, response.getId());
        assertEquals("Configurar BFF", response.getDescripcion());
        verify(tareaClient).crearTarea(request);
    }

    @Test
    void cambiarEstadoTarea_deberiaDelegarEnTareaClient() {
        TareaResponseDTO esperado = TareaResponseDTO.builder()
                .id(8L)
                .estado("DONE")
                .idProyecto(1L)
                .build();

        ProyectoResponseDTO proyecto = ProyectoResponseDTO.builder()
                .id(1L)
                .nombre("Plataforma Innovatech")
                .estado("IN_PROGRESS")
                .build();

        when(proyectoClient.listarProyectos()).thenReturn(List.of(proyecto));
        when(tareaClient.listarTareasPorProyecto(1L)).thenReturn(List.of(
                TareaResponseDTO.builder()
                        .id(8L)
                        .estado("IN_PROGRESS")
                        .idProyecto(1L)
                        .responsable("Admin Innovatech")
                        .build()
        ));

        when(tareaClient.cambiarEstadoTarea(eq(8L), any(TareaClient.CambioEstadoTareaRequest.class)))
                .thenReturn(esperado);

        TareaResponseDTO response = facade.cambiarEstadoTarea(8L, "DONE");

        assertEquals("DONE", response.getEstado());
        verify(tareaClient).cambiarEstadoTarea(eq(8L), any(TareaClient.CambioEstadoTareaRequest.class));
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
}
