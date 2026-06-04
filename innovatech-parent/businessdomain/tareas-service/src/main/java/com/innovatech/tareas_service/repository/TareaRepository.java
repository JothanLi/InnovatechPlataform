package com.innovatech.tareas_service.repository;

import com.innovatech.tareas_service.entity.EstadoTarea;
import com.innovatech.tareas_service.entity.PrioridadTarea;
import com.innovatech.tareas_service.entity.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    Page<Tarea> findByProyectoId(Long proyectoId, Pageable pageable);

    Page<Tarea> findByEquipoId(Long equipoId, Pageable pageable);

    Page<Tarea> findByAsignadoA(Long asignadoA, Pageable pageable);

    Page<Tarea> findByEstado(EstadoTarea estado, Pageable pageable);

    Page<Tarea> findByPrioridad(PrioridadTarea prioridad, Pageable pageable);

    List<Tarea> findByProyectoIdAndEstado(Long proyectoId, EstadoTarea estado);

    List<Tarea> findByEquipoIdAndEstado(Long equipoId, EstadoTarea estado);

    @Query("SELECT t FROM Tarea t WHERE t.fechaVencimiento BETWEEN :inicio AND :fin")
    List<Tarea> findTareasPorRangoFecha(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT t FROM Tarea t WHERE t.estado = :estado AND t.prioridad = :prioridad")
    Page<Tarea> findByEstadoAndPrioridad(
            @Param("estado") EstadoTarea estado,
            @Param("prioridad") PrioridadTarea prioridad,
            Pageable pageable
    );

    long countByEstado(EstadoTarea estado);

    long countByProyectoId(Long proyectoId);

    long countByEquipoId(Long equipoId);
}
