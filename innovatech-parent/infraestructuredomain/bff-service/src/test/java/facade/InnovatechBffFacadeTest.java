package facade;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.client.ProyectoClient;
import com.innovatech.bff_service.client.TareaClient;
import com.innovatech.bff_service.dto.AsignacionProyectoResponse;
import com.innovatech.bff_service.dto.AvanceProyectoResponse;
import com.innovatech.bff_service.dto.ProyectoDetalleResponse;
import com.innovatech.bff_service.dto.ProyectoResponseDTO;
import com.innovatech.bff_service.dto.TareaResponseDTO;
import com.innovatech.bff_service.facade.InnovatechBffFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InnovatechBffFacadeTest {

    private ProyectoClient proyectoClient;
    private TareaClient tareaClient;
    private EquipoClient equipoClient;
    private InnovatechBffFacade facade;

    @BeforeEach
    void setUp() {
        proyectoClient = mock(ProyectoClient.class);
        tareaClient = mock(TareaClient.class);
        equipoClient = mock(EquipoClient.class);

        facade = new InnovatechBffFacade(
                proyectoClient,
                tareaClient,
                equipoClient
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
}