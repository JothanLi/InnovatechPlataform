package com.innovatech.tareas_service.repository;

import com.innovatech.tareas_service.model.EstadoTarea;
import com.innovatech.tareas_service.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByIdProyecto(Long idProyecto);

    List<Tarea> findByEstado(EstadoTarea estado);

    boolean existsByIdProyectoAndEstadoNot(Long idProyecto, EstadoTarea estado);
}