package com.innovatech.equipos_service.integration;

import com.innovatech.equipos_service.adapter.ProyectoServiceAdapter;
import com.innovatech.equipos_service.dto.AsignacionProyectoRequest;
import com.innovatech.equipos_service.dto.AsignacionProyectoResponse;
import com.innovatech.equipos_service.dto.MiembroEquipoRequest;
import com.innovatech.equipos_service.dto.MiembroEquipoResponse;
import com.innovatech.equipos_service.dto.ProyectoAdaptadoResponse;
import com.innovatech.equipos_service.exception.ReglaNegocioException;
import com.innovatech.equipos_service.model.RolEquipo;
import com.innovatech.equipos_service.repository.AsignacionProyectoRepository;
import com.innovatech.equipos_service.repository.MiembroEquipoRepository;
import com.innovatech.equipos_service.service.AsignacionProyectoService;
import com.innovatech.equipos_service.service.MiembroEquipoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equipos_it;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "jwt.secret=12345678901234567890123456789012345678901234567890"
})
@Transactional
class EquipoServiceIntegrationTest {

    @Autowired
    private MiembroEquipoService miembroEquipoService;

    @Autowired
    private AsignacionProyectoService asignacionProyectoService;

    @Autowired
    private MiembroEquipoRepository miembroEquipoRepository;

    @Autowired
    private AsignacionProyectoRepository asignacionProyectoRepository;

    @MockitoBean
    private ProyectoServiceAdapter proyectoServiceAdapter;

    @BeforeEach
    void setUp() {
        asignacionProyectoRepository.deleteAll();
        miembroEquipoRepository.deleteAll();

        when(proyectoServiceAdapter.obtenerProyectoAdaptado(10L))
                .thenReturn(new ProyectoAdaptadoResponse(
                        10L,
                        "Plataforma Innovatech",
                        "IN_PROGRESS"
                ));
    }

    @Test
    void crearMiembroYAsignarloAProyecto_deberiaPersistirRelacionEnH2() {
        MiembroEquipoRequest miembroRequest = new MiembroEquipoRequest(
                "Sebastian",
                "Mariqueo",
                "Prueba",
                "sebastian.integracion@innovatech.cl",
                RolEquipo.DEVELOPER,
                "Password123"
        );

        MiembroEquipoResponse miembroCreado =
                miembroEquipoService.crearMiembro(miembroRequest);

        assertNotNull(miembroCreado.id());
        assertEquals("sebastian.integracion@innovatech.cl", miembroCreado.email());
        assertEquals(RolEquipo.DEVELOPER, miembroCreado.rol());
        assertEquals(1, miembroEquipoRepository.count());

        AsignacionProyectoRequest asignacionRequest =
                new AsignacionProyectoRequest(10L, miembroCreado.id());

        AsignacionProyectoResponse asignacionCreada =
                asignacionProyectoService.asignarMiembroAProyecto(asignacionRequest);

        assertNotNull(asignacionCreada.id());
        assertEquals(10L, asignacionCreada.idProyecto());
        assertEquals("Plataforma Innovatech", asignacionCreada.nombreProyecto());
        assertEquals(miembroCreado.id(), asignacionCreada.idMiembro());
        assertEquals("Sebastian Mariqueo Prueba", asignacionCreada.nombreMiembro());

        assertEquals(1, asignacionProyectoRepository.count());

        verify(proyectoServiceAdapter, times(1)).obtenerProyectoAdaptado(10L);
    }

    @Test
    void asignarMiembroDuplicado_deberiaLanzarReglaNegocioYPersistirSoloUnaAsignacion() {
        MiembroEquipoResponse miembroCreado =
                miembroEquipoService.crearMiembro(new MiembroEquipoRequest(
                        "Camila",
                        "Torres",
                        "Rojas",
                        "camila.integracion@innovatech.cl",
                        RolEquipo.QA,
                        "Password123"
                ));

        AsignacionProyectoRequest request =
                new AsignacionProyectoRequest(10L, miembroCreado.id());

        asignacionProyectoService.asignarMiembroAProyecto(request);

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> asignacionProyectoService.asignarMiembroAProyecto(request)
        );

        assertEquals("El miembro ya se encuentra asignado a este proyecto", exception.getMessage());
        assertEquals(1, asignacionProyectoRepository.count());
    }

    @Test
    void listarAsignacionesPorProyecto_deberiaConsultarDatosPersistidosEnH2() {
        MiembroEquipoResponse miembroCreado =
                miembroEquipoService.crearMiembro(new MiembroEquipoRequest(
                        "Matias",
                        "Perez",
                        "Silva",
                        "matias.integracion@innovatech.cl",
                        RolEquipo.SCRUM_MASTER,
                        "Password123"
                ));

        asignacionProyectoService.asignarMiembroAProyecto(
                new AsignacionProyectoRequest(10L, miembroCreado.id())
        );

        List<AsignacionProyectoResponse> asignaciones =
                asignacionProyectoService.listarAsignacionesPorProyecto(10L);

        assertEquals(1, asignaciones.size());
        assertEquals(10L, asignaciones.get(0).idProyecto());
        assertEquals("Plataforma Innovatech", asignaciones.get(0).nombreProyecto());
        assertEquals(miembroCreado.id(), asignaciones.get(0).idMiembro());
        assertEquals("Matias Perez Silva", asignaciones.get(0).nombreMiembro());

        verify(proyectoServiceAdapter, times(2)).obtenerProyectoAdaptado(10L);
    }
}