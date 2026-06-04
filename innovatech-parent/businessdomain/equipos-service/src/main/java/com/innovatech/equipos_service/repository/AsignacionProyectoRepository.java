package com.innovatech.equipos_service.repository;

import com.innovatech.equipos_service.model.AsignacionProyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsignacionProyectoRepository extends JpaRepository<AsignacionProyecto, Long> {

    List<AsignacionProyecto> findByIdProyecto(Long idProyecto);

    boolean existsByIdProyectoAndMiembroId(Long idProyecto, Long idMiembro);
}