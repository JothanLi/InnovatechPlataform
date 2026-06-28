package com.innovatech.proyectos_service.repository;

import com.innovatech.proyectos_service.model.EstadoProyecto;
import com.innovatech.proyectos_service.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Proyecto> findByEstado(EstadoProyecto estado);
}