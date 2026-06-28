package com.innovatech.equipos_service.repository;

import com.innovatech.equipos_service.model.MiembroEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MiembroEquipoRepository extends JpaRepository<MiembroEquipo, Long> {

    boolean existsByEmail(String email);

    Optional<MiembroEquipo> findByEmail(String email);
}